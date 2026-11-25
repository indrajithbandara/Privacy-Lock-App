package com.example

import android.content.Context
import android.os.Build
import android.os.Process
import android.app.KeyguardManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.AppDatabase
import com.example.data.PrivacyRepository
import com.example.data.SecurityConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Two explicit instrumentation invocations with a real force-stop between them. */
@RunWith(AndroidJUnit4::class)
class RestartPersistenceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val evidence get() = context.getSharedPreferences("restart_test_evidence", Context.MODE_PRIVATE)

    private fun requireEmulator() {
        assertTrue("Disposable emulator required", Build.HARDWARE in setOf("ranchu", "goldfish") || Build.PRODUCT.contains("sdk"))
    }

    @Test fun prepareFailedAttempt() = runBlocking {
        requireEmulator()
        assertFalse("Disposable emulator must have no secure device credential", checkNotNull(context.getSystemService(KeyguardManager::class.java)).isDeviceSecure)
        val db = AppDatabase.getDatabase(context)
        val repository = PrivacyRepository(db, context)
        assertTrue("Fresh test installation required", repository.getConfigDirect().hashedPin.isEmpty())
        assertTrue(repository.changePrimaryPin("", "246810"))
        repository.updateConfig { it.copy(failedAttemptLimit = 1) }
        val result = repository.verifyPrimaryPin("111111", "Restart persistence fixture")
        assertFalse(result.accepted)
        assertTrue(result.attempted)
        assertTrue(result.retryAfterSeconds > 0)
        assertTrue(db.timelineEventDao().getAllEventsDirect().any { it.type == "AUTH_FAILURE" && it.appName == "Restart persistence fixture" })
        assertTrue(evidence.edit().putInt("pid", Process.myPid()).commit())
    }

    @Test fun verifyAfterProcessRestart() = runBlocking {
        requireEmulator()
        val previousPid = evidence.getInt("pid", -1)
        assertTrue("Preparation phase must precede this test", previousPid > 0)
        // The shell separately verifies a changed kernel boot ID in the reboot phase;
        // Linux may reuse the same PID across boots, unlike a same-boot force-stop.
        if (InstrumentationRegistry.getArguments().getString("reboot") != "true") {
            assertNotEquals("App process must genuinely have changed", previousPid, Process.myPid())
        }
        val db = AppDatabase.getDatabase(context)
        val repository = PrivacyRepository(db, context)
        assertTrue(repository.getConfigDirect().hashedPin.isNotEmpty())
        assertTrue(db.timelineEventDao().getAllEventsDirect().any { it.type == "AUTH_FAILURE" && it.appName == "Restart persistence fixture" })
        val blocked = repository.verifyPrimaryPin("246810")
        assertFalse("Correct PIN must not bypass persisted cooldown", blocked.accepted)
        assertFalse("Cooldown must prevent credential verification", blocked.attempted)
        assertTrue(blocked.retryAfterSeconds > 0)
        delay(blocked.retryAfterSeconds * 1000 + 500)
        assertTrue("Correct PIN accepted only after cooldown", repository.verifyPrimaryPin("246810").accepted)
        assertEquals(0, context.getSharedPreferences("authentication_attempts_v1", Context.MODE_PRIVATE).getInt("attempts", 0))
        // Only restore the fresh disposable fixture configuration after successful assertions.
        db.securityConfigDao().insertConfig(SecurityConfig())
        assertTrue(evidence.edit().clear().commit())
    }
}
