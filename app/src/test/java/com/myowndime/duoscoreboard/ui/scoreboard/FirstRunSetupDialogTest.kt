package com.myowndime.duoscoreboard.ui.scoreboard

import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.myowndime.duoscoreboard.domain.model.AppSettings
import com.myowndime.duoscoreboard.ui.settings.SettingsTestTags
import com.myowndime.duoscoreboard.ui.theme.DuoScoreboardTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FirstRunSetupDialogTest {

    @get:Rule
    val composeRule = createComposeRule()

    /**
     * Fast-forwards past the confirm button's launch-touch ignore window so tests
     * can tap it, without the tests needing to know the exact delay value.
     */
    private fun skipConfirmDebounce() {
        composeRule.mainClock.autoAdvance = false
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
    }

    @Test
    fun confirmButtonIgnoresTouchesRightAfterTheDialogAppears() {
        var confirmed = false
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            DuoScoreboardTheme {
                FirstRunSetupDialog(onConfirm = { _, _, _ -> confirmed = true })
            }
        }

        // A stray touch-up from the launcher-icon tap that opened the app can land
        // here on the very first frame; it must not be treated as a real confirm.
        composeRule.onNodeWithTag(FirstRunSetupTestTags.CONFIRM).assertIsNotEnabled()
        composeRule.onNodeWithTag(FirstRunSetupTestTags.CONFIRM).performClick()
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()

        assertEquals(false, confirmed)
    }

    @Test
    fun confirmButtonBecomesEnabledAfterTheIgnoreWindow() {
        composeRule.setContent {
            DuoScoreboardTheme {
                FirstRunSetupDialog(onConfirm = { _, _, _ -> })
            }
        }

        skipConfirmDebounce()

        composeRule.onNodeWithTag(FirstRunSetupTestTags.CONFIRM).assertIsEnabled()
    }

    @Test
    fun defaultsToManualRulesWhenConfirmedWithoutChanges() {
        var result: Triple<Int, Int, Boolean>? = null
        composeRule.setContent {
            DuoScoreboardTheme {
                FirstRunSetupDialog(
                    onConfirm = { matchFormatSets, setTargetScore, deuceEnabled ->
                        result = Triple(matchFormatSets, setTargetScore, deuceEnabled)
                    },
                )
            }
        }

        skipConfirmDebounce()
        composeRule.onNodeWithTag(FirstRunSetupTestTags.CONFIRM).performClick()
        composeRule.waitForIdle()

        assertEquals(
            Triple(AppSettings.MATCH_FORMAT_MANUAL, AppSettings.SET_TARGET_MANUAL, true),
            result,
        )
    }

    @Test
    fun choosingBestOfThreeAndATargetIsPassedToConfirm() {
        var result: Triple<Int, Int, Boolean>? = null
        composeRule.setContent {
            DuoScoreboardTheme {
                FirstRunSetupDialog(
                    onConfirm = { matchFormatSets, setTargetScore, deuceEnabled ->
                        result = Triple(matchFormatSets, setTargetScore, deuceEnabled)
                    },
                )
            }
        }

        composeRule.onNodeWithTag(SettingsTestTags.matchFormat(3)).performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SettingsTestTags.setTarget(11)).performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(SettingsTestTags.DEUCE_ENABLED).performScrollTo().performClick()
        composeRule.waitForIdle()
        skipConfirmDebounce()
        composeRule.onNodeWithTag(FirstRunSetupTestTags.CONFIRM).performClick()
        composeRule.waitForIdle()

        assertEquals(Triple(3, 11, false), result)
    }

    @Test
    fun cannotBeDismissedByTappingOutside() {
        var confirmed = false
        composeRule.setContent {
            DuoScoreboardTheme {
                FirstRunSetupDialog(onConfirm = { _, _, _ -> confirmed = true })
            }
        }

        composeRule.onNodeWithText("Start scoring").assertExists()
        assertEquals(false, confirmed)
    }
}
