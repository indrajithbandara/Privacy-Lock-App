package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.security.AppLockManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProtectionSessionTest {
    @Test fun switchingTargetRevokesImmediateSession() {
        AppLockManager.clearTemporaryUnlocks()
        AppLockManager.setForegroundTarget("fixture.first")
        try {
            AppLockManager.temporarilyUnlockPackage("fixture.first")
            assertTrue(AppLockManager.isPackageTemporarilyUnlocked("fixture.first"))
            AppLockManager.setForegroundTarget("fixture.second")
            assertFalse(AppLockManager.isPackageTemporarilyUnlocked("fixture.first"))
            assertFalse(AppLockManager.isForegroundTarget("fixture.first"))
            assertTrue(AppLockManager.isForegroundTarget("fixture.second"))
        } finally {
            AppLockManager.clearTemporaryUnlocks()
            AppLockManager.setForegroundTarget(null)
        }
    }

    @Test fun sessionResetAndOwnerControlsRemainAccessible() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        AppLockManager.temporarilyUnlockPackage("fixture.first")
        AppLockManager.clearTemporaryUnlocks()
        assertFalse(AppLockManager.isPackageTemporarilyUnlocked("fixture.first"))
        assertFalse(AppLockManager.canProtect(context, context.packageName))
        assertFalse(AppLockManager.canProtect(context, "com.android.settings"))
    }
}
