package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue

@RunWith(AndroidJUnit4::class)
class PrimaryUiTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    @Test fun pinEnrollmentRequiresConfirmationAndStoresCredential() {
        val activity = compose.activity
        val db = com.example.data.AppDatabase.getDatabase(activity)
        val repository = com.example.data.PrivacyRepository(db, activity.applicationContext)
        val original = runBlocking { repository.getConfigDirect() }
        assertTrue("Use a fresh test installation", original.hashedPin.isEmpty())
        try {
            compose.waitUntil(timeoutMillis = 30000) { compose.onAllNodesWithText(activity.getString(R.string.nav_settings)).fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText(activity.getString(R.string.nav_settings)).performClick()
            compose.onNodeWithText(activity.getString(R.string.setup)).performClick()
            compose.onNodeWithTag("pin_setup_input").performTextInput("135790")
            compose.onNodeWithTag("pin_confirm_input").performTextInput("135791")
            compose.onNodeWithText(activity.getString(R.string.common_confirm)).performClick()
            compose.onNodeWithText(activity.getString(R.string.settings_pin_matching)).assertIsDisplayed()
            assertTrue(runBlocking { repository.getConfigDirect().hashedPin.isEmpty() })
            compose.onNodeWithTag("pin_confirm_input").performTextClearance()
            compose.onNodeWithTag("pin_confirm_input").performTextInput("135790")
            compose.onNodeWithText(activity.getString(R.string.common_confirm)).performClick()
            compose.waitUntil(timeoutMillis = 60000) { compose.onAllNodesWithText(activity.getString(R.string.pin_configured)).fetchSemanticsNodes().isNotEmpty() }
            assertTrue(runBlocking { repository.verifyPrimaryPin("135790").accepted })
        } finally {
            runBlocking { db.securityConfigDao().insertConfig(original) }
        }
    }

    @Test fun realStartupAndNavigation() {
        val activity = compose.activity
        val appsTitle = activity.getString(R.string.your_apps)
        compose.waitUntil(timeoutMillis = 30000) {
            compose.onAllNodesWithText(appsTitle).fetchSemanticsNodes().isNotEmpty()
        }
        compose.onNodeWithText(appsTitle).assertIsDisplayed()
        if (!com.example.security.SecurityUtils.isAccessibilityServiceEnabled(activity)) {
            compose.onNodeWithText(activity.getString(R.string.accessibility_settings)).performClick()
            compose.onNodeWithText(activity.getString(R.string.accessibility_consent_open)).assertIsDisplayed()
            compose.onNodeWithText(activity.getString(R.string.common_cancel)).performClick()
        }
        compose.onNodeWithText(activity.getString(R.string.nav_events)).performClick()
        compose.onNodeWithText(activity.getString(R.string.security_events)).assertIsDisplayed()
        compose.onNodeWithText(activity.getString(R.string.nav_apps)).performClick()
        compose.onNodeWithText(appsTitle).assertIsDisplayed()
        compose.onNodeWithText(activity.getString(R.string.nav_profiles)).performClick()
        compose.onNodeWithText(activity.getString(R.string.profiles_explanation)).assertIsDisplayed()
    }
}
