package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LockedAppDao {
    @Query("SELECT * FROM locked_apps ORDER BY isPinned DESC, name ASC")
    fun getAllApps(): Flow<List<LockedApp>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: LockedApp)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<LockedApp>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewApp(app: LockedApp): Long

    @Query("UPDATE locked_apps SET name = :name, category = :category, iconName = :iconName WHERE packageName = :packageName")
    suspend fun updateAppMetadata(packageName: String, name: String, category: String, iconName: String)

    @Query("UPDATE locked_apps SET isLocked = :isLocked WHERE packageName = :packageName")
    suspend fun updateAppLockStatus(packageName: String, isLocked: Boolean)

    @Query("UPDATE locked_apps SET isFavorite = :isFavorite WHERE packageName = :packageName")
    suspend fun updateAppFavoriteStatus(packageName: String, isFavorite: Boolean)

    @Query("UPDATE locked_apps SET isPinned = :isPinned WHERE packageName = :packageName")
    suspend fun updateAppPinnedStatus(packageName: String, isPinned: Boolean)

    @Query("SELECT * FROM locked_apps WHERE packageName = :packageName")
    suspend fun getApp(packageName: String): LockedApp?

    @Query("SELECT * FROM locked_apps")
    suspend fun getAllAppsDirect(): List<LockedApp>

    @Query("DELETE FROM locked_apps WHERE packageName = :packageName")
    suspend fun deleteApp(packageName: String)
}

@Dao
interface SecurityConfigDao {
    @Query("SELECT * FROM security_config WHERE id = 'global'")
    fun getConfig(): Flow<SecurityConfig?>

    @Query("SELECT * FROM security_config WHERE id = 'global'")
    suspend fun getConfigDirect(): SecurityConfig?

    @Query("SELECT * FROM security_config ORDER BY id")
    suspend fun getAllConfigsDirect(): List<SecurityConfig>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: SecurityConfig)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun initializeConfig(config: SecurityConfig): Long

    @Query("UPDATE security_config SET hashedPin = :replacement WHERE id = 'global' AND hashedPin = :expected")
    suspend fun replacePin(expected: String, replacement: String): Int
}

@Dao
interface TimelineEventDao {
    @Query("SELECT * FROM timeline_events ORDER BY timestamp DESC LIMIT 100")
    fun getEvents(): Flow<List<TimelineEvent>>

    @Query("SELECT * FROM timeline_events ORDER BY id")
    suspend fun getAllEventsDirect(): List<TimelineEvent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: TimelineEvent)

    @Query("DELETE FROM timeline_events")
    suspend fun clearAllEvents()

    @Query("DELETE FROM timeline_events WHERE id NOT IN (SELECT id FROM timeline_events ORDER BY timestamp DESC, id DESC LIMIT :limit)")
    suspend fun retainNewestEvents(limit: Int)
}

@Dao
interface IntruderSelfieDao {
    @Query("SELECT * FROM intruder_selfies ORDER BY timestamp DESC")
    fun getSelfies(): Flow<List<IntruderSelfie>>

    @Query("SELECT * FROM intruder_selfies ORDER BY id")
    suspend fun getAllSelfiesDirect(): List<IntruderSelfie>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelfie(selfie: IntruderSelfie)

    @Query("DELETE FROM intruder_selfies WHERE id = :id")
    suspend fun deleteSelfie(id: Int)

    @Query("DELETE FROM intruder_selfies")
    suspend fun clearAllSelfies()
}
