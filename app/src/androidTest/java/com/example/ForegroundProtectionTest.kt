package com.example

import android.app.UiAutomation
import android.graphics.Rect
import android.os.Build
import android.os.SystemClock
import android.view.accessibility.AccessibilityNodeInfo
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.AppDatabase
import com.example.data.PrivacyRepository
import com.example.security.AppLockManager
import com.example.security.SecurityUtils
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import org.junit.runner.RunWith

/** Exercises real Android window events against a separately installed, harmless fixture APK. */
@RunWith(AndroidJUnit4::class)
class ForegroundProtectionTest {
    @get:Rule val compose = createEmptyComposeRule()
    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val fixture = "com.example.protectionfixture"
    private lateinit var automation: UiAutomation

    private fun eventually(description: String, timeout: Long = 15_000, predicate: () -> Boolean) {
        val deadline = SystemClock.elapsedRealtime() + timeout
        while (SystemClock.elapsedRealtime() < deadline) {
            if (predicate()) return
            SystemClock.sleep(200)
        }
        fail("Timed out: $description")
    }

    private fun nodes(): List<AccessibilityNodeInfo> {
        val root = automation.rootInActiveWindow ?: return emptyList()
        val result = mutableListOf<AccessibilityNodeInfo>()
        fun visit(node: AccessibilityNodeInfo) {
            result.add(node)
            for (i in 0 until node.childCount) node.getChild(i)?.let(::visit)
        }
        visit(root)
        return result
    }

    private fun hasText(text: String) = nodes().any { it.text?.toString() == text }
    private fun clickNode(node: AccessibilityNodeInfo): Boolean {
        var current: AccessibilityNodeInfo? = node
        while (current != null) {
            if (current.isClickable && current.performAction(AccessibilityNodeInfo.ACTION_CLICK)) return true
            current = current.parent
        }
        return tapNode(node)
    }

    private fun tapNode(node: AccessibilityNodeInfo): Boolean {
        // Settings rows may omit click actions. Also use ordinary touches for the PIN keypad.
        if (!node.isVisibleToUser) return false
        val bounds = Rect().also(node::getBoundsInScreen)
        if (bounds.isEmpty) return false
        shell("input tap ${bounds.centerX()} ${bounds.centerY()}")
        return true
    }
    private fun clickText(text: String): Boolean = nodes().firstOrNull { it.isVisibleToUser && it.text?.toString() == text }?.let(::clickNode) == true
    private fun clickSwitch(): Boolean = nodes().firstOrNull { it.className?.toString() == "android.widget.Switch" && it.isEnabled }?.let(::clickNode) == true
    private fun launchSettings() {
        shell("am start -W -a android.settings.ACCESSIBILITY_SETTINGS -f 0x10008000")
    }

    /** Exercise the visible, user-controlled Android Settings consent flow; no secure-setting writes. */
    private fun changeServiceThroughSettings(enable: Boolean) {
        if (SecurityUtils.isAccessibilityServiceEnabled(context) == enable) return
        if (enable) {
            ActivityScenario.launch(MainActivity::class.java).use {
                val disclosure = context.getString(R.string.accessibility_settings)
                compose.waitUntil(timeoutMillis = 30_000) { compose.onAllNodesWithText(disclosure).fetchSemanticsNodes().isNotEmpty() }
                compose.onNodeWithText(disclosure).assertIsDisplayed().performClick()
                compose.onNodeWithText(context.getString(R.string.accessibility_consent_open)).assertIsDisplayed().performClick()
                eventually("system settings opened") { nodes().any { it.packageName?.toString() == "com.android.settings" } }
            }
        } else launchSettings()
        val name = context.getString(R.string.app_name)
        eventually("accessibility settings service list") {
            if (hasText(name)) return@eventually true
            clickText("Downloaded apps") || clickText("Installed apps")
            false
        }
        eventually("open service settings") { clickText(name) }
        eventually("service switch") { clickSwitch() }
        eventually("system consent", 15_000) {
            if (SecurityUtils.isAccessibilityServiceEnabled(context) == enable) return@eventually true
            listOf("Allow", "OK", "Stop", "Turn off").any { clickText(it) }
            false
        }
        eventually("service connection state") { AppLockManager.serviceConnected.value == enable }
        android.util.Log.i("ForegroundProtectionTest", "Service consent completed: $enable")
    }

    private fun shell(command: String) {
        automation.executeShellCommand(command).use { descriptor ->
            java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() }
        }
    }

    private fun launchFixture() {
        // Compose/Espresso may acquire UiAutomation during UI interactions. Always
        // reacquire the non-suppressing connection before a real external-app launch.
        automation = instrumentation.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
        eventually("service remains connected before fixture launch") { AppLockManager.serviceConnected.value }
        shell("am start -W -n $fixture/.FixtureActivity")
        eventually("service observed fixture foreground") { AppLockManager.isForegroundTarget(fixture) }
    }
    private fun enterPin(pin: String) {
        pin.forEachIndexed { index, digit ->
            // Interact with the real LockActivity Compose node, not a separate test activity
            // or a fake credential callback. Android window assertions remain external.
            compose.onNodeWithTag("keypad_btn_$digit").assertIsDisplayed().performTouchInput { click() }
            if (index < pin.lastIndex) {
                val expected = context.getString(R.string.auth_digits_entered, index + 1)
                compose.waitUntil(timeoutMillis = 15_000) { compose.onAllNodesWithText(expected).fetchSemanticsNodes().isNotEmpty() }
                android.util.Log.i("ForegroundProtectionTest", "Verified keypad length ${index + 1}")
            }
        }
    }

    @Test fun realForegroundPromptRejectsWrongPinAndRelocksAfterSwitch() = runBlocking {
        // This scenario manipulates only a disposable emulator with the dedicated fixture installed.
        assertTrue("Use a disposable emulator", Build.HARDWARE in setOf("ranchu", "goldfish") || Build.PRODUCT.contains("sdk"))
        assertNotNull("Install the dedicated test fixture first", context.packageManager.getLaunchIntentForPackage(fixture))
        automation = instrumentation.getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES)
        val db = AppDatabase.getDatabase(context)
        val repository = PrivacyRepository(db, context)
        val previous = repository.getConfigDirect()
        assertTrue("Use a fresh test installation", previous.hashedPin.isEmpty())
        val serviceWasEnabled = SecurityUtils.isAccessibilityServiceEnabled(context)
        assertFalse("Use a fresh emulator permission state", serviceWasEnabled)
        var failure: Throwable? = null
        var profileId: Long? = null
        try {
            assertTrue(repository.changePrimaryPin("", "246810"))
            repository.updateConfig { it.copy(autoLockTimeoutSeconds = 0, randomizeKeypad = false, biometricsEnabled = false) }
            repository.syncInstalledApps(context)
            assertTrue(AppLockManager.canProtect(context, fixture))
            repository.toggleAppLock(fixture, true)
            assertTrue(db.lockedAppDao().isEffectivelyLocked(fixture))
            changeServiceThroughSettings(true)
            launchFixture()
            val prompt = context.getString(R.string.auth_app_locked, "Protection test fixture")
            eventually("real lock activity") { hasText(prompt) }
            android.util.Log.i("ForegroundProtectionTest", "Observed actual foreground lock")
            enterPin("111111")
            eventually("wrong PIN rejection", 60_000) { hasText(context.getString(R.string.auth_denied)) }
            assertTrue(db.timelineEventDao().getAllEventsDirect().any { it.type == "AUTH_FAILURE" && it.appName == "Protection test fixture" })
            android.util.Log.i("ForegroundProtectionTest", "Rejected incorrect PIN and recorded event")
            SystemClock.sleep(1500)
            enterPin("246810")
            eventually("fixture access after authentication", 60_000) { hasText("Fixture content visible") }
            android.util.Log.i("ForegroundProtectionTest", "Authenticated and observed fixture")
            shell("input keyevent KEYCODE_HOME")
            eventually("leave fixture") { !AppLockManager.isForegroundTarget(fixture) }
            launchFixture()
            eventually("relock after real app switch") { hasText(prompt) }
            android.util.Log.i("ForegroundProtectionTest", "Observed relocking after application switch")
            // Profile-only protection must work even when the global timeout is longer.
            shell("input keyevent KEYCODE_HOME")
            repository.updateConfig { it.copy(autoLockTimeoutSeconds = 300) }
            repository.saveProfile(null, "Emulator fixture profile", setOf(fixture), true)
            val activeProfile = repository.profiles.first().single { it.name == "Emulator fixture profile" }
            profileId = activeProfile.id
            repository.setProfileActive(activeProfile.id, true)
            repository.toggleAppLock(fixture, false)
            assertTrue(db.lockedAppDao().isEffectivelyLocked(fixture))
            assertTrue(db.lockedAppDao().isImmediateProfileLock(fixture))
            launchFixture()
            eventually("profile-only foreground lock") { hasText(prompt) }
            enterPin("246810")
            eventually("profile fixture access", 60_000) { hasText("Fixture content visible") }
            shell("input keyevent KEYCODE_HOME")
            eventually("leave profile fixture") { !AppLockManager.isForegroundTarget(fixture) }
            launchFixture()
            eventually("profile immediate relock despite global timeout") { hasText(prompt) }
            android.util.Log.i("ForegroundProtectionTest", "Observed strict profile relock")
            changeServiceThroughSettings(false)
            assertFalse(AppLockManager.serviceConnected.value)
        } catch (error: Throwable) {
            failure = error
            throw error
        } finally {
            // Restore disposable test state without changing any permission through privileged APIs.
            profileId?.let { repository.deleteProfile(it) }
            repository.toggleAppLock(fixture, false)
            db.securityConfigDao().insertConfig(previous)
            AppLockManager.clearTemporaryUnlocks()
            try {
                if (SecurityUtils.isAccessibilityServiceEnabled(context)) changeServiceThroughSettings(false)
                shell("input keyevent KEYCODE_HOME")
            } catch (cleanup: Throwable) {
                failure?.addSuppressed(cleanup) ?: throw cleanup
            }
        }
    }
}
