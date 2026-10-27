package com.myowndime.duoscoreboard.ui.scoreboard

import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.myowndime.duoscoreboard.domain.model.AppSettings
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.ui.theme.DuoScoreboardTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment

@RunWith(AndroidJUnit4::class)
class ScoreboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var oneScore = 0
    private var twoScore = 0
    private var setsOne = 0
    private var setsTwo = 0
    private var matchFormatSets = AppSettings.MATCH_FORMAT_MANUAL
    private var resetCount = 0
    private var swapCount = 0
    private var lastSetScores: Pair<Int, Int>? = null
    private var swapped = false
    private var padScore = false
    private var finishSetCount = 0
    private var finishCount = 0
    private var settingsCount = 0
    private var sharedText: String? = null

    private fun setContent() {
        composeRule.setContent {
            DuoScoreboardTheme {
                ScoreboardScreen(
                    state = ScoreboardUiState(
                        settings = AppSettings(matchFormatSets = matchFormatSets, padScoreDigits = padScore),
                        playerOneScore = oneScore,
                        playerTwoScore = twoScore,
                        playerOneSets = setsOne,
                        playerTwoSets = setsTwo,
                        sidesSwapped = swapped,
                    ),
                    playerOneName = "Ann",
                    playerTwoName = "Ben",
                    onIncrement = { player ->
                        if (player == Player.ONE) oneScore++ else twoScore++
                    },
                    onDecrement = { player ->
                        if (player == Player.ONE) {
                            oneScore = (oneScore - 1).coerceAtLeast(0)
                        } else {
                            twoScore = (twoScore - 1).coerceAtLeast(0)
                        }
                    },
                    onResetConfirmed = { resetCount++ },
                    onSwapSides = { swapCount++ },
                    onSetScores = { one, two -> lastSetScores = one to two },
                    onFinishSetConfirmed = { finishSetCount++ },
                    onFinishConfirmed = { finishCount++ },
                    onOpenHistory = {},
                    onOpenSettings = { settingsCount++ },
                    onShare = { sharedText = it },
                )
            }
        }
    }

    @Test
    fun bothPlayerPanelsAreShown() {
        setContent()

        composeRule.onNodeWithText("Ann").assertIsDisplayed()
        composeRule.onNodeWithText("Ben").assertIsDisplayed()
        composeRule.onNodeWithTag(ScoreboardTestTags.score(Player.ONE)).assertIsDisplayed()
        composeRule.onNodeWithTag(ScoreboardTestTags.score(Player.TWO)).assertIsDisplayed()
    }

    @Test
    fun bothPlayerPanelsAreShownInLandscape() {
        RuntimeEnvironment.setQualifiers("+w800dp-h400dp-land")

        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.panel(Player.ONE)).assertIsDisplayed()
        composeRule.onNodeWithTag(ScoreboardTestTags.panel(Player.TWO)).assertIsDisplayed()
        composeRule.onNodeWithTag(ScoreboardTestTags.score(Player.ONE)).assertIsDisplayed()
        composeRule.onNodeWithTag(ScoreboardTestTags.score(Player.TWO)).assertIsDisplayed()
    }

    @Test
    fun bothPlayerPanelsAreShownInDarkTheme() {
        RuntimeEnvironment.setQualifiers("+night")

        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.panel(Player.ONE)).assertIsDisplayed()
        composeRule.onNodeWithTag(ScoreboardTestTags.panel(Player.TWO)).assertIsDisplayed()
    }

    @Test
    fun tappingAPanelAddsAPoint() {
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.panel(Player.ONE)).performClick()
        composeRule.waitForIdle()

        assertEquals(1, oneScore)
        assertEquals(0, twoScore)
    }

    @Test
    fun swipingDownRemovesAPoint() {
        oneScore = 3
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.panel(Player.ONE))
            .performTouchInput { swipeDown() }
        composeRule.waitForIdle()

        assertEquals(2, oneScore)
    }

    @Test
    fun minusButtonRemovesAPoint() {
        twoScore = 2
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.decrement(Player.TWO)).performClick()
        composeRule.waitForIdle()

        assertEquals(1, twoScore)
    }

    @Test
    fun resetAsksForConfirmationFirst() {
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.RESET_ACTION).performClick()
        composeRule.waitForIdle()
        assertEquals(0, resetCount)

        composeRule.onNodeWithText("Confirm").performClick()
        composeRule.waitForIdle()
        assertEquals(1, resetCount)
    }

    @Test
    fun cancellingTheResetDialogChangesNothing() {
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.RESET_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.waitForIdle()

        assertEquals(0, resetCount)
    }

    @Test
    fun finishingASetAsksForConfirmationFirst() {
        oneScore = 11
        twoScore = 8
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.FINISH_SET_ACTION).performClick()
        composeRule.waitForIdle()
        assertEquals(0, finishSetCount)

        composeRule.onNodeWithText("Confirm").performClick()
        composeRule.waitForIdle()
        assertEquals(1, finishSetCount)
    }

    @Test
    fun setCountersAreHiddenUntilSetsAreUsed() {
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.sets(Player.ONE)).assertDoesNotExist()
    }

    @Test
    fun setCountersAppearOnceASetIsWon() {
        setsOne = 2
        setsTwo = 1
        setContent()

        // The set status bar merges its descendants into one accessibility node, so
        // the per-side nodes are only reachable through the unmerged tree.
        composeRule.onNodeWithTag(ScoreboardTestTags.sets(Player.ONE), useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag(ScoreboardTestTags.sets(Player.TWO), useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithText("Sets 2").assertIsDisplayed()
        composeRule.onNodeWithText("Sets 1").assertIsDisplayed()
    }

    private fun openOverflowAndClick(tag: String) {
        composeRule.onNodeWithTag(ScoreboardTestTags.OVERFLOW_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(tag).performClick()
        composeRule.waitForIdle()
    }

    @Test
    fun sharingSendsTheCurrentScoreAsText() {
        oneScore = 7
        twoScore = 4
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.OVERFLOW_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(ScoreboardTestTags.SHARE_ACTION).performClick()
        composeRule.waitForIdle()

        assertEquals("Ann: 7\nBen: 4", sharedText)
    }

    @Test
    fun settingsIsReachableFromTheOverflowMenu() {
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.OVERFLOW_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(ScoreboardTestTags.SETTINGS_ACTION).performClick()
        composeRule.waitForIdle()

        assertEquals(1, settingsCount)
    }

    @Test
    fun winningTheMatchOpensTheFinishDialogOnItsOwn() {
        matchFormatSets = 3
        setsOne = 2
        setsTwo = 0
        setContent()

        composeRule.onNodeWithText("Ann won the match! Save it?").assertIsDisplayed()
        composeRule.onNodeWithText("Confirm").performClick()
        composeRule.waitForIdle()

        assertEquals(1, finishCount)
    }

    @Test
    fun notYetWinningTheMatchDoesNotOpenTheFinishDialog() {
        matchFormatSets = 3
        setsOne = 1
        setsTwo = 0
        setContent()

        composeRule.onNodeWithText("Confirm").assertDoesNotExist()
    }

    @Test
    fun swapButtonSwapsTheSides() {
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.SWAP_ACTION).performClick()
        composeRule.waitForIdle()

        assertEquals(1, swapCount)
    }

    @Test
    fun swappedStateDrawsPlayerTwoBeforePlayerOne() {
        swapped = true
        setContent()

        assertTrue(position(Player.TWO) < position(Player.ONE))
    }

    @Test
    fun unswappedStateDrawsPlayerOneFirst() {
        setContent()

        assertTrue(position(Player.ONE) < position(Player.TWO))
    }

    /** Distance along the reading direction - works in portrait (y) and landscape (x). */
    private fun position(player: Player): Float {
        val offset = composeRule.onNodeWithTag(ScoreboardTestTags.panel(player))
            .fetchSemanticsNode().positionInRoot
        return offset.x + offset.y
    }

    @Test
    fun scoresAreShownAsPlainNumbersByDefault() {
        oneScore = 7
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.score(Player.ONE)).assertTextEquals("7")
    }

    @Test
    fun paddedScoresAreShownWithThreeDigits() {
        padScore = true
        oneScore = 7
        twoScore = 0
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.score(Player.ONE)).assertTextEquals("007")
        composeRule.onNodeWithTag(ScoreboardTestTags.score(Player.TWO)).assertTextEquals("000")
    }

    @Test
    fun theLargestScoreFitsOnTheBoard() {
        oneScore = 999
        padScore = true
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.score(Player.ONE)).assertIsDisplayed()
    }

    @Test
    fun setScoreDialogStartsWithTheCurrentScores() {
        oneScore = 12
        twoScore = 5
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.SET_SCORE_ACTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(SetScoreTestTags.FIRST_FIELD)
            .assert(hasText("12"))
        composeRule.onNodeWithTag(SetScoreTestTags.SECOND_FIELD)
            .assert(hasText("5"))
    }

    @Test
    fun confirmingTheSetScoreDialogReportsTheTypedScores() {
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.SET_SCORE_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SetScoreTestTags.FIRST_FIELD).performTextReplacement("150")
        composeRule.onNodeWithTag(SetScoreTestTags.SECOND_FIELD).performTextReplacement("42")
        composeRule.onNodeWithTag(SetScoreTestTags.CONFIRM).performClick()
        composeRule.waitForIdle()

        assertEquals(150 to 42, lastSetScores)
    }

    @Test
    fun setScoreDialogMapsFieldsBackToThePlayersWhenSwapped() {
        swapped = true
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.SET_SCORE_ACTION).performClick()
        composeRule.waitForIdle()
        // Screen order is Ben (player two) first, so the first field belongs to Ben.
        composeRule.onNodeWithTag(SetScoreTestTags.FIRST_FIELD).performTextReplacement("9")
        composeRule.onNodeWithTag(SetScoreTestTags.SECOND_FIELD).performTextReplacement("3")
        composeRule.onNodeWithTag(SetScoreTestTags.CONFIRM).performClick()
        composeRule.waitForIdle()

        assertEquals(3 to 9, lastSetScores)
    }

    @Test
    fun setScoreDialogCapsEntriesAtThreeDigits() {
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.SET_SCORE_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SetScoreTestTags.FIRST_FIELD).performTextReplacement("12345")

        composeRule.onNodeWithTag(SetScoreTestTags.FIRST_FIELD)
            .assert(hasText("123"))
    }

    @Test
    fun setScoreDialogRefusesAnEmptyField() {
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.SET_SCORE_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SetScoreTestTags.FIRST_FIELD).performTextReplacement("")

        composeRule.onNodeWithTag(SetScoreTestTags.CONFIRM).assertIsNotEnabled()
    }

    @Test
    fun cancellingTheSetScoreDialogChangesNothing() {
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.SET_SCORE_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.waitForIdle()

        assertEquals(null, lastSetScores)
    }

    @Test
    fun finishingAMatchAsksForConfirmationFirst() {
        oneScore = 5
        setContent()

        composeRule.onNodeWithTag(ScoreboardTestTags.FINISH_ACTION).performClick()
        composeRule.waitForIdle()
        assertEquals(0, finishCount)

        composeRule.onNodeWithText("Confirm").performClick()
        composeRule.waitForIdle()
        assertEquals(1, finishCount)
    }
}
