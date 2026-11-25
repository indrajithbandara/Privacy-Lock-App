package com.example

import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.AppDatabase
import com.example.data.PrivacyRepository
import com.example.security.AppLockManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Isolates real lock UI and credential rejection from external window-event delivery. */
@RunWith(AndroidJUnit4::class)
class RealLockUiTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun realLockActivityRejectsIncorrectPinAndRecordsFailure() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = AppDatabase.getDatabase(context)
        val repository = PrivacyRepository(db, context)
        val fixture = "com.example.protectionfixture"
        val original = runBlocking { repository.getConfigDirect() }
        assertTrue("Fresh disposable test installation required", original.hashedPin.isEmpty())
        assertTrue(AppLockManager.canProtect(context, fixture))
        try {
            assertTrue(runBlocking { repository.changePrimaryPin("", "246810") })
            runBlocking { repository.updateConfig { it.copy(randomizeKeypad = false, biometricsEnabled = false) } }
            // Respect any cooldown left by another instrumentation scenario.
            runBlocking {
                val initial = repository.verifyPrimaryPin("246810")
                if (!initial.accepted && initial.retryAfterSeconds > 0) {
                    delay(initial.retryAfterSeconds * 1000 + 250)
                    assertTrue(repository.verifyPrimaryPin("246810").accepted)
                } else assertTrue(initial.accepted)
            }
            val before = runBlocking { db.timelineEventDao().getAllEventsDirect().count { it.type == "AUTH_FAILURE" } }
            ActivityScenario.launch<LockActivity>(Intent(context, LockActivity::class.java).putExtra("TARGET_PACKAGE_NAME", fixture)).use {
                val title = context.getString(R.string.auth_app_locked, "Protection test fixture")
                compose.waitUntil(timeoutMillis = 30_000) { compose.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty() }
                repeat(6) { index ->
                    compose.onNodeWithTag("keypad_btn_1").assertIsDisplayed().performTouchInput { click() }
                    if (index < 5) {
                        val count = context.getString(R.string.auth_digits_entered, index + 1)
                        compose.waitUntil(timeoutMillis = 15_000) { compose.onAllNodesWithText(count).fetchSemanticsNodes().isNotEmpty() }
                    }
                }
                compose.waitUntil(timeoutMillis = 60_000) { compose.onAllNodesWithText(context.getString(R.string.auth_denied)).fetchSemanticsNodes().isNotEmpty() }
                val after = runBlocking { db.timelineEventDao().getAllEventsDirect().count { it.type == "AUTH_FAILURE" } }
                assertTrue("A real failed authentication event must be persisted", after > before)
                assertFalse(AppLockManager.isPackageTemporarilyUnlocked(fixture))
            }
            runBlocking { assertTrue(repository.verifyPrimaryPin("246810").accepted) }
        } finally {
            runBlocking { db.securityConfigDao().insertConfig(original) }
            AppLockManager.clearTemporaryUnlocks()
        }
    }
}
