package com.myowndime.duoscoreboard.ui.history

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.myowndime.duoscoreboard.domain.model.MatchRecord
import com.myowndime.duoscoreboard.ui.theme.DuoScoreboardTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var sharedText: String? = null
    private var deletedId: Long? = null
    private var deleteAllCount = 0

    private val match = MatchRecord(
        id = 1L,
        playerOneName = "Ann",
        playerTwoName = "Ben",
        playerOneScore = 21,
        playerTwoScore = 18,
        playedAtEpochMillis = 1_700_000_000_000,
    )

    private fun setContent(matches: List<MatchRecord> = listOf(match)) {
        composeRule.setContent {
            DuoScoreboardTheme {
                HistoryScreen(
                    matches = matches,
                    onBack = {},
                    onDelete = { deletedId = it },
                    onDeleteAll = { deleteAllCount++ },
                    onShare = { sharedText = it },
                )
            }
        }
    }

    @Test
    fun emptyStateIsShownWithoutMatches() {
        setContent(matches = emptyList())

        composeRule.onNodeWithTag(HistoryTestTags.EMPTY).assertIsDisplayed()
    }

    @Test
    fun matchRowShowsWinnerAndScore() {
        setContent()

        composeRule.onNodeWithText("Winner: Ann").assertIsDisplayed()
        composeRule.onNodeWithText("Ann 21 : 18 Ben").assertIsDisplayed()
    }

    @Test
    fun sharingAMatchSendsScoreAndTimestamp() {
        setContent()

        composeRule.onAllNodesWithTag(HistoryTestTags.SHARE)[0].performClick()
        composeRule.waitForIdle()

        val lines = sharedText.orEmpty().lines()
        assertEquals("Ann: 21", lines[0])
        assertEquals("Ben: 18", lines[1])
        assertEquals(3, lines.size)
    }

    @Test
    fun deletingASingleMatchAsksForConfirmationFirst() {
        setContent()

        composeRule.onNodeWithContentDescription("Delete match").performClick()
        composeRule.waitForIdle()
        assertNull(deletedId)

        composeRule.onNodeWithText("Confirm").performClick()
        composeRule.waitForIdle()
        assertEquals(1L, deletedId)
    }

    @Test
    fun deletingEverythingAsksForConfirmationFirst() {
        setContent()

        composeRule.onNodeWithTag(HistoryTestTags.DELETE_ALL).performClick()
        composeRule.waitForIdle()
        assertEquals(0, deleteAllCount)

        composeRule.onNodeWithText("Confirm").performClick()
        composeRule.waitForIdle()
        assertEquals(1, deleteAllCount)
    }
}
