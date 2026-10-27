package com.myowndime.duoscoreboard.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.myowndime.duoscoreboard.ui.AppViewModelProvider

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()

    SettingsScreen(
        settings = settings,
        onNameChange = viewModel::setName,
        onColorChange = viewModel::setColor,
        onThemeChange = viewModel::setThemeMode,
        onKeepScreenOnChange = viewModel::setKeepScreenOn,
        onSpeakScoresChange = viewModel::setSpeakScores,
        onSetTargetChange = viewModel::setSetTargetScore,
        onDeuceChange = viewModel::setDeuceEnabled,
        onMatchFormatChange = viewModel::setMatchFormatSets,
        onPadScoreDigitsChange = viewModel::setPadScoreDigits,
        onBack = onBack,
    )
}
