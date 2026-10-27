package com.myowndime.duoscoreboard.domain.model

/** Outcome of a finished match. */
enum class MatchResult {
    PLAYER_ONE,
    PLAYER_TWO,
    DRAW,
    ;

    companion object {
        fun of(playerOneScore: Int, playerTwoScore: Int): MatchResult = when {
            playerOneScore > playerTwoScore -> PLAYER_ONE
            playerTwoScore > playerOneScore -> PLAYER_TWO
            else -> DRAW
        }
    }
}

/** A finished match as shown in the history list. Contains no device or user identifiers. */
data class MatchRecord(
    val id: Long = 0L,
    val playerOneName: String,
    val playerTwoName: String,
    val playerOneScore: Int,
    val playerTwoScore: Int,
    val playedAtEpochMillis: Long,
    val playerOneSets: Int = 0,
    val playerTwoSets: Int = 0,
) {
    /** True when the match was played in sets rather than as a single running score. */
    val hasSets: Boolean = playerOneSets > 0 || playerTwoSets > 0

    /**
     * Sets decide the match; the points of the unfinished set only break a tie
     * (2:2 in sets with 7:5 on court goes to player one).
     */
    val result: MatchResult = if (playerOneSets != playerTwoSets) {
        MatchResult.of(playerOneSets, playerTwoSets)
    } else {
        MatchResult.of(playerOneScore, playerTwoScore)
    }

    /** Name of the winner, or `null` for a draw. */
    val winnerName: String? = when (result) {
        MatchResult.PLAYER_ONE -> playerOneName
        MatchResult.PLAYER_TWO -> playerTwoName
        MatchResult.DRAW -> null
    }
}
