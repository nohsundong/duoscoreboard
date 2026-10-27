package com.myowndime.duoscoreboard.ui.settings

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.myowndime.duoscoreboard.domain.model.AppSettings
import com.myowndime.duoscoreboard.domain.model.ThemeMode
import com.myowndime.duoscoreboard.ui.theme.DuoScoreboardTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private var settings = AppSettings()
    private var lastMatchFormat: Int? = null
    private var lastSetTarget: Int? = null
    private var lastDeuce: Boolean? = null
    private var lastTheme: ThemeMode? = null
    private var lastPad: Boolean? = null

    private fun setContent() {
        composeRule.setContent {
            DuoScoreboardTheme {
                SettingsScreen(
                    settings = settings,
                    onNameChange = { _, _ -> },
                    onColorChange = { _, _ -> },
                    onThemeChange = { lastTheme = it },
                    onKeepScreenOnChange = {},
                    onSpeakScoresChange = {},
                    onSetTargetChange = { lastSetTarget = it },
                    onDeuceChange = { lastDeuce = it },
                    onMatchFormatChange = { lastMatchFormat = it },
                    onPadScoreDigitsChange = { lastPad = it },
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun choosingABestOfFormatReportsIt() {
        setContent()

        composeRule.onNodeWithTag(SettingsTestTags.matchFormat(5)).performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals(5, lastMatchFormat)
    }

    @Test
    fun choosingASetTargetReportsIt() {
        setContent()

        composeRule.onNodeWithTag(SettingsTestTags.setTarget(21)).performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals(21, lastSetTarget)
    }

    @Test
    fun toggleDeuceReportsTheNewValue() {
        settings = AppSettings(setTargetScore = 11)
        setContent()

        composeRule.onNodeWithTag(SettingsTestTags.DEUCE_ENABLED).performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals(false, lastDeuce)
    }

    @Test
    fun togglingThreeDigitScoresReportsTheNewValue() {
        setContent()

        composeRule.onNodeWithTag(SettingsTestTags.PAD_SCORE_DIGITS).performScrollTo().performClick()
        composeRule.waitForIdle()

        assertEquals(true, lastPad)
    }

    @Test
    fun themeSelectionStillWorksAlongsideTheNewControls() {
        setContent()

        composeRule.onNodeWithTag(SettingsTestTags.themeOption(ThemeMode.DARK))
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()

        assertEquals(ThemeMode.DARK, lastTheme)
    }
}
