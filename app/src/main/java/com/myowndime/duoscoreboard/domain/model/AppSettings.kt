package com.myowndime.duoscoreboard.domain.model

/** How the app picks between the light and dark color scheme. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

/**
 * A player's customizable appearance.
 *
 * [name] is stored blank until the user types one; the UI then falls back to a
 * localized default so the stored value never depends on the device language.
 */
data class PlayerProfile(
    val name: String,
    val colorArgb: Long,
)

/** Everything the user can configure. Stored locally in DataStore, never transmitted. */
data class AppSettings(
    val playerOne: PlayerProfile = PlayerProfile(name = "", colorArgb = DEFAULT_PLAYER_ONE_COLOR),
    val playerTwo: PlayerProfile = PlayerProfile(name = "", colorArgb = DEFAULT_PLAYER_TWO_COLOR),
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val keepScreenOn: Boolean = true,
    /** Reads every score change out loud through the on-device speech engine. */
    val speakScores: Boolean = false,
    /**
     * Points that win a set, or [SET_TARGET_MANUAL] to end sets by hand.
     * Whether reaching it also needs a two point margin is controlled by [deuceEnabled].
     */
    val setTargetScore: Int = SET_TARGET_MANUAL,
    /**
     * Whether a set at the target score still needs a two point margin to end
     * (e.g. 11:10 keeps going, 12:10 wins). Has no effect while sets are ended by hand.
     */
    val deuceEnabled: Boolean = true,
    /**
     * Sets needed to win the whole match (e.g. 3 = "3전 2선승"), or
     * [MATCH_FORMAT_MANUAL] to keep playing sets until the finish-match button is pressed.
     */
    val matchFormatSets: Int = MATCH_FORMAT_MANUAL,
    /** True once the first-run rules setup has been shown and confirmed. */
    val hasCompletedFirstRunSetup: Boolean = false,
    /** Shows scores zero-padded to three digits (007, 000) instead of plain numbers. */
    val padScoreDigits: Boolean = false,
) {
    fun profileOf(player: Player): PlayerProfile = when (player) {
        Player.ONE -> playerOne
        Player.TWO -> playerTwo
    }

    companion object {
        const val DEFAULT_PLAYER_ONE_COLOR: Long = 0xFF1565C0
        const val DEFAULT_PLAYER_TWO_COLOR: Long = 0xFFC62828
        const val MAX_NAME_LENGTH: Int = 16

        /** Sets are ended with the button instead of by a point target. */
        const val SET_TARGET_MANUAL: Int = 0

        /** Point margin a player needs on top of the target to take a set, when deuce applies. */
        const val SET_WIN_MARGIN: Int = 2

        /** Targets offered in settings: table tennis, badminton/squash, volleyball. */
        val SET_TARGET_OPTIONS: List<Int> = listOf(SET_TARGET_MANUAL, 11, 15, 21, 25)

        /** The match has no automatic end; the finish-match button decides when it's over. */
        const val MATCH_FORMAT_MANUAL: Int = 0

        /** Total sets in the match: manual, single set, or a best-of-N (odd) format. */
        val MATCH_FORMAT_OPTIONS: List<Int> = listOf(MATCH_FORMAT_MANUAL, 1, 3, 5, 7)

        /**
         * Sets a player must win to take the match under [matchFormatSets], or 0 if the
         * match has no automatic end (manual format, or best-of-1 needs exactly 1).
         */
        fun setsToWinMatch(matchFormatSets: Int): Int =
            if (matchFormatSets <= 0) 0 else matchFormatSets / 2 + 1
    }
}
