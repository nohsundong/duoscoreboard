package com.myowndime.duoscoreboard.ui.scoreboard

import com.myowndime.duoscoreboard.domain.model.MatchResult
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.testing.FakeMatchRepository
import com.myowndime.duoscoreboard.testing.FakeSettingsRepository
import com.myowndime.duoscoreboard.testing.MainDispatcherRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ScoreboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val settingsRepository = FakeSettingsRepository()
    private val matchRepository = FakeMatchRepository()

    private fun viewModel(now: () -> Long = { 1_700_000_000_000 }) = ScoreboardViewModel(
        settingsRepository = settingsRepository,
        matchRepository = matchRepository,
        now = now,
    )

    @Test
    fun `increment raises only the tapped player's score`() {
        val viewModel = viewModel()

        viewModel.increment(Player.ONE)
        viewModel.increment(Player.ONE)
        viewModel.increment(Player.TWO)

        assertEquals(2, viewModel.uiState.value.playerOneScore)
        assertEquals(1, viewModel.uiState.value.playerTwoScore)
    }

    @Test
    fun `decrement never goes below zero`() {
        val viewModel = viewModel()

        viewModel.decrement(Player.ONE)
        viewModel.decrement(Player.ONE)

        assertEquals(ScoreboardViewModel.MIN_SCORE, viewModel.uiState.value.playerOneScore)
    }

    @Test
    fun `score is capped at the maximum`() {
        val viewModel = viewModel()

        repeat(ScoreboardViewModel.MAX_SCORE + 5) { viewModel.increment(Player.TWO) }

        assertEquals(ScoreboardViewModel.MAX_SCORE, viewModel.uiState.value.playerTwoScore)
    }

    @Test
    fun `reset clears both scores but keeps history`() = runTest {
        val viewModel = viewModel()
        viewModel.increment(Player.ONE)
        viewModel.increment(Player.TWO)
        viewModel.finishMatch("Ann", "Ben")

        viewModel.increment(Player.ONE)
        viewModel.reset()

        assertEquals(0, viewModel.uiState.value.playerOneScore)
        assertEquals(0, viewModel.uiState.value.playerTwoScore)
        assertEquals(1, matchRepository.current.size)
    }

    @Test
    fun `finish match stores scores, names, timestamp and clears the board`() = runTest {
        val viewModel = viewModel(now = { 42L })
        repeat(3) { viewModel.increment(Player.ONE) }
        viewModel.increment(Player.TWO)

        viewModel.finishMatch("Ann", "Ben")

        val stored = matchRepository.current.single()
        assertEquals("Ann", stored.playerOneName)
        assertEquals("Ben", stored.playerTwoName)
        assertEquals(3, stored.playerOneScore)
        assertEquals(1, stored.playerTwoScore)
        assertEquals(42L, stored.playedAtEpochMillis)
        assertEquals(MatchResult.PLAYER_ONE, stored.result)
        assertEquals(0, viewModel.uiState.value.playerOneScore)
        assertEquals(0, viewModel.uiState.value.playerTwoScore)
    }

    @Test
    fun `finish match emits a saved message`() = runTest {
        val viewModel = viewModel()
        viewModel.increment(Player.ONE)

        viewModel.finishMatch("Ann", "Ben")

        assertEquals(ScoreboardMessage.MATCH_SAVED, viewModel.messages.first())
    }

    @Test
    fun `a goalless board is not saved`() = runTest {
        val viewModel = viewModel()

        viewModel.finishMatch("Ann", "Ben")

        assertTrue(matchRepository.current.isEmpty())
        assertEquals(ScoreboardMessage.MATCH_EMPTY, viewModel.messages.first())
    }

    @Test
    fun `every score change is announced with the new value`() = runTest {
        val viewModel = viewModel()

        viewModel.increment(Player.TWO)

        val announcement = viewModel.announcements.first()
        assertEquals(Player.TWO, announcement.player)
        assertEquals(1, announcement.score)
    }

    @Test
    fun `announcement reports the clamped value, not the raw one`() = runTest {
        val viewModel = viewModel()

        viewModel.decrement(Player.ONE)

        val announcement = viewModel.announcements.first()
        assertEquals(Player.ONE, announcement.player)
        assertEquals(ScoreboardViewModel.MIN_SCORE, announcement.score)
    }

    @Test
    fun `finishing a set gives it to the leader and restarts the points`() {
        val viewModel = viewModel()
        repeat(11) { viewModel.increment(Player.ONE) }
        repeat(4) { viewModel.increment(Player.TWO) }

        viewModel.finishSet()

        val state = viewModel.uiState.value
        assertEquals(1, state.playerOneSets)
        assertEquals(0, state.playerTwoSets)
        assertEquals(0, state.playerOneScore)
        assertEquals(0, state.playerTwoScore)
    }

    @Test
    fun `a tied set cannot be finished`() = runTest {
        val viewModel = viewModel()
        viewModel.increment(Player.ONE)
        viewModel.increment(Player.TWO)

        viewModel.finishSet()

        val state = viewModel.uiState.value
        assertEquals(0, state.playerOneSets)
        assertEquals(0, state.playerTwoSets)
        assertEquals(1, state.playerOneScore)
        assertEquals(ScoreboardMessage.SET_TIED, viewModel.messages.first())
    }

    @Test
    fun `a target score ends the set automatically once two points ahead`() = runTest {
        settingsRepository.setSetTargetScore(11)
        val viewModel = viewModel()
        repeat(9) { viewModel.increment(Player.TWO) }

        repeat(2) { viewModel.increment(Player.TWO) }

        val state = viewModel.uiState.value
        assertEquals(1, state.playerTwoSets)
        assertEquals(0, state.playerTwoScore)
    }

    @Test
    fun `reaching the target without a two point lead keeps the set running`() = runTest {
        settingsRepository.setSetTargetScore(11)
        val viewModel = viewModel()
        repeat(10) { viewModel.increment(Player.ONE) }
        repeat(10) { viewModel.increment(Player.TWO) }

        viewModel.increment(Player.ONE)

        val state = viewModel.uiState.value
        assertEquals(0, state.playerOneSets)
        assertEquals(11, state.playerOneScore)
        assertEquals(10, state.playerTwoScore)
    }

    @Test
    fun `with deuce off, reaching the target wins outright`() = runTest {
        settingsRepository.setSetTargetScore(11)
        settingsRepository.setDeuceEnabled(false)
        val viewModel = viewModel()
        repeat(10) { viewModel.increment(Player.ONE) }
        repeat(10) { viewModel.increment(Player.TWO) }

        viewModel.increment(Player.ONE)

        val state = viewModel.uiState.value
        assertEquals(1, state.playerOneSets)
        assertEquals(0, state.playerOneScore)
        assertEquals(0, state.playerTwoScore)
    }

    @Test
    fun `with deuce off the set still waits for the target itself`() = runTest {
        settingsRepository.setSetTargetScore(11)
        settingsRepository.setDeuceEnabled(false)
        val viewModel = viewModel()
        repeat(9) { viewModel.increment(Player.ONE) }
        repeat(3) { viewModel.increment(Player.TWO) }

        val state = viewModel.uiState.value
        assertEquals(0, state.playerOneSets)
        assertEquals(9, state.playerOneScore)
        assertEquals(3, state.playerTwoScore)
    }

    @Test
    fun `match format manual never reports a match winner`() = runTest {
        val viewModel = viewModel()
        repeat(5) { viewModel.finishSet(); viewModel.increment(Player.ONE) }

        assertEquals(null, viewModel.uiState.value.matchWinner)
    }

    @Test
    fun `best-of-3 declares a winner once a player takes two sets`() = runTest {
        settingsRepository.setMatchFormatSets(3)
        val viewModel = viewModel()

        viewModel.increment(Player.TWO)
        viewModel.finishSet()
        assertEquals(null, viewModel.uiState.value.matchWinner)

        viewModel.increment(Player.TWO)
        viewModel.finishSet()

        assertEquals(Player.TWO, viewModel.uiState.value.matchWinner)
    }

    @Test
    fun `correcting a score down never awards a set`() = runTest {
        settingsRepository.setSetTargetScore(11)
        val viewModel = viewModel()
        repeat(13) { viewModel.increment(Player.ONE) }
        // 11:0 already took the first set, the next points start set two.
        repeat(3) { viewModel.increment(Player.TWO) }

        viewModel.decrement(Player.TWO)

        val state = viewModel.uiState.value
        assertEquals(1, state.playerOneSets)
        assertEquals(0, state.playerTwoSets)
        assertEquals(2, state.playerOneScore)
        assertEquals(2, state.playerTwoScore)
    }

    @Test
    fun `reset clears sets as well as points`() {
        val viewModel = viewModel()
        viewModel.increment(Player.ONE)
        viewModel.finishSet()
        viewModel.increment(Player.TWO)

        viewModel.reset()

        val state = viewModel.uiState.value
        assertEquals(0, state.playerOneSets)
        assertEquals(0, state.playerTwoSets)
        assertEquals(0, state.playerOneScore)
        assertEquals(0, state.playerTwoScore)
    }

    @Test
    fun `finished match stores the set counts and the winner follows them`() = runTest {
        val viewModel = viewModel()
        viewModel.increment(Player.TWO)
        viewModel.finishSet()
        repeat(3) { viewModel.increment(Player.ONE) }

        viewModel.finishMatch("Ann", "Ben")

        val stored = matchRepository.current.single()
        assertEquals(0, stored.playerOneSets)
        assertEquals(1, stored.playerTwoSets)
        assertEquals(3, stored.playerOneScore)
        assertEquals(MatchResult.PLAYER_TWO, stored.result)
        assertEquals("Ben", stored.winnerName)
    }

    @Test
    fun `a match with only sets on the board is still saved`() = runTest {
        val viewModel = viewModel()
        viewModel.increment(Player.ONE)
        viewModel.finishSet()

        viewModel.finishMatch("Ann", "Ben")

        assertEquals(1, matchRepository.current.size)
        assertEquals(ScoreboardMessage.MATCH_SAVED, viewModel.messages.first())
    }

    @Test
    fun `swapping sides changes who is drawn first but not the scores`() {
        val viewModel = viewModel()
        repeat(3) { viewModel.increment(Player.ONE) }
        viewModel.increment(Player.TWO)

        viewModel.swapSides()

        val state = viewModel.uiState.value
        assertTrue(state.sidesSwapped)
        assertEquals(Player.TWO, state.firstPlayer)
        assertEquals(Player.ONE, state.secondPlayer)
        assertEquals(3, state.playerOneScore)
        assertEquals(1, state.playerTwoScore)
    }

    @Test
    fun `swapping twice puts everyone back`() {
        val viewModel = viewModel()

        viewModel.swapSides()
        viewModel.swapSides()

        assertEquals(Player.ONE, viewModel.uiState.value.firstPlayer)
    }

    @Test
    fun `reset puts the sides back as well`() {
        val viewModel = viewModel()
        viewModel.swapSides()

        viewModel.reset()

        assertEquals(false, viewModel.uiState.value.sidesSwapped)
    }

    @Test
    fun `saving a match puts the sides back for the next one`() = runTest {
        val viewModel = viewModel()
        viewModel.increment(Player.ONE)
        viewModel.swapSides()

        viewModel.finishMatch("Ann", "Ben")

        assertEquals(false, viewModel.uiState.value.sidesSwapped)
    }

    @Test
    fun `scores can be set directly`() {
        val viewModel = viewModel()

        viewModel.setScores(playerOneScore = 120, playerTwoScore = 7)

        assertEquals(120, viewModel.uiState.value.playerOneScore)
        assertEquals(7, viewModel.uiState.value.playerTwoScore)
    }

    @Test
    fun `directly set scores are clamped to 0 through 999`() {
        val viewModel = viewModel()

        viewModel.setScores(playerOneScore = 5_000, playerTwoScore = -4)

        assertEquals(ScoreboardViewModel.MAX_SCORE, viewModel.uiState.value.playerOneScore)
        assertEquals(ScoreboardViewModel.MIN_SCORE, viewModel.uiState.value.playerTwoScore)
    }

    @Test
    fun `setting scores never awards a set or touches existing sets`() = runTest {
        settingsRepository.setSetTargetScore(11)
        val viewModel = viewModel()
        repeat(11) { viewModel.increment(Player.ONE) }

        viewModel.setScores(playerOneScore = 50, playerTwoScore = 0)

        val state = viewModel.uiState.value
        assertEquals(1, state.playerOneSets)
        assertEquals(50, state.playerOneScore)
    }

    @Test
    fun `a score set directly keeps counting from there`() {
        val viewModel = viewModel()
        viewModel.setScores(playerOneScore = 998, playerTwoScore = 0)

        viewModel.increment(Player.ONE)
        viewModel.increment(Player.ONE)

        assertEquals(ScoreboardViewModel.MAX_SCORE, viewModel.uiState.value.playerOneScore)
    }

    @Test
    fun `sets are shown once a target is configured`() = runTest {
        settingsRepository.setSetTargetScore(21)
        val viewModel = viewModel()

        assertTrue(viewModel.uiState.value.showSets)
    }

    @Test
    fun `sets are shown once a match format is chosen, even at 0-0`() = runTest {
        settingsRepository.setMatchFormatSets(3)
        val viewModel = viewModel()

        assertTrue(viewModel.uiState.value.showSets)
    }

    @Test
    fun `resetting the board keeps sets visible when a match format is chosen`() = runTest {
        settingsRepository.setMatchFormatSets(3)
        val viewModel = viewModel()
        viewModel.increment(Player.ONE)
        viewModel.finishSet()

        viewModel.reset()

        assertTrue(viewModel.uiState.value.showSets)
    }

    @Test
    fun `ui state exposes the stored player customization`() = runTest {
        settingsRepository.setPlayerName(Player.ONE, "  Ann  ")
        settingsRepository.setPlayerColor(Player.TWO, 0xFF2E7D32)
        val viewModel = viewModel()

        assertEquals("Ann", viewModel.uiState.value.settings.playerOne.name)
        assertEquals(0xFF2E7D32, viewModel.uiState.value.settings.playerTwo.colorArgb)
        assertEquals(0, viewModel.uiState.value.scoreOf(Player.ONE))
    }
}
