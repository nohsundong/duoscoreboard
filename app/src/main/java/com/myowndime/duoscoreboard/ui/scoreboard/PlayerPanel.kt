package com.myowndime.duoscoreboard.ui.scoreboard

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import com.myowndime.duoscoreboard.R
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.ui.theme.contentColorOn

private val PanelCorner = 24.dp
private val SwipeThreshold = 56.dp

/**
 * One player's half of the board.
 *
 * Gestures: tapping anywhere adds a point, a long press or a downward swipe removes
 * one, an upward swipe adds one. The explicit +/- buttons and the custom
 * accessibility actions expose the same behaviour to screen reader and switch users.
 */
@Composable
fun PlayerPanel(
    player: Player,
    name: String,
    score: Int,
    panelColor: Color,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
    sets: Int = 0,
    showSets: Boolean = false,
    padScore: Boolean = false,
) {
    val contentColor = contentColorOn(panelColor)
    val haptics = LocalHapticFeedback.current
    val currentIncrement by rememberUpdatedState(onIncrement)
    val currentDecrement by rememberUpdatedState(onDecrement)

    val incrementLabel = stringResource(R.string.action_increment, name)
    val decrementLabel = stringResource(R.string.action_decrement, name)
    val setsLabel = stringResource(R.string.sets_label, sets)
    val scoreDescription = pluralStringResource(
        id = R.plurals.player_panel_description,
        count = score,
        name,
        score,
    )
    val panelDescription = if (showSets) "$setsLabel. $scoreDescription" else scoreDescription

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(PanelCorner))
            .background(panelColor)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { currentIncrement() },
                    onLongPress = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        currentDecrement()
                    },
                )
            }
            .pointerInput(Unit) {
                val threshold = SwipeThreshold.toPx()
                var dragged = 0f
                detectVerticalDragGestures(
                    onDragStart = { dragged = 0f },
                    onDragCancel = { dragged = 0f },
                    onDragEnd = {
                        when {
                            dragged > threshold -> currentDecrement()
                            dragged < -threshold -> currentIncrement()
                        }
                        dragged = 0f
                    },
                ) { change, dragAmount ->
                    dragged += dragAmount
                    change.consume()
                }
            }
            .semantics {
                contentDescription = panelDescription
                customActions = listOf(
                    CustomAccessibilityAction(incrementLabel) {
                        currentIncrement()
                        true
                    },
                    CustomAccessibilityAction(decrementLabel) {
                        currentDecrement()
                        true
                    },
                )
            }
            .testTag(ScoreboardTestTags.panel(player)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = name,
                color = contentColor,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                val displayedScore = formatScore(score, padScore)
                val ratio = when (displayedScore.length) {
                    1 -> 0.72f
                    2 -> 0.60f
                    else -> 0.44f
                }
                // Converting dp to sp keeps the number inside the panel at every system
                // font scale; the score is by far the largest text on the screen already.
                val fontSize = with(LocalDensity.current) {
                    (min(maxWidth, maxHeight) * ratio).toSp()
                }
                Text(
                    text = displayedScore,
                    color = contentColor,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.testTag(ScoreboardTestTags.score(player)),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PanelButton(
                    symbol = "−",
                    label = decrementLabel,
                    contentColor = contentColor,
                    testTag = ScoreboardTestTags.decrement(player),
                    onClick = { currentDecrement() },
                )
                PanelButton(
                    symbol = "+",
                    label = incrementLabel,
                    contentColor = contentColor,
                    testTag = ScoreboardTestTags.increment(player),
                    onClick = { currentIncrement() },
                )
            }
        }
    }
}

@Composable
private fun PanelButton(
    symbol: String,
    label: String,
    contentColor: Color,
    testTag: String,
    onClick: () -> Unit,
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(contentColor.copy(alpha = 0.18f))
            .semantics { contentDescription = label }
            .testTag(testTag),
    ) {
        Text(
            text = symbol,
            color = contentColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
