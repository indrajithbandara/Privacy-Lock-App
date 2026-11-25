package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LockedAppDao {
    @Query("SELECT EXISTS(SELECT 1 FROM locked_apps WHERE packageName = :packageName AND isLocked = 1 UNION ALL SELECT 1 FROM profile_apps JOIN protection_profiles ON profile_apps.profileId = protection_profiles.id WHERE packageName = :packageName AND active = 1)")
    suspend fun isEffectivelyLocked(packageName: String): Boolean

    @Query("SELECT packageName FROM locked_apps WHERE isLocked = 1 UNION SELECT packageName FROM profile_apps JOIN protection_profiles ON profile_apps.profileId = protection_profiles.id WHERE active = 1")
    fun getEffectiveLocks(): Flow<List<String>>

    @Query("SELECT DISTINCT packageName FROM profile_apps JOIN protection_profiles ON profile_apps.profileId = protection_profiles.id WHERE active = 1 AND forceImmediateRelock = 1")
    fun getImmediateProfileLocks(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM profile_apps JOIN protection_profiles ON profile_apps.profileId = protection_profiles.id WHERE packageName = :packageName AND active = 1 AND forceImmediateRelock = 1)")
    suspend fun isImmediateProfileLock(packageName: String): Boolean
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
interface ProtectionProfileDao {
    @Query("SELECT * FROM protection_profiles ORDER BY name, id")
    fun observeProfiles(): Flow<List<ProtectionProfile>>
    @Query("SELECT * FROM profile_apps")
    fun observeMembers(): Flow<List<ProfileApp>>
    @Query("SELECT * FROM protection_profiles WHERE id = :id")
    suspend fun get(id: Long): ProtectionProfile?
    @Insert
    suspend fun insert(profile: ProtectionProfile): Long
    @Query("UPDATE protection_profiles SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)
    @Query("UPDATE protection_profiles SET forceImmediateRelock = :enabled WHERE id = :id")
    suspend fun setImmediateRelock(id: Long, enabled: Boolean)
    @Query("UPDATE protection_profiles SET active = :active WHERE id = :id")
    suspend fun setActive(id: Long, active: Boolean)
    @Query("DELETE FROM protection_profiles WHERE id = :id")
    suspend fun delete(id: Long)
    @Query("DELETE FROM profile_apps WHERE profileId = :id")
    suspend fun clearMembers(id: Long)
    @Insert
    suspend fun insertMembers(members: List<ProfileApp>)
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
