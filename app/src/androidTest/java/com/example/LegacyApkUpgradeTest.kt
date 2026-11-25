package com.example

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.AppDatabase
import com.example.data.PrivacyRepository
import com.example.security.CredentialEnvelope
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/** Runs only on the dedicated emulator after a real APK update over the legacy fixture. */
@RunWith(AndroidJUnit4::class)
class LegacyApkUpgradeTest {
    @Test fun installedUpgradePreservesLegacyRecordsAndMigratesVerifier() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = AppDatabase.getDatabase(context)
        val repository = PrivacyRepository(db, context)
        val config = repository.getConfigDirect()
        assertEquals("DARK", config.themeMode)
        assertEquals(30, config.autoLockTimeoutSeconds)
        assertEquals(3, config.failedAttemptLimit)
        val app = checkNotNull(db.lockedAppDao().getApp("com.example.legacyrecord"))
        assertTrue(app.isLocked && app.isFavorite && app.isPinned)
        assertTrue(db.timelineEventDao().getAllEventsDirect().any { it.id == 17 && it.description == "Representative legacy fixture history" })
        assertTrue(db.intruderSelfieDao().getAllSelfiesDirect().any { it.id == 11 && it.failedPinAttempt == "******" })
        assertTrue(repository.verifyPrimaryPin("731824").accepted)
        assertTrue(CredentialEnvelope.isWrapped(repository.getConfigDirect().hashedPin))
        assertTrue(File(context.noBackupFilesDir, "database_migration_v1").exists())
        assertFalse(context.getDatabasePath("privacy_lock_db").exists())
        val header = context.getDatabasePath("privacy_lock_encrypted_v1").inputStream().use { input -> ByteArray(16).also { input.read(it) } }
        assertFalse(header.contentEquals("SQLite format 3\u0000".toByteArray()))
    }
}
