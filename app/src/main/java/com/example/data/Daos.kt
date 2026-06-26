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

    @Query("UPDATE locked_apps SET isLocked = :isLocked WHERE packageName = :packageName")
    suspend fun updateAppLockStatus(packageName: String, isLocked: Boolean)

    @Query("UPDATE locked_apps SET isFavorite = :isFavorite WHERE packageName = :packageName")
    suspend fun updateAppFavoriteStatus(packageName: String, isFavorite: Boolean)

    @Query("UPDATE locked_apps SET isPinned = :isPinned WHERE packageName = :packageName")
    suspend fun updateAppPinnedStatus(packageName: String, isPinned: Boolean)

    @Query("SELECT * FROM locked_apps WHERE packageName = :packageName")
    suspend fun getApp(packageName: String): LockedApp?
}

@Dao
interface SecurityConfigDao {
    @Query("SELECT * FROM security_config WHERE id = 'global'")
    fun getConfig(): Flow<SecurityConfig?>

    @Query("SELECT * FROM security_config WHERE id = 'global'")
    suspend fun getConfigDirect(): SecurityConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: SecurityConfig)
}

@Dao
interface TimelineEventDao {
    @Query("SELECT * FROM timeline_events ORDER BY timestamp DESC LIMIT 100")
    fun getEvents(): Flow<List<TimelineEvent>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: TimelineEvent)

    @Query("DELETE FROM timeline_events")
    suspend fun clearAllEvents()
}

@Dao
interface IntruderSelfieDao {
    @Query("SELECT * FROM intruder_selfies ORDER BY timestamp DESC")
    fun getSelfies(): Flow<List<IntruderSelfie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelfie(selfie: IntruderSelfie)

    @Query("DELETE FROM intruder_selfies WHERE id = :id")
    suspend fun deleteSelfie(id: Int)

    @Query("DELETE FROM intruder_selfies")
    suspend fun clearAllSelfies()
}
