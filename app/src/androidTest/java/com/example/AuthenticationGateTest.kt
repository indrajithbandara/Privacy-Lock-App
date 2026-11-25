package com.example

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.security.AuthenticationGate
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthenticationGateTest {
    @Test fun failedAttemptPersistsAndBlocksFurtherVerification() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val prefs = context.getSharedPreferences("authentication_attempts_v1", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        try {
            val failed = AuthenticationGate.authenticate(context, 1) { false }
            assertFalse(failed.accepted)
            assertTrue(failed.attempted)
            assertEquals(1, prefs.getInt("attempts", 0))
            var verifierCalled = false
            val blocked = AuthenticationGate.authenticate(context, 1) {
                verifierCalled = true
                true
            }
            assertFalse(blocked.accepted)
            assertFalse(verifierCalled)
            assertTrue(blocked.retryAfterSeconds > 0)
        } finally {
            prefs.edit().clear().commit()
        }
    }
}
