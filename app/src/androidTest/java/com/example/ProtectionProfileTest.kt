package com.example

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.data.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProtectionProfileTest {
    @Test fun additiveMigrationPreservesExistingEncryptedRows() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "profile-migration-${java.util.UUID.randomUUID()}"
        val key = ByteArray(32).also(java.security.SecureRandom()::nextBytes)
        val existing = LockedApp("fixture.existing", "Existing", "Other", true, iconName = "android")
        try {
            val old = AppDatabase.buildEncrypted(context, name, key)
            try {
                old.lockedAppDao().insertApp(existing)
                old.securityConfigDao().insertConfig(SecurityConfig(themeMode = "DARK"))
                // Reconstruct the version-2 table layout before exercising the real 2->3 migration.
                val sqlite = old.openHelper.writableDatabase
                sqlite.execSQL("DROP TABLE profile_apps")
                sqlite.execSQL("DROP TABLE protection_profiles")
                sqlite.version = 2
            } finally { old.close() }
            val upgraded = AppDatabase.buildEncrypted(context, name, key)
            try {
                assertEquals(existing, upgraded.lockedAppDao().getApp(existing.packageName))
                assertEquals("DARK", upgraded.securityConfigDao().getConfigDirect()?.themeMode)
                assertTrue(upgraded.lockedAppDao().isEffectivelyLocked(existing.packageName))
                assertTrue(upgraded.protectionProfileDao().insert(ProtectionProfile(name = "New")) > 0)
            } finally { upgraded.close() }
        } finally { key.fill(0); context.deleteDatabase(name) }
    }

    @Test fun effectiveProtectionIsUnionAndDeletionDoesNotRemoveIndividualLocks() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        try {
            val apps = db.lockedAppDao()
            val profiles = db.protectionProfileDao()
            apps.insertApp(LockedApp("fixture.individual", "Individual", "Other", true, iconName = "android"))
            val first = profiles.insert(ProtectionProfile(name = "First"))
            val second = profiles.insert(ProtectionProfile(name = "Second", active = true))
            profiles.insertMembers(listOf(ProfileApp(first, "fixture.profile"), ProfileApp(second, "fixture.profile"), ProfileApp(first, "fixture.individual")))
            assertTrue(apps.isEffectivelyLocked("fixture.profile"))
            profiles.setActive(second, false)
            assertFalse(apps.isEffectivelyLocked("fixture.profile"))
            profiles.setActive(first, true)
            assertTrue(apps.isEffectivelyLocked("fixture.profile"))
            profiles.rename(first, "Edited")
            assertEquals("Edited", profiles.get(first)?.name)
            profiles.delete(first)
            assertFalse(apps.isEffectivelyLocked("fixture.profile"))
            assertTrue(apps.isEffectivelyLocked("fixture.individual"))
        } finally { db.close() }
    }

    @Test fun encryptedProfilePersistsAcrossReopen() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val name = "profile-test-${java.util.UUID.randomUUID()}"
        val key = ByteArray(32).also(java.security.SecureRandom()::nextBytes)
        try {
            val db = AppDatabase.buildEncrypted(context, name, key)
            val id = try {
                val profiles = db.protectionProfileDao()
                val id = profiles.insert(ProtectionProfile(name = "Persistent", active = true))
                profiles.insertMembers(listOf(ProfileApp(id, "fixture.member")))
                id
            } finally { db.close() }
            val reopened = AppDatabase.buildEncrypted(context, name, key)
            try {
                assertEquals("Persistent", reopened.protectionProfileDao().get(id)?.name)
                assertTrue(reopened.lockedAppDao().isEffectivelyLocked("fixture.member"))
            } finally { reopened.close() }
        } finally { key.fill(0); context.deleteDatabase(name) }
    }
}
