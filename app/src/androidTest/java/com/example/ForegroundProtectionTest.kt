package com.example

import android.app.UiAutomation
import android.graphics.Rect
import android.os.Build
import android.os.SystemClock
import android.view.MotionEvent
import android.view.InputDevice
import android.view.accessibility.AccessibilityNodeInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.AppDatabase
import com.example.data.PrivacyRepository
import com.example.security.AppLockManager
import com.example.security.SecurityUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises real Android window events against a separately installed, harmless fixture APK. */
@RunWith(AndroidJUnit4::class)
class ForegroundProtectionTest {
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
        // Settings rows may expose non-clickable text or omit accessibility click actions.
        // Tap the visible label on the real emulator screen as an ordinary user would.
        if (!node.isVisibleToUser) return false
        val bounds = Rect().also(node::getBoundsInScreen)
        if (bounds.isEmpty) return false
        val now = SystemClock.uptimeMillis()
        val down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, bounds.exactCenterX(), bounds.exactCenterY(), 0)
        val up = MotionEvent.obtain(now, now + 50, MotionEvent.ACTION_UP, bounds.exactCenterX(), bounds.exactCenterY(), 0)
        down.source = InputDevice.SOURCE_TOUCHSCREEN
        up.source = InputDevice.SOURCE_TOUCHSCREEN
        return try { automation.injectInputEvent(down, true) && automation.injectInputEvent(up, true) }
        finally { down.recycle(); up.recycle() }
    }
    private fun clickText(text: String): Boolean = nodes().firstOrNull { it.isVisibleToUser && it.text?.toString() == text }?.let(::clickNode) == true
    private fun clickSwitch(): Boolean = nodes().firstOrNull { it.className?.toString() == "android.widget.Switch" && it.isEnabled }?.let(::clickNode) == true
    private fun launchSettings() {
        shell("am start -W -a android.settings.ACCESSIBILITY_SETTINGS -f 0x10008000")
    }

    /** Exercise the visible, user-controlled Android Settings consent flow; no secure-setting writes. */
    private fun changeServiceThroughSettings(enable: Boolean) {
        if (SecurityUtils.isAccessibilityServiceEnabled(context) == enable) return
        launchSettings()
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

    private fun launchFixture() { shell("am start -W -n $fixture/.FixtureActivity") }
    private fun enterPin(pin: String) {
        pin.forEach { digit -> eventually("keypad digit") { clickText(digit.toString()) } }
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
            eventually("wrong PIN rejection") { hasText(context.getString(R.string.auth_denied)) }
            assertTrue(db.timelineEventDao().getAllEventsDirect().any { it.type == "AUTH_FAILURE" && it.appName == "Protection test fixture" })
            android.util.Log.i("ForegroundProtectionTest", "Rejected incorrect PIN and recorded event")
            SystemClock.sleep(1500)
            enterPin("246810")
            eventually("fixture access after authentication") { hasText("Fixture content visible") }
            android.util.Log.i("ForegroundProtectionTest", "Authenticated and observed fixture")
            shell("input keyevent KEYCODE_HOME")
            eventually("leave fixture") { !AppLockManager.isForegroundTarget(fixture) }
            launchFixture()
            eventually("relock after real app switch") { hasText(prompt) }
            android.util.Log.i("ForegroundProtectionTest", "Observed relocking after application switch")
            changeServiceThroughSettings(false)
            assertFalse(AppLockManager.serviceConnected.value)
        } catch (error: Throwable) {
            failure = error
            throw error
        } finally {
            // Restore disposable test state without changing any permission through privileged APIs.
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
