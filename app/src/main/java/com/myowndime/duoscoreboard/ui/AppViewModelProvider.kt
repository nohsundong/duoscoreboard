package com.myowndime.duoscoreboard.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.myowndime.duoscoreboard.DuoScoreboardApp
import com.myowndime.duoscoreboard.ui.history.HistoryViewModel
import com.myowndime.duoscoreboard.ui.scoreboard.ScoreboardViewModel
import com.myowndime.duoscoreboard.ui.settings.SettingsViewModel

object AppViewModelProvider {

    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val app = duoScoreboardApp()
            ScoreboardViewModel(
                settingsRepository = app.container.settingsRepository,
                matchRepository = app.container.matchRepository,
            )
        }
        initializer {
            HistoryViewModel(matchRepository = duoScoreboardApp().container.matchRepository)
        }
        initializer {
            SettingsViewModel(settingsRepository = duoScoreboardApp().container.settingsRepository)
        }
    }
}

private fun CreationExtras.duoScoreboardApp(): DuoScoreboardApp =
    checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as DuoScoreboardApp
