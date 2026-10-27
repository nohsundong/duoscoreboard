package com.myowndime.duoscoreboard.ui.scoreboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myowndime.duoscoreboard.domain.model.AppSettings
import com.myowndime.duoscoreboard.domain.model.MatchRecord
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.domain.repository.MatchRepository
import com.myowndime.duoscoreboard.domain.repository.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch
import kotlin.math.abs

data class ScoreboardUiState(
    val settings: AppSettings = AppSettings(),
    val playerOneScore: Int = 0,
    val playerTwoScore: Int = 0,
    val playerOneSets: Int = 0,
    val playerTwoSets: Int = 0,
    /** When true player two is drawn first (left / top), e.g. after changing ends. */
    val sidesSwapped: Boolean = false,
) {
    val hasScore: Boolean get() = playerOneScore > 0 || playerTwoScore > 0

    /** The player drawn on the left (landscape) or top (portrait). */
    val firstPlayer: Player get() = if (sidesSwapped) Player.TWO else Player.ONE

    /** The player drawn on the right (landscape) or bottom (portrait). */
    val secondPlayer: Player get() = if (sidesSwapped) Player.ONE else Player.TWO

    /** Sets are only worth screen space once they are actually being played. */
    val showSets: Boolean
        get() = playerOneSets > 0 ||
            playerTwoSets > 0 ||
            settings.setTargetScore != AppSettings.SET_TARGET_MANUAL ||
            settings.matchFormatSets != AppSettings.MATCH_FORMAT_MANUAL

    /** Non-null once a player has won enough sets to take the whole match. */
    val matchWinner: Player?
        get() {
            val setsToWin = AppSettings.setsToWinMatch(settings.matchFormatSets)
            return when {
                setsToWin <= 0 -> null
                playerOneSets >= setsToWin -> Player.ONE
                playerTwoSets >= setsToWin -> Player.TWO
                else -> null
            }
        }

    fun scoreOf(player: Player): Int = when (player) {
        Player.ONE -> playerOneScore
        Player.TWO -> playerTwoScore
    }

    fun setsOf(player: Player): Int = when (player) {
        Player.ONE -> playerOneSets
        Player.TWO -> playerTwoSets
    }
}

/** One-shot user feedback, consumed by the screen and shown in a snackbar. */
enum class ScoreboardMessage {
    MATCH_SAVED,
    MATCH_EMPTY,
    SET_TIED,
}

/** A score change worth reading out loud. */
data class ScoreAnnouncement(
    val player: Player,
    val score: Int,
)

class ScoreboardViewModel(
    private val settingsRepository: SettingsRepository,
    private val matchRepository: MatchRepository,
    private val now: () -> Long = System::currentTimeMillis,
) : ViewModel() {

    private data class Scores(
        val one: Int = 0,
        val two: Int = 0,
        val setsOne: Int = 0,
        val setsTwo: Int = 0,
    ) {
        val isBlank: Boolean get() = one == 0 && two == 0 && setsOne == 0 && setsTwo == 0
    }

    private val scores = MutableStateFlow(Scores())
    private val sidesSwapped = MutableStateFlow(false)

    private val messageChannel = Channel<ScoreboardMessage>(Channel.BUFFERED)
    val messages: Flow<ScoreboardMessage> = messageChannel.receiveAsFlow()

    // Conflated: when points are tapped in quick succession only the latest score is
    // worth speaking, the earlier ones would already be stale by the time they play.
    private val announcementChannel = Channel<ScoreAnnouncement>(Channel.CONFLATED)
    val announcements: Flow<ScoreAnnouncement> = announcementChannel.receiveAsFlow()

    // Kept as its own StateFlow so the set rules can read the current target
    // synchronously, right inside the score change that may complete a set.
    private val settings: StateFlow<AppSettings> = settingsRepository.settings.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = AppSettings(),
    )

    val uiState: StateFlow<ScoreboardUiState> =
        combine(settings, scores, sidesSwapped) { currentSettings, score, swapped ->
            ScoreboardUiState(
                settings = currentSettings,
                playerOneScore = score.one,
                playerTwoScore = score.two,
                playerOneSets = score.setsOne,
                playerTwoSets = score.setsTwo,
                sidesSwapped = swapped,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ScoreboardUiState(),
        )

    fun increment(player: Player) = changeScore(player, +1)

    fun decrement(player: Player) = changeScore(player, -1)

    /**
     * Awards the running set to whoever leads and starts the next one at 0:0.
     * A tie cannot end a set, so nothing happens beyond a message.
     */
    fun finishSet() {
        val current = scores.value
        if (current.one == current.two) {
            messageChannel.trySend(ScoreboardMessage.SET_TIED)
            return
        }
        scores.value = current.awardSet()
    }

    /** Clears points and sets, ready for a completely new match. */
    fun reset() {
        scores.value = Scores()
        sidesSwapped.value = false
    }

    /** Changes ends: the two players trade places on screen, scores stay with their owner. */
    fun swapSides() {
        sidesSwapped.update { !it }
    }

    /**
     * Sets both running scores directly, e.g. to resume a game or fix a mistake.
     * Values are clamped to 0..[MAX_SCORE]. Sets are left alone, and no set is awarded
     * even if a value reaches the target - the next scored point decides that.
     */
    fun setScores(playerOneScore: Int, playerTwoScore: Int) {
        scores.update { current ->
            current.copy(one = clamp(playerOneScore), two = clamp(playerTwoScore))
        }
    }

    /**
     * Saves the current state as a match and clears the board.
     *
     * The display names are passed in because they are localized in the UI layer
     * when the user has not chosen a custom name.
     */
    fun finishMatch(playerOneName: String, playerTwoName: String) {
        val current = scores.value
        if (current.isBlank) {
            messageChannel.trySend(ScoreboardMessage.MATCH_EMPTY)
            return
        }
        viewModelScope.launch {
            matchRepository.add(
                MatchRecord(
                    playerOneName = playerOneName,
                    playerTwoName = playerTwoName,
                    playerOneScore = current.one,
                    playerTwoScore = current.two,
                    playedAtEpochMillis = now(),
                    playerOneSets = current.setsOne,
                    playerTwoSets = current.setsTwo,
                ),
            )
            scores.value = Scores()
            sidesSwapped.value = false
            messageChannel.send(ScoreboardMessage.MATCH_SAVED)
        }
    }

    private fun changeScore(player: Player, delta: Int) {
        val updated = scores.updateAndGet { current ->
            when (player) {
                Player.ONE -> current.copy(one = clamp(current.one + delta))
                Player.TWO -> current.copy(two = clamp(current.two + delta))
            }
        }
        val newScore = when (player) {
            Player.ONE -> updated.one
            Player.TWO -> updated.two
        }
        announcementChannel.trySend(ScoreAnnouncement(player, newScore))

        // Only a scored point can win a set - correcting a score downwards must never
        // hand the set to somebody.
        if (delta > 0) {
            autoFinishSetIfTargetReached(updated)
        }
    }

    private fun autoFinishSetIfTargetReached(current: Scores) {
        val currentSettings = settings.value
        val target = currentSettings.setTargetScore
        if (target == AppSettings.SET_TARGET_MANUAL) return

        val requiredMargin = if (currentSettings.deuceEnabled) AppSettings.SET_WIN_MARGIN else 1
        val leadingScore = maxOf(current.one, current.two)
        val margin = abs(current.one - current.two)
        if (leadingScore >= target && margin >= requiredMargin) {
            scores.value = current.awardSet()
        }
    }

    private fun Scores.awardSet(): Scores = if (one > two) {
        copy(one = 0, two = 0, setsOne = setsOne + 1)
    } else {
        copy(one = 0, two = 0, setsTwo = setsTwo + 1)
    }

    private fun clamp(value: Int): Int = value.coerceIn(MIN_SCORE, MAX_SCORE)

    companion object {
        const val MIN_SCORE: Int = 0
        const val MAX_SCORE: Int = 999
    }
}
