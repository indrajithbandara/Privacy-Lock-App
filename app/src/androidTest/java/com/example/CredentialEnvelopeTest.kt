package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.security.CredentialEnvelope
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CredentialEnvelopeTest {
    @Test fun encryptsWithRandomIvAndRejectsTampering() {
        val value = "test-credential-record"
        val first = CredentialEnvelope.wrap(value)
        val second = CredentialEnvelope.wrap(value)
        assertTrue(CredentialEnvelope.isWrapped(first))
        assertNotEquals(first, second)
        assertEquals(value, CredentialEnvelope.unwrap(first))
        val parts = first.split(':')
        val bytes = android.util.Base64.decode(parts[2], android.util.Base64.NO_WRAP)
        bytes[0] = (bytes[0].toInt() xor 1).toByte()
        val altered = parts[0] + ":" + parts[1] + ":" +
            android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        assertNull(CredentialEnvelope.unwrap(altered))
    }
}
