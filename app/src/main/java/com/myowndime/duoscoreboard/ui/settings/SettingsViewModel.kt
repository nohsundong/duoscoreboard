package com.myowndime.duoscoreboard.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myowndime.duoscoreboard.domain.model.AppSettings
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.domain.model.ThemeMode
import com.myowndime.duoscoreboard.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettings(),
    )

    fun setName(player: Player, name: String) {
        viewModelScope.launch { settingsRepository.setPlayerName(player, name) }
    }

    fun setColor(player: Player, colorArgb: Long) {
        viewModelScope.launch { settingsRepository.setPlayerColor(player, colorArgb) }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(themeMode) }
    }

    fun setKeepScreenOn(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setKeepScreenOn(enabled) }
    }

    fun setSpeakScores(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSpeakScores(enabled) }
    }

    fun setSetTargetScore(target: Int) {
        viewModelScope.launch { settingsRepository.setSetTargetScore(target) }
    }

    fun setDeuceEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setDeuceEnabled(enabled) }
    }

    fun setMatchFormatSets(matchFormatSets: Int) {
        viewModelScope.launch { settingsRepository.setMatchFormatSets(matchFormatSets) }
    }

    fun setFirstRunSetupCompleted() {
        viewModelScope.launch { settingsRepository.setFirstRunSetupCompleted() }
    }

    fun setPadScoreDigits(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setPadScoreDigits(enabled) }
    }
}
