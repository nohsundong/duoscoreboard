package com.myowndime.duoscoreboard.ui.scoreboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.myowndime.duoscoreboard.R
import kotlinx.coroutines.delay

/** Formats [millis] as m:ss, or h:mm:ss once it reaches an hour. */
internal fun formatTimer(millis: Long): String {
    val totalSeconds = (millis.coerceAtLeast(0L) / 1000L)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes, seconds)
    } else {
        "%d:%02d".format(minutes, seconds)
    }
}

/**
 * Stopwatch or countdown shown in the scoreboard's bottom bar.
 *
 * It owns its own state: changing the mode or length in settings resets it, and it
 * keeps counting while the screen rotates.
 */
@Composable
fun TimerBar(
    countdown: Boolean,
    countdownMinutes: Int,
    modifier: Modifier = Modifier,
) {
    val limitMillis = countdownMinutes * 60_000L
    var elapsedMillis by rememberSaveable { mutableLongStateOf(0L) }
    var running by rememberSaveable { mutableStateOf(false) }

    // A different mode or length makes the old reading meaningless.
    // Compared against a saved value so a rotation (which re-runs effects) doesn't reset it.
    val config = if (countdown) countdownMinutes else -1
    var appliedConfig by rememberSaveable { mutableIntStateOf(config) }
    if (appliedConfig != config) {
        appliedConfig = config
        running = false
        elapsedMillis = 0L
    }

    LaunchedEffect(running, countdown, limitMillis) {
        if (!running) return@LaunchedEffect
        var last = System.nanoTime()
        while (true) {
            delay(100)
            val now = System.nanoTime()
            elapsedMillis += (now - last) / 1_000_000L
            last = now
            if (countdown && elapsedMillis >= limitMillis) {
                elapsedMillis = limitMillis
                running = false
                break
            }
        }
    }

    val finished = countdown && elapsedMillis >= limitMillis
    val shownMillis = if (countdown) limitMillis - elapsedMillis else elapsedMillis

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = if (finished) {
                stringResource(R.string.timer_time_up)
            } else {
                formatTimer(if (countdown) shownMillis + 999L else shownMillis)
            },
            style = MaterialTheme.typography.titleLarge,
            color = if (finished) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.testTag(ScoreboardTestTags.TIMER_DISPLAY),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(
                onClick = { running = !running },
                enabled = !finished,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.testTag(ScoreboardTestTags.TIMER_TOGGLE),
            ) {
                Text(
                    stringResource(if (running) R.string.timer_pause else R.string.timer_start),
                )
            }
            TextButton(
                onClick = {
                    running = false
                    elapsedMillis = 0L
                },
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier.testTag(ScoreboardTestTags.TIMER_RESET),
            ) {
                Text(stringResource(R.string.timer_reset))
            }
        }
    }
}
