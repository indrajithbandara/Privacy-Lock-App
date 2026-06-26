package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class PrivacyRepository(private val db: AppDatabase) {

    private val appDao = db.lockedAppDao()
    private val configDao = db.securityConfigDao()
    private val eventDao = db.timelineEventDao()
    private val selfieDao = db.intruderSelfieDao()

    val allApps: Flow<List<LockedApp>> = appDao.getAllApps()
    val securityConfig: Flow<SecurityConfig?> = configDao.getConfig()
    val timelineEvents: Flow<List<TimelineEvent>> = eventDao.getEvents()
    val intruderSelfies: Flow<List<IntruderSelfie>> = selfieDao.getSelfies()

    suspend fun getApp(packageName: String): LockedApp? = appDao.getApp(packageName)

    suspend fun getConfigDirect(): SecurityConfig {
        return configDao.getConfigDirect() ?: SecurityConfig().also {
            configDao.insertConfig(it)
        }
    }

    suspend fun saveConfig(config: SecurityConfig) {
        configDao.insertConfig(config)
    }

    suspend fun toggleAppLock(packageName: String, isLocked: Boolean) {
        appDao.updateAppLockStatus(packageName, isLocked)
        val app = appDao.getApp(packageName)
        val status = if (isLocked) "locked" else "unlocked"
        logEvent(
            type = "CONFIG",
            description = "Simulated application '${app?.name ?: packageName}' has been $status.",
            appName = app?.name
        )
    }

    suspend fun toggleAppFavorite(packageName: String, isFavorite: Boolean) {
        appDao.updateAppFavoriteStatus(packageName, isFavorite)
    }

    suspend fun toggleAppPinned(packageName: String, isPinned: Boolean) {
        appDao.updateAppPinnedStatus(packageName, isPinned)
    }

    suspend fun logEvent(type: String, description: String, appName: String? = null) {
        eventDao.insertEvent(
            TimelineEvent(
                type = type,
                description = description,
                appName = appName
            )
        )
    }

    suspend fun logIntruderSelfie(failedPinAttempt: String, avatarId: Int) {
        val maskedAttempt = "*".repeat(failedPinAttempt.length).ifEmpty { "None" }
        selfieDao.insertSelfie(
            IntruderSelfie(
                failedPinAttempt = maskedAttempt,
                avatarId = avatarId
            )
        )
        logEvent(
            type = "INTRUDER",
            description = "Intruder attempt blocked! Masked entry: '$maskedAttempt'. Secure capture recorded."
        )
    }

    suspend fun deleteIntruderSelfie(id: Int) {
        selfieDao.deleteSelfie(id)
    }

    suspend fun clearAllTimeline() {
        eventDao.clearAllEvents()
    }

    suspend fun clearAllSelfies() {
        selfieDao.clearAllSelfies()
    }

    /**
     * Populates standard, simulated enterprise-grade apps if database is empty.
     */
    suspend fun prepopulateIfNeeded() {
        val currentApps = allApps.firstOrNull() ?: emptyList()
        if (currentApps.isEmpty()) {
            val defaults = listOf(
                LockedApp("com.android.settings", "Settings", "System", isLocked = true, iconName = "settings"),
                LockedApp("com.android.vending", "Google Play Store", "System", isLocked = true, iconName = "play_store"),
                LockedApp("com.android.packageinstaller", "Package Installer", "System", isLocked = true, iconName = "installer"),
                LockedApp("com.whatsapp", "WhatsApp", "Social", isLocked = false, iconName = "whatsapp"),
                LockedApp("com.discord", "Discord", "Social", isLocked = false, iconName = "discord"),
                LockedApp("com.google.android.gm", "Gmail", "Utilities", isLocked = false, iconName = "gmail"),
                LockedApp("com.google.android.apps.messaging", "Messages", "Utilities", isLocked = false, iconName = "messages"),
                LockedApp("com.android.chrome", "Chrome", "Utilities", isLocked = false, iconName = "chrome"),
                LockedApp("com.google.android.apps.nbu.paisa.user", "Google Pay", "Finance", isLocked = false, iconName = "google_pay"),
                LockedApp("com.chase.sig.android", "Chase Mobile", "Finance", isLocked = false, iconName = "chase"),
                LockedApp("com.binance.dev", "Binance", "Finance", isLocked = false, iconName = "binance"),
                LockedApp("com.google.android.apps.photos", "Photos & Gallery", "Social", isLocked = false, iconName = "gallery")
            )
            appDao.insertApps(defaults)
            logEvent("CONFIG", "Initial secure simulated sandbox environment successfully provisioned.")
        }

        // Prepopulate default config too
        val currentConfig = configDao.getConfigDirect()
        if (currentConfig == null) {
            configDao.insertConfig(SecurityConfig())
        }
    }
}
