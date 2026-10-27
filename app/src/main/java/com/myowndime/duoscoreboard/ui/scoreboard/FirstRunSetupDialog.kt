package com.myowndime.duoscoreboard.ui.scoreboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.myowndime.duoscoreboard.R
import com.myowndime.duoscoreboard.domain.model.AppSettings
import com.myowndime.duoscoreboard.ui.settings.MatchRulesControls
import kotlinx.coroutines.delay

object FirstRunSetupTestTags {
    const val CONFIRM: String = "first_run_confirm"
}

/**
 * Shown once, on the very first launch, so a match can start with the right rules
 * instead of the user discovering the settings screen mid-game. Cannot be dismissed
 * without choosing - closing it any other way would leave the rules undecided.
 */
@Composable
fun FirstRunSetupDialog(
    onConfirm: (matchFormatSets: Int, setTargetScore: Int, deuceEnabled: Boolean) -> Unit,
) {
    var matchFormatSets by rememberSaveable { mutableStateOf(AppSettings.MATCH_FORMAT_MANUAL) }
    var setTargetScore by rememberSaveable { mutableStateOf(AppSettings.SET_TARGET_MANUAL) }
    var deuceEnabled by rememberSaveable { mutableStateOf(true) }

    // This dialog is the very first thing drawn after the app launches. The touch-up
    // from the finger that tapped the launcher icon can land on whatever the new
    // screen renders at that spot, which is this dialog's confirm button since it
    // sits in the same bottom-right corner most launcher grids place icons in. A
    // short ignore-window absorbs that stray touch instead of auto-confirming
    // default, unchosen rules.
    var confirmEnabled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(CONFIRM_ENABLE_DELAY_MS)
        confirmEnabled = true
    }

    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        title = { Text(stringResource(R.string.first_run_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.first_run_message),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                MatchRulesControls(
                    matchFormatSets = matchFormatSets,
                    setTargetScore = setTargetScore,
                    deuceEnabled = deuceEnabled,
                    onMatchFormatChange = { matchFormatSets = it },
                    onSetTargetChange = { setTargetScore = it },
                    onDeuceChange = { deuceEnabled = it },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(matchFormatSets, setTargetScore, deuceEnabled) },
                enabled = confirmEnabled,
                modifier = Modifier.testTag(FirstRunSetupTestTags.CONFIRM),
            ) {
                Text(stringResource(R.string.first_run_start))
            }
        },
    )
}

private const val CONFIRM_ENABLE_DELAY_MS = 500L
