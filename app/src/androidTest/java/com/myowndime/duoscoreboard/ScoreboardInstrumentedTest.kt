package com.myowndime.duoscoreboard

import android.content.pm.PackageManager
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.myowndime.duoscoreboard.domain.model.Player
import com.myowndime.duoscoreboard.ui.scoreboard.ScoreboardTestTags
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Runs on a real device or emulator: `./gradlew connectedDebugAndroidTest`.
 */
@RunWith(AndroidJUnit4::class)
class ScoreboardInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun scoreboardShowsBothPanels() {
        composeRule.onNodeWithTag(ScoreboardTestTags.panel(Player.ONE)).assertIsDisplayed()
        composeRule.onNodeWithTag(ScoreboardTestTags.panel(Player.TWO)).assertIsDisplayed()
    }

    @Test
    fun plusButtonIsClickable() {
        composeRule.onNodeWithTag(ScoreboardTestTags.increment(Player.ONE)).performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag(ScoreboardTestTags.score(Player.ONE)).assertIsDisplayed()
    }

    @Test
    fun installedPackageRequestsNoPermissions() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS,
        )
        assertEquals(emptyList<String>(), info.requestedPermissions?.toList().orEmpty())
    }
}
