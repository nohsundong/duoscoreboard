package com.myowndime.duoscoreboard

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.ui.history.HistoryTestTags
import com.myowndime.duoscoreboard.ui.scoreboard.ScoreboardTestTags
import com.myowndime.duoscoreboard.ui.settings.SettingsTestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Starts the real activity with the real Room database and DataStore, so a crash in
 * dependency wiring, theming or navigation fails the build.
 */
@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun scoreboardStartsWithBothPanels() {
        composeRule.onNodeWithTag(ScoreboardTestTags.panel(Player.ONE)).assertIsDisplayed()
        composeRule.onNodeWithTag(ScoreboardTestTags.panel(Player.TWO)).assertIsDisplayed()
    }

    @Test
    fun scoringAPointUpdatesTheBoard() {
        composeRule.onNodeWithTag(ScoreboardTestTags.increment(Player.ONE)).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(ScoreboardTestTags.score(Player.ONE)).assertIsDisplayed()
    }

    @Test
    fun historyOpensAndStartsEmpty() {
        composeRule.onNodeWithTag(ScoreboardTestTags.HISTORY_ACTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(HistoryTestTags.EMPTY).assertIsDisplayed()
    }

    @Test
    fun settingsOpensWithBothNameFields() {
        composeRule.onNodeWithTag(ScoreboardTestTags.OVERFLOW_ACTION).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(ScoreboardTestTags.SETTINGS_ACTION).performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag(SettingsTestTags.nameField(Player.ONE)).assertIsDisplayed()
        composeRule.onNodeWithTag(SettingsTestTags.nameField(Player.TWO)).assertIsDisplayed()
    }
}
