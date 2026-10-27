package com.myowndime.duoscoreboard.ui.scoreboard

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myowndime.duoscoreboard.R
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.ui.AppViewModelProvider
import com.myowndime.duoscoreboard.ui.common.rememberScoreSpeaker
import com.myowndime.duoscoreboard.ui.common.shareText
import com.myowndime.duoscoreboard.ui.settings.SettingsViewModel

@Composable
fun ScoreboardRoute(
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: ScoreboardViewModel = viewModel(factory = AppViewModelProvider.Factory),
    settingsViewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val playerOneName = state.settings.playerOne.name
        .ifBlank { stringResource(R.string.default_player_one_name) }
    val playerTwoName = state.settings.playerTwo.name
        .ifBlank { stringResource(R.string.default_player_two_name) }

    val savedMessage = stringResource(R.string.message_match_saved)
    val emptyMessage = stringResource(R.string.message_match_empty)
    val setTiedMessage = stringResource(R.string.message_set_tied)
    val chooserTitle = stringResource(R.string.share_chooser_title)

    LaunchedEffect(viewModel, savedMessage, emptyMessage, setTiedMessage) {
        viewModel.messages.collect { message ->
            val text = when (message) {
                ScoreboardMessage.MATCH_SAVED -> savedMessage
                ScoreboardMessage.MATCH_EMPTY -> emptyMessage
                ScoreboardMessage.SET_TIED -> setTiedMessage
            }
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(text)
        }
    }

    // Spoken announcements: the event is collected into state so the sentence can be
    // built with the composable resource lookups instead of a raw Context call.
    val speaker = rememberScoreSpeaker(enabled = state.settings.speakScores)
    var pendingAnnouncement by remember { mutableStateOf<ScoreAnnouncement?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.announcements.collect { pendingAnnouncement = it }
    }

    val announcement = pendingAnnouncement
    val announcementText = announcement?.let {
        val name = when (it.player) {
            Player.ONE -> playerOneName
            Player.TWO -> playerTwoName
        }
        pluralStringResource(R.plurals.announce_score, it.score, name, it.score)
    }

    LaunchedEffect(announcement, announcementText, speaker) {
        if (announcement != null && announcementText != null) {
            speaker?.speak(announcementText)
            pendingAnnouncement = null
        }
    }

    ScoreboardScreen(
        state = state,
        playerOneName = playerOneName,
        playerTwoName = playerTwoName,
        onIncrement = viewModel::increment,
        onDecrement = viewModel::decrement,
        onResetConfirmed = viewModel::reset,
        onSwapSides = viewModel::swapSides,
        onSetScores = viewModel::setScores,
        onFinishSetConfirmed = viewModel::finishSet,
        onFinishConfirmed = { viewModel.finishMatch(playerOneName, playerTwoName) },
        onOpenHistory = onOpenHistory,
        onOpenSettings = onOpenSettings,
        onShare = { text -> shareText(context, text, chooserTitle) },
        snackbarHostState = snackbarHostState,
    )

    if (!state.settings.hasCompletedFirstRunSetup) {
        FirstRunSetupDialog(
            onConfirm = { matchFormatSets, setTargetScore, deuceEnabled ->
                settingsViewModel.setMatchFormatSets(matchFormatSets)
                settingsViewModel.setSetTargetScore(setTargetScore)
                settingsViewModel.setDeuceEnabled(deuceEnabled)
                settingsViewModel.setFirstRunSetupCompleted()
            },
        )
    }
}
