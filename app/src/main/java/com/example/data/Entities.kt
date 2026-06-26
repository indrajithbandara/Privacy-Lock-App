package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

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

@Entity(tableName = "security_config")
data class SecurityConfig(
    @PrimaryKey val id: String = "global",
    val hashedPin: String = "",
    val hashedDecoyPin: String = "",
    val randomizeKeypad: Boolean = false,
    val screenshotProtection: Boolean = false,
    val autoLockNewApps: Boolean = false,
    val temporaryUnlockDurationMinutes: Int = 0, // 0 = disabled, otherwise 5, 10, etc.
    val decoyModeType: String = "NONE", // "NONE", "FAKE_CRASH"
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
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
