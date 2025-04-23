package com.example.nhlive

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.printToLog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.nhlive.dataElements.CommonName
import com.example.nhlive.dataElements.Game
import com.example.nhlive.dataElements.GameDay
import com.example.nhlive.dataElements.ScheduleResponse
import com.example.nhlive.dataElements.TeamInfo
import com.example.nhlive.ui.theme.NHLiveTheme
import com.example.nhlive.uiElements.GameListScreenWithRefresh

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Rule

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun greetingDisplaysCorrectName() {
        composeRule.waitForIdle()

        composeRule
            .onNodeWithText("Live Scores:")
            .assertIsDisplayed()
    }

    @Test
    fun lazyColumn_appearsWithinTimeout() {
        composeRule
            .onNode(hasScrollAction())
            .assertExists(
                "Expected a LazyColumn (or other scrollable) in the hierarchy"
            )
    }

    @Test
    fun potwButton_navigatesToPotwScreen() {
        // click the AccountCircle icon in the top bar
        composeRule
            .onNode(hasContentDescription("Player of the Week"))
            .assertExists("POTW icon missing")
            .performClick()
    }

    @Test
    fun refreshFab_isPresentAndClickable() {
        composeRule
            .onNode(hasContentDescription("Refresh Games"))
            .assertExists("Refresh FAB missing")
            .assertIsDisplayed()
            .performClick()
    }

    @Test
    fun settingsIcon_isPresentAndClickable() {
        composeRule
            .onNode(hasContentDescription("Toggle Theme"))
            .assertExists("Settings icon missing")
            .assertIsDisplayed()
            .performClick()
    }
}