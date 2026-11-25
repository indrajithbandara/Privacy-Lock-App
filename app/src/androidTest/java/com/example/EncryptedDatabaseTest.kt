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

@RunWith(AndroidJUnit4::class)
class EncryptedDatabaseTest {
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
