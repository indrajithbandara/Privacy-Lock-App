package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrimaryUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun realStartupAndNavigation() {
        val activity = compose.activity
        val appsTitle = activity.getString(R.string.your_apps)
        compose.waitUntil(timeoutMillis = 30000) {
            compose.onAllNodesWithText(appsTitle).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(appsTitle).assertIsDisplayed()
        compose.onNodeWithText(activity.getString(R.string.nav_events)).performClick()
        compose.onNodeWithText(activity.getString(R.string.security_events)).assertIsDisplayed()
        compose.onNodeWithText(activity.getString(R.string.nav_apps)).performClick()
        compose.onNodeWithText(appsTitle).assertIsDisplayed()
        compose.onNodeWithText(activity.getString(R.string.nav_profiles)).performClick()
        compose.onNodeWithText(activity.getString(R.string.profiles_explanation)).assertIsDisplayed()
    }
}
