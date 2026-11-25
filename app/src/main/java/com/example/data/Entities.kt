package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(tableName = "locked_apps")
data class LockedApp(
    @PrimaryKey val packageName: String,
    val name: String,
    val category: String, // "System", "Social", "Finance", "Utilities", "Other"
    val isLocked: Boolean = false,
    val isFavorite: Boolean = false,
    val isPinned: Boolean = false,
    val iconName: String
)

@Entity(tableName = "protection_profiles")
data class ProtectionProfile(@PrimaryKey(autoGenerate = true) val id: Long = 0, val name: String, val active: Boolean = false)

@Entity(tableName = "profile_apps", primaryKeys = ["profileId", "packageName"],
    foreignKeys = [ForeignKey(entity = ProtectionProfile::class, parentColumns = ["id"], childColumns = ["profileId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index("packageName")])
data class ProfileApp(val profileId: Long, val packageName: String)

@Entity(tableName = "security_config")
data class SecurityConfig(
    @PrimaryKey val id: String = "global",
    val hashedPin: String = "",
    val hashedDecoyPin: String = "",
    val hashedPanicPin: String = "",
    val biometricsEnabled: Boolean = false,
    val autoLockTimeoutSeconds: Int = 0,
    val randomizeKeypad: Boolean = false,
    val vibrateOnKeyPress: Boolean = true,
    val lockNewlyInstalledApps: Boolean = false,
    val intruderDetectionEnabled: Boolean = true,
    val intruderSelfieEnabled: Boolean = true,
    val failedAttemptLimit: Int = 5,
    val screenshotProtection: Boolean = false,
    val autoLockNewApps: Boolean = false,
    val temporaryUnlockDurationMinutes: Int = 0,
    val decoyModeType: String = "NONE",
    val themeMode: String = "SYSTEM",
    val stealthCode: String = "#1234"
)

@Entity(tableName = "timeline_events")
data class TimelineEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val type: String, // "LOCK", "UNLOCK", "INTRUDER", "CONFIG"
    val description: String,
    val appName: String? = null
)

@Entity(tableName = "intruder_selfies")
data class IntruderSelfie(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val failedPinAttempt: String,
    val avatarId: Int // Maps to an illustrated intruder avatar
)
