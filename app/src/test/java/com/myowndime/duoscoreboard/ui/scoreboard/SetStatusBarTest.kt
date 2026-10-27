package com.myowndime.duoscoreboard.ui.scoreboard

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.myowndime.duoscoreboard.domain.model.AppSettings
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.ui.theme.DuoScoreboardTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SetStatusBarTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun setContent(
        playerOneSets: Int = 0,
        playerTwoSets: Int = 0,
        matchFormatSets: Int = AppSettings.MATCH_FORMAT_MANUAL,
    ) {
        composeRule.setContent {
            DuoScoreboardTheme {
                SetStatusBar(
                    playerOneName = "Ann",
                    playerOneSets = playerOneSets,
                    playerOneColor = Color(0xFF1565C0),
                    playerTwoName = "Ben",
                    playerTwoSets = playerTwoSets,
                    playerTwoColor = Color(0xFFC62828),
                    matchFormatSets = matchFormatSets,
                )
            }
        }
    }

    @Test
    fun manualFormatShowsPlainSetCounts() {
        setContent(playerOneSets = 2, playerTwoSets = 1)

        composeRule.onNodeWithText("Sets 2").assertIsDisplayed()
        composeRule.onNodeWithText("Sets 1").assertIsDisplayed()
        composeRule.onNodeWithText("Set score").assertIsDisplayed()
    }

    @Test
    fun bestOfFormatShowsTheFormatLabelInsteadOfPlainCounts() {
        setContent(playerOneSets = 2, playerTwoSets = 1, matchFormatSets = 5)

        composeRule.onNodeWithText("Best of 5 (first to 3)").assertIsDisplayed()
        composeRule.onNodeWithText("Sets 2").assertDoesNotExist()
    }

    @Test
    fun singleSetFormatShowsTheSingleSetLabel() {
        setContent(matchFormatSets = 1)

        composeRule.onNodeWithText("Single set").assertIsDisplayed()
    }

    @Test
    fun barExposesACombinedDescriptionForScreenReaders() {
        setContent(playerOneSets = 2, playerTwoSets = 1, matchFormatSets = 5)

        composeRule.onNodeWithTag(ScoreboardTestTags.SET_STATUS_BAR)
            .assert(
                SemanticsMatcher("has the expected combined description") { node ->
                    val description = node.config.getOrNull(SemanticsProperties.ContentDescription)
                        ?.joinToString(" ")
                    description == "Set score: Ann 2, Ben 1. Best of 5 (first to 3)."
                },
            )
    }

    @Test
    fun eachSideCarriesTheSetsTestTag() {
        setContent(playerOneSets = 1, playerTwoSets = 0)

        // The bar merges its descendants into one accessibility node (so a screen
        // reader announces both scores as one sentence), which makes the per-side
        // nodes only reachable through the unmerged tree.
        composeRule.onNodeWithTag(ScoreboardTestTags.sets(Player.ONE), useUnmergedTree = true)
            .assertIsDisplayed()
        composeRule.onNodeWithTag(ScoreboardTestTags.sets(Player.TWO), useUnmergedTree = true)
            .assertIsDisplayed()
    }
}
