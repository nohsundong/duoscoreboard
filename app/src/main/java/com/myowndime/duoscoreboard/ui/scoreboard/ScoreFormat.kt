package com.myowndime.duoscoreboard.ui.scoreboard

/** Digits shown when scores are zero-padded; matches the 0..999 score range. */
const val PADDED_SCORE_DIGITS: Int = 3

/** Formats a score for display: `7` normally, `007` when [padded]. */
fun formatScore(score: Int, padded: Boolean): String =
    if (padded) score.toString().padStart(PADDED_SCORE_DIGITS, '0') else score.toString()
