package com.myowndime.duoscoreboard.testing

import com.myowndime.duoscoreboard.domain.model.AppSettings
import com.myowndime.duoscoreboard.domain.model.MatchRecord
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.domain.model.ThemeMode
import com.myowndime.duoscoreboard.domain.repository.MatchRepository
import com.myowndime.duoscoreboard.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(
    initial: AppSettings = AppSettings(),
) : SettingsRepository {

    private val state = MutableStateFlow(initial)

    override val settings: Flow<AppSettings> = state

    val current: AppSettings get() = state.value

    override suspend fun setPlayerName(player: Player, name: String) {
        val sanitized = name.trim().take(AppSettings.MAX_NAME_LENGTH)
        state.value = when (player) {
            Player.ONE -> state.value.copy(
                playerOne = state.value.playerOne.copy(name = sanitized),
            )
            Player.TWO -> state.value.copy(
                playerTwo = state.value.playerTwo.copy(name = sanitized),
            )
        }
    }

    override suspend fun setPlayerColor(player: Player, colorArgb: Long) {
        state.value = when (player) {
            Player.ONE -> state.value.copy(
                playerOne = state.value.playerOne.copy(colorArgb = colorArgb),
            )
            Player.TWO -> state.value.copy(
                playerTwo = state.value.playerTwo.copy(colorArgb = colorArgb),
            )
        }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        state.value = state.value.copy(themeMode = themeMode)
    }

    override suspend fun setKeepScreenOn(enabled: Boolean) {
        state.value = state.value.copy(keepScreenOn = enabled)
    }

    override suspend fun setSpeakScores(enabled: Boolean) {
        state.value = state.value.copy(speakScores = enabled)
    }

    override suspend fun setSetTargetScore(target: Int) {
        state.value = state.value.copy(setTargetScore = target)
    }

    override suspend fun setDeuceEnabled(enabled: Boolean) {
        state.value = state.value.copy(deuceEnabled = enabled)
    }

    override suspend fun setMatchFormatSets(matchFormatSets: Int) {
        state.value = state.value.copy(matchFormatSets = matchFormatSets)
    }

    override suspend fun setFirstRunSetupCompleted() {
        state.value = state.value.copy(hasCompletedFirstRunSetup = true)
    }

    override suspend fun setPadScoreDigits(enabled: Boolean) {
        state.value = state.value.copy(padScoreDigits = enabled)
    }
}

class FakeMatchRepository : MatchRepository {

    private val state = MutableStateFlow<List<MatchRecord>>(emptyList())
    private var nextId = 1L

    override val matches: Flow<List<MatchRecord>> = state

    val current: List<MatchRecord> get() = state.value

    override suspend fun add(record: MatchRecord): Long {
        val id = nextId++
        state.value = listOf(record.copy(id = id)) + state.value
        return id
    }

    override suspend fun delete(id: Long) {
        state.value = state.value.filterNot { it.id == id }
    }

    override suspend fun clear() {
        state.value = emptyList()
    }
}
