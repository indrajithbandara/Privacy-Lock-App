package com.example

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.AppDatabase
import com.example.data.LockedApp
import com.example.data.SecurityConfig
import com.example.data.TimelineEvent
import com.example.data.IntruderSelfie
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.security.SecureRandom
import java.security.KeyStore
import java.io.File

@RunWith(AndroidJUnit4::class)
class EncryptedDatabaseTest {
    @Test fun completeUpgradePersistsKeyAndRetiresOnlyVerifiedLegacyCopy() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val suffix = java.util.UUID.randomUUID().toString()
        val legacy = "upgrade-test-legacy-$suffix"
        val encrypted = "upgrade-test-encrypted-$suffix"
        val marker = "upgrade-test-marker-$suffix"
        val key = "upgrade-test-key-$suffix"
        val alias = "upgrade-test-alias-$suffix"
        val expected = SecurityConfig(hashedPin = "test-only-legacy-verifier", themeMode = "DARK", failedAttemptLimit = 3)
        val app = LockedApp("fixture.app", "Fixture", "Other", true, true, true, "android")
        try {
            val source = Room.databaseBuilder(context, AppDatabase::class.java, legacy).build()
            try {
                source.lockedAppDao().insertApp(app)
                source.securityConfigDao().insertConfig(expected)
            } finally { source.close() }
            // Simulate interruption after the encrypted target and its key were created, before copy.
            val raw = com.example.data.DatabaseKeyStore.loadOrCreate(context, false, key, alias)
            val interrupted = AppDatabase.buildEncrypted(context, encrypted, raw)
            try { interrupted.lockedAppDao().getAllAppsDirect() } finally { interrupted.close(); raw.fill(0) }
            assertTrue(context.getDatabasePath(legacy).exists())
            assertFalse(File(context.noBackupFilesDir, marker).exists())
            val migrated = AppDatabase.openEncrypted(context, legacy, encrypted, marker, key, alias)
            try {
                assertEquals(expected, migrated.securityConfigDao().getConfigDirect())
                assertEquals(app, migrated.lockedAppDao().getApp(app.packageName))
            } finally { migrated.close() }
            assertFalse(context.getDatabasePath(legacy).exists())
            assertTrue(File(context.noBackupFilesDir, marker).exists())
            val reopened = AppDatabase.openEncrypted(context, legacy, encrypted, marker, key, alias)
            try { assertEquals(expected, reopened.securityConfigDao().getConfigDirect()) } finally { reopened.close() }
            // Failure to recover an existing key must never create a new empty database.
            File(context.noBackupFilesDir, key).delete()
            try {
                AppDatabase.openEncrypted(context, legacy, encrypted, marker, key, alias).close()
                fail("Opening an encrypted database without its key must fail")
            } catch (_: IllegalStateException) { }
            assertTrue(context.getDatabasePath(encrypted).exists())
        } finally {
            context.deleteDatabase(legacy)
            context.deleteDatabase(encrypted)
            File(context.noBackupFilesDir, marker).delete()
            File(context.noBackupFilesDir, key).delete()
            KeyStore.getInstance("AndroidKeyStore").apply { load(null); deleteEntry(alias) }
        }
    }

    @Test fun copiesExistingRowsAndReopensEncryptedDatabase() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val sourceName = "migration-test-plain"
        val targetName = "migration-test-encrypted"
        context.deleteDatabase(sourceName)
        context.deleteDatabase(targetName)
        val passphrase = ByteArray(32).also(SecureRandom()::nextBytes)
        val source = Room.databaseBuilder(context, AppDatabase::class.java, sourceName).build()
        val target = AppDatabase.buildEncrypted(context, targetName, passphrase)
        val app = LockedApp("test.app", "Test", "Other", true, false, false, "android")
        try {
            source.lockedAppDao().insertApp(app)
            source.securityConfigDao().insertConfig(SecurityConfig(themeMode = "DARK"))
            source.timelineEventDao().insertEvent(TimelineEvent(id = 14, type = "TEST", description = "Legacy record"))
            source.intruderSelfieDao().insertSelfie(IntruderSelfie(id = 7, failedPinAttempt = "masked", avatarId = 1))
            AppDatabase.copyLegacy(source, target)
            assertEquals(app, target.lockedAppDao().getApp("test.app"))
            assertEquals(14, target.timelineEventDao().getAllEventsDirect().single().id)
            assertEquals(7, target.intruderSelfieDao().getAllSelfiesDirect().single().id)
            target.close()
            val header = context.getDatabasePath(targetName).inputStream().use { stream ->
                ByteArray(16).also { stream.read(it) }
            }
            assertFalse(header.contentEquals("SQLite format 3\u0000".toByteArray()))
            val reopened = AppDatabase.buildEncrypted(context, targetName, passphrase)
            try {
                assertEquals(app, reopened.lockedAppDao().getApp("test.app"))
            } finally { reopened.close() }
        } finally {
            source.close()
            target.close()
            passphrase.fill(0)
            context.deleteDatabase(sourceName)
            context.deleteDatabase(targetName)
        }
    }
}
