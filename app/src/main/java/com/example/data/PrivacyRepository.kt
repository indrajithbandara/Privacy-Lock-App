package com.example.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
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

    private fun getCategoryForApp(appInfo: android.content.pm.ApplicationInfo): String {
        if ((appInfo.flags and android.content.pm.ApplicationInfo.FLAG_SYSTEM) != 0) {
            return "System"
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            return when (appInfo.category) {
                android.content.pm.ApplicationInfo.CATEGORY_SOCIAL -> "Social"
                android.content.pm.ApplicationInfo.CATEGORY_GAME -> "Games"
                android.content.pm.ApplicationInfo.CATEGORY_NEWS -> "News"
                android.content.pm.ApplicationInfo.CATEGORY_MAPS -> "Maps"
                android.content.pm.ApplicationInfo.CATEGORY_IMAGE,
                android.content.pm.ApplicationInfo.CATEGORY_VIDEO,
                android.content.pm.ApplicationInfo.CATEGORY_AUDIO -> "Media"
                android.content.pm.ApplicationInfo.CATEGORY_PRODUCTIVITY -> "Finance"
                else -> "Utilities"
            }
        }
        return "Utilities"
    }

    private fun getFallbackIconName(packageName: String): String {
        return when {
            packageName.contains("whatsapp", ignoreCase = true) -> "whatsapp"
            packageName.contains("discord", ignoreCase = true) -> "discord"
            packageName.contains("gmail", ignoreCase = true) -> "gmail"
            packageName.contains("message", ignoreCase = true) -> "messages"
            packageName.contains("chrome", ignoreCase = true) -> "chrome"
            packageName.contains("pay", ignoreCase = true) || packageName.contains("wallet", ignoreCase = true) -> "google_pay"
            packageName.contains("chase", ignoreCase = true) -> "chase"
            packageName.contains("binance", ignoreCase = true) -> "binance"
            packageName.contains("photo", ignoreCase = true) || packageName.contains("gallery", ignoreCase = true) -> "gallery"
            packageName.contains("setting", ignoreCase = true) -> "settings"
            packageName.contains("play", ignoreCase = true) || packageName.contains("vending", ignoreCase = true) -> "play_store"
            else -> "android"
        }
    }

    suspend fun syncInstalledApps(context: Context) {
        val pm = context.packageManager
        val installedApps = try {
            pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList()
        }

        val currentAppsMap = appDao.getAllAppsDirect().associateBy { it.packageName }
        val newAppsList = mutableListOf<LockedApp>()

        for (appInfo in installedApps) {
            // Skip our own app package to avoid infinite blocking loops
            if (appInfo.packageName == context.packageName) {
                continue
            }

            val name = appInfo.loadLabel(pm).toString()
            val category = getCategoryForApp(appInfo)

            val existing = currentAppsMap[appInfo.packageName]
            val isLocked = existing?.isLocked ?: false
            val isFavorite = existing?.isFavorite ?: false
            val isPinned = existing?.isPinned ?: false
            val iconName = existing?.iconName ?: getFallbackIconName(appInfo.packageName)

            newAppsList.add(
                LockedApp(
                    packageName = appInfo.packageName,
                    name = name,
                    category = category,
                    isLocked = isLocked,
                    isFavorite = isFavorite,
                    isPinned = isPinned,
                    iconName = iconName
                )
            )
        }

        // Include simulated defaults if they are not already there (so the interactive sandbox works seamlessly)
        val defaults = listOf(
            LockedApp("com.whatsapp", "WhatsApp (Demo)", "Social", isLocked = false, iconName = "whatsapp"),
            LockedApp("com.discord", "Discord (Demo)", "Social", isLocked = false, iconName = "discord"),
            LockedApp("com.google.android.gm", "Gmail (Demo)", "Utilities", isLocked = false, iconName = "gmail"),
            LockedApp("com.google.android.apps.messaging", "Messages (Demo)", "Utilities", isLocked = false, iconName = "messages"),
            LockedApp("com.android.chrome", "Chrome (Demo)", "Utilities", isLocked = false, iconName = "chrome"),
            LockedApp("com.google.android.apps.nbu.paisa.user", "Google Pay (Demo)", "Finance", isLocked = false, iconName = "google_pay"),
            LockedApp("com.chase.sig.android", "Chase Mobile (Demo)", "Finance", isLocked = false, iconName = "chase"),
            LockedApp("com.binance.dev", "Binance (Demo)", "Finance", isLocked = false, iconName = "binance"),
            LockedApp("com.google.android.apps.photos", "Photos & Gallery (Demo)", "Social", isLocked = false, iconName = "gallery")
        )

        for (demo in defaults) {
            if (!currentAppsMap.containsKey(demo.packageName)) {
                newAppsList.add(demo)
            }
        }

        appDao.insertApps(newAppsList)
        logEvent("CONFIG", "Secure local privacy space synchronized with physical device apps.")
    }

    /**
     * Populates standard, simulated enterprise-grade apps if database is empty, and syncs device apps.
     */
    suspend fun prepopulateIfNeeded(context: Context) {
        // Run full sync with installed apps
        syncInstalledApps(context)

        // Prepopulate default config too
        val currentConfig = configDao.getConfigDirect()
        if (currentConfig == null) {
            configDao.insertConfig(SecurityConfig())
        }
    }
}
