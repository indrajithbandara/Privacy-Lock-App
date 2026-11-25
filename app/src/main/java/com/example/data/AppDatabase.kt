package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import android.util.AtomicFile
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

@Database(
    entities = [
        LockedApp::class,
        SecurityConfig::class,
        TimelineEvent::class,
        IntruderSelfie::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun lockedAppDao(): LockedAppDao
    abstract fun securityConfigDao(): SecurityConfigDao
    abstract fun timelineEventDao(): TimelineEventDao
    abstract fun intruderSelfieDao(): IntruderSelfieDao

    companion object {
        private const val LEGACY = "privacy_lock_db"
        private const val ENCRYPTED = "privacy_lock_encrypted_v1"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: runBlocking(Dispatchers.IO) {
                openEncrypted(context.applicationContext)
            }.also { INSTANCE = it }
        }

        internal fun buildEncrypted(context: Context, name: String, passphrase: ByteArray): AppDatabase {
            System.loadLibrary("sqlcipher")
            return Room.databaseBuilder(context, AppDatabase::class.java, name)
                .openHelperFactory(SupportOpenHelperFactory(passphrase))
                .build()
        }

        internal suspend fun openEncrypted(context: Context, legacyName: String = LEGACY, encryptedName: String = ENCRYPTED,
                                           markerName: String = "database_migration_v1", keyName: String = "database_key_v1",
                                           keyAlias: String = "privacy_lock_database_v1"): AppDatabase {
            val legacyExists = context.getDatabasePath(legacyName).exists()
            val encryptedExists = context.getDatabasePath(encryptedName).exists()
            val marker = AtomicFile(File(context.noBackupFilesDir, markerName))
            if (marker.baseFile.exists()) check(encryptedExists) { "Migrated database is missing" }
            val passphrase = DatabaseKeyStore.loadOrCreate(context, encryptedExists, keyName, keyAlias)
            val target = buildEncrypted(context, encryptedName, passphrase)
            try {
                // Force open and schema validation before using or retiring the old database.
                target.lockedAppDao().getAllAppsDirect()
                if (!marker.baseFile.exists()) {
                    if (legacyExists) {
                        val source = Room.databaseBuilder(context, AppDatabase::class.java, legacyName).build()
                        try { copyLegacy(source, target) } finally { source.close() }
                    }
                    val output = marker.startWrite()
                    try {
                        output.write(1)
                        marker.finishWrite(output)
                    } catch (error: Exception) {
                        marker.failWrite(output)
                        throw error
                    }
                }
                // The verified copy and durable marker exist. Retire only the original database,
                // including its journal/WAL, without touching any other user files.
                if (legacyExists) check(context.deleteDatabase(legacyName)) { "Legacy database cleanup failed" }
                return target
            } catch (error: Throwable) {
                target.close()
                throw error
            }
        }

        internal suspend fun copyLegacy(source: AppDatabase, target: AppDatabase) {
            val apps = source.lockedAppDao().getAllAppsDirect().sortedBy { it.packageName }
            val configs = source.securityConfigDao().getAllConfigsDirect()
            val events = source.timelineEventDao().getAllEventsDirect()
            val selfies = source.intruderSelfieDao().getAllSelfiesDirect()
            target.withTransaction {
                target.lockedAppDao().insertApps(apps)
                configs.forEach { target.securityConfigDao().insertConfig(it) }
                events.forEach { target.timelineEventDao().insertEvent(it) }
                selfies.forEach { target.intruderSelfieDao().insertSelfie(it) }
                check(target.lockedAppDao().getAllAppsDirect().sortedBy { it.packageName } == apps)
                check(target.securityConfigDao().getAllConfigsDirect() == configs)
                check(target.timelineEventDao().getAllEventsDirect() == events)
                check(target.intruderSelfieDao().getAllSelfiesDirect() == selfies)
            }
        }
    }
}
