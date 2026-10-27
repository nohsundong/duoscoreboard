package com.myowndime.duoscoreboard.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.myowndime.duoscoreboard.domain.model.AppSettings
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.domain.model.PlayerProfile
import com.myowndime.duoscoreboard.domain.model.ThemeMode
import com.myowndime.duoscoreboard.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private const val DATA_STORE_NAME = "settings"

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = DATA_STORE_NAME,
)

/**
 * Stores preferences in a local DataStore file inside the app sandbox.
 * Nothing here is synced, uploaded or shared with other apps.
 */
class DataStoreSettingsRepository(
    private val dataStore: DataStore<Preferences>,
) : SettingsRepository {

    constructor(context: Context) : this(context.applicationContext.settingsDataStore)

    override val settings: Flow<AppSettings> = dataStore.data
        .catch { throwable ->
            // A corrupted or unreadable file must not crash the scoreboard.
            if (throwable is IOException) emit(emptyPreferences()) else throw throwable
        }
        .map { preferences -> preferences.toAppSettings() }

    override suspend fun setPlayerName(player: Player, name: String) {
        val sanitized = name.trim().take(AppSettings.MAX_NAME_LENGTH)
        dataStore.edit { preferences ->
            preferences[nameKey(player)] = sanitized
        }
    }

    override suspend fun setPlayerColor(player: Player, colorArgb: Long) {
        dataStore.edit { preferences ->
            preferences[colorKey(player)] = colorArgb
        }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[Keys.THEME_MODE] = themeMode.name
        }
    }

    override suspend fun setKeepScreenOn(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.KEEP_SCREEN_ON] = enabled
        }
    }

    override suspend fun setSpeakScores(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.SPEAK_SCORES] = enabled
        }
    }

    override suspend fun setSetTargetScore(target: Int) {
        val allowed = if (target in AppSettings.SET_TARGET_OPTIONS) {
            target
        } else {
            AppSettings.SET_TARGET_MANUAL
        }
        dataStore.edit { preferences ->
            preferences[Keys.SET_TARGET_SCORE] = allowed
        }
    }

    override suspend fun setDeuceEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.DEUCE_ENABLED] = enabled
        }
    }

    override suspend fun setMatchFormatSets(matchFormatSets: Int) {
        val allowed = if (matchFormatSets in AppSettings.MATCH_FORMAT_OPTIONS) {
            matchFormatSets
        } else {
            AppSettings.MATCH_FORMAT_MANUAL
        }
        dataStore.edit { preferences ->
            preferences[Keys.MATCH_FORMAT_SETS] = allowed
        }
    }

    override suspend fun setFirstRunSetupCompleted() {
        dataStore.edit { preferences ->
            preferences[Keys.FIRST_RUN_SETUP_DONE] = true
        }
    }

    override suspend fun setPadScoreDigits(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[Keys.PAD_SCORE_DIGITS] = enabled
        }
    }

    private object Keys {
        val PLAYER_ONE_NAME = stringPreferencesKey("player_one_name")
        val PLAYER_TWO_NAME = stringPreferencesKey("player_two_name")
        val PLAYER_ONE_COLOR = longPreferencesKey("player_one_color")
        val PLAYER_TWO_COLOR = longPreferencesKey("player_two_color")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val SPEAK_SCORES = booleanPreferencesKey("speak_scores")
        val SET_TARGET_SCORE = intPreferencesKey("set_target_score")
        val DEUCE_ENABLED = booleanPreferencesKey("deuce_enabled")
        val MATCH_FORMAT_SETS = intPreferencesKey("match_format_sets")
        val FIRST_RUN_SETUP_DONE = booleanPreferencesKey("first_run_setup_done")
        val PAD_SCORE_DIGITS = booleanPreferencesKey("pad_score_digits")
    }

    private fun nameKey(player: Player) = when (player) {
        Player.ONE -> Keys.PLAYER_ONE_NAME
        Player.TWO -> Keys.PLAYER_TWO_NAME
    }

    private fun colorKey(player: Player) = when (player) {
        Player.ONE -> Keys.PLAYER_ONE_COLOR
        Player.TWO -> Keys.PLAYER_TWO_COLOR
    }

    private fun Preferences.toAppSettings(): AppSettings {
        val defaults = AppSettings()
        return AppSettings(
            playerOne = PlayerProfile(
                name = this[Keys.PLAYER_ONE_NAME] ?: defaults.playerOne.name,
                colorArgb = this[Keys.PLAYER_ONE_COLOR] ?: defaults.playerOne.colorArgb,
            ),
            playerTwo = PlayerProfile(
                name = this[Keys.PLAYER_TWO_NAME] ?: defaults.playerTwo.name,
                colorArgb = this[Keys.PLAYER_TWO_COLOR] ?: defaults.playerTwo.colorArgb,
            ),
            themeMode = this[Keys.THEME_MODE]?.let { stored ->
                ThemeMode.entries.firstOrNull { it.name == stored }
            } ?: defaults.themeMode,
            keepScreenOn = this[Keys.KEEP_SCREEN_ON] ?: defaults.keepScreenOn,
            speakScores = this[Keys.SPEAK_SCORES] ?: defaults.speakScores,
            setTargetScore = this[Keys.SET_TARGET_SCORE]
                ?.takeIf { it in AppSettings.SET_TARGET_OPTIONS }
                ?: defaults.setTargetScore,
            deuceEnabled = this[Keys.DEUCE_ENABLED] ?: defaults.deuceEnabled,
            matchFormatSets = this[Keys.MATCH_FORMAT_SETS]
                ?.takeIf { it in AppSettings.MATCH_FORMAT_OPTIONS }
                ?: defaults.matchFormatSets,
            hasCompletedFirstRunSetup = this[Keys.FIRST_RUN_SETUP_DONE]
                ?: defaults.hasCompletedFirstRunSetup,
            padScoreDigits = this[Keys.PAD_SCORE_DIGITS] ?: defaults.padScoreDigits,
        )
    }
}
