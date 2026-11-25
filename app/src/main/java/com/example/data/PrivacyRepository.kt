package com.example.data

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.example.security.AppLockManager
import com.example.security.AuthenticationGate
import com.example.security.AuthenticationResult
import com.example.security.PinCredentials
import com.example.security.CredentialEnvelope
import com.example.security.SecurityUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class PrivacyRepository(private val db: AppDatabase, private val context: Context) {

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

    suspend fun changePrimaryPin(currentPin: String, newPin: String): Boolean {
        val configuration = getConfigDirect()
        val stored = configuration.hashedPin
        if (stored.isNotEmpty()) {
            val result = AuthenticationGate.authenticate(context, configuration.failedAttemptLimit) {
                val decoded = CredentialEnvelope.unwrap(stored) ?: return@authenticate false
                if (!PinCredentials.verify(currentPin, decoded)) return@authenticate false
                val replacement = SecurityUtils.hashPin(newPin)
                configDao.replacePin(stored, replacement) == 1
            }
            if (result.attempted && !result.accepted) logAuthenticationFailure()
            return result.accepted
        }
        val replacement = withContext(Dispatchers.Default) { SecurityUtils.hashPin(newPin) }
        return configDao.replacePin(stored, replacement) == 1
    }

    suspend fun verifyPrimaryPin(pin: String, appName: String? = null): AuthenticationResult {
        val configuration = configDao.getConfigDirect() ?: return AuthenticationResult(false)
        val stored = configuration.hashedPin
        if (stored.isEmpty()) return AuthenticationResult(false)
        val result = AuthenticationGate.authenticate(context, configuration.failedAttemptLimit) {
            val decoded = CredentialEnvelope.unwrap(stored) ?: return@authenticate false
            if (!PinCredentials.verify(pin, decoded)) return@authenticate false
            if (!CredentialEnvelope.isWrapped(stored) || PinCredentials.isLegacy(decoded)) {
                val upgraded = SecurityUtils.hashPin(pin)
                return@authenticate configDao.replacePin(stored, upgraded) == 1
            }
            configDao.getConfigDirect()?.hashedPin == stored
        }
        if (result.attempted && !result.accepted) {
            logAuthenticationFailure(appName)
            if (result.retryAfterSeconds >= 30) {
                logEvent("AUTH_LOCKOUT", "Temporary PIN authentication lockout.", appName)
            }
        }
        return result
    }

    suspend fun toggleAppLock(packageName: String, isLocked: Boolean) {
        if (isLocked && (getConfigDirect().hashedPin.isEmpty() || !AppLockManager.canProtect(context, packageName))) return
        appDao.updateAppLockStatus(packageName, isLocked)
        val app = appDao.getApp(packageName)
        val status = if (isLocked) "locked" else "unlocked"
        logEvent(
            type = "CONFIG",
            description = "Protection preference for '${app?.name ?: packageName}' changed to $status.",
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

    suspend fun logAuthenticationFailure(appName: String? = null) {
        logEvent("AUTH_FAILURE", "Incorrect PIN entered.", appName)
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
            return
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

        appDao.insertApps(newAppsList)
        val installedPackages = newAppsList.map { it.packageName }.toSet()
        for (existing in currentAppsMap.values) {
            if (existing.name.endsWith(" (Demo)") && existing.packageName !in installedPackages) {
                appDao.deleteApp(existing.packageName)
            }
        }
        logEvent("CONFIG", "Secure local privacy space synchronized with physical device apps.")
    }

    /**
     * Synchronizes device apps and initializes configuration.
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
