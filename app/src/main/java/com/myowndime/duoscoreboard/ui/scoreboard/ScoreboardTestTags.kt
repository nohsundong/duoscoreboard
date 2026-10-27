package com.myowndime.duoscoreboard.ui.scoreboard

import com.myowndime.duoscoreboard.domain.model.Player

/** Stable identifiers so UI tests do not depend on localized text. */
object ScoreboardTestTags {
    const val RESET_ACTION: String = "action_reset"
    const val FINISH_SET_ACTION: String = "action_finish_set"
    const val SET_STATUS_BAR: String = "set_status_bar"
    const val SWAP_ACTION: String = "action_swap_sides"
    const val SET_SCORE_ACTION: String = "action_set_score"
    const val FINISH_ACTION: String = "action_finish"
    const val HISTORY_ACTION: String = "action_history"
    const val OVERFLOW_ACTION: String = "action_overflow"
    const val SHARE_ACTION: String = "action_share"
    const val SETTINGS_ACTION: String = "action_settings"

    fun panel(player: Player): String = "panel_${player.name}"

    fun score(player: Player): String = "score_${player.name}"

    fun sets(player: Player): String = "sets_${player.name}"

    fun increment(player: Player): String = "increment_${player.name}"

    fun decrement(player: Player): String = "decrement_${player.name}"
}
