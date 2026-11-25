package com.example

import com.example.security.PinCredentials
import org.junit.Assert.*
import org.junit.Test

class PinCredentialsTest {
    @Test fun uniqueSaltsAndCorrectVerification() {
        val first = PinCredentials.create("731824")
        val second = PinCredentials.create("731824")
        assertNotEquals(first, second)
        assertNotEquals(first.split(':')[1], second.split(':')[1])
        assertTrue(PinCredentials.verify("731824", first))
        assertFalse(PinCredentials.verify("731825", first))
    }

    @Test fun malformedAndMissingRecordsAreRejected() {
        for (record in listOf("", "p2s256:bad:bad", "unsupported:123:456", "123")) {
            assertFalse(PinCredentials.verify("731824", record))
        }
    }

    @Test fun legacyAndroidPbkdf2Compatibility() {
        val record = PinCredentials.create("731824", sha256Available = false)
        assertTrue(record.startsWith("p2s1:"))
        assertTrue(PinCredentials.verify("731824", record))
        assertFalse(PinCredentials.verify("731825", record))
    }

    @Test fun legacyRecordIsRecognizedForMigration() {
        val old = java.security.MessageDigest.getInstance("SHA-256")
            .digest("731824PrivacyLockSaltEnterprise".toByteArray())
            .joinToString("") { "%02x".format(it) }
        assertTrue(PinCredentials.isLegacy(old))
        assertTrue(PinCredentials.verify("731824", old))
        assertFalse(PinCredentials.verify("731825", old))
        assertFalse(PinCredentials.isLegacy(PinCredentials.create("731824")))
    }
}
