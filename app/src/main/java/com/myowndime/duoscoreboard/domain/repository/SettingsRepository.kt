package com.myowndime.duoscoreboard.domain.repository

import com.myowndime.duoscoreboard.domain.model.AppSettings
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/** Local, on-device user preferences. */
interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setPlayerName(player: Player, name: String)

    suspend fun setPlayerColor(player: Player, colorArgb: Long)

    suspend fun setThemeMode(themeMode: ThemeMode)

    suspend fun setKeepScreenOn(enabled: Boolean)

    suspend fun setSpeakScores(enabled: Boolean)

    /** [AppSettings.SET_TARGET_MANUAL] ends sets by hand; any other value auto-ends them. */
    suspend fun setSetTargetScore(target: Int)

    /** Whether reaching the set target still needs a two point margin to win it. */
    suspend fun setDeuceEnabled(enabled: Boolean)

    /** [AppSettings.MATCH_FORMAT_MANUAL] or a best-of-N sets-in-the-match value. */
    suspend fun setMatchFormatSets(matchFormatSets: Int)

    /** Marks the first-run rules setup as shown so it never appears again. */
    suspend fun setFirstRunSetupCompleted()

    /** Whether scores are shown zero-padded to three digits (000). */
    suspend fun setPadScoreDigits(enabled: Boolean)
}
