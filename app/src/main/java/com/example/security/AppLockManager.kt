package com.example.security

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object AppLockManager {
    // Keeps track of currently unlocked packages temporarily (while session is alive)
    private val temporarilyUnlockedPackages = mutableSetOf<String>()
    
    // Tracks when each package was last unlocked to enforce timeouts
    private val unlockedTimestamps = mutableMapOf<String, Long>()

    // Quick cache of locked packages to avoid querying DB on every window change event
    private val lockedPackages = mutableSetOf<String>()

    private val _lockedAppsFlow = MutableStateFlow<Set<String>>(emptySet())
    val lockedAppsFlow = _lockedAppsFlow.asStateFlow()
    private val _serviceConnected = MutableStateFlow(false)
    val serviceConnected = _serviceConnected.asStateFlow()
    private val _sessionRevision = MutableStateFlow(0L)
    val sessionRevision = _sessionRevision.asStateFlow()

    fun reportServiceConnected(connected: Boolean): Unit = synchronized(foregroundStateLock) {
        _serviceConnected.value = connected
        if (!connected) clearTemporaryUnlocks()
    }

    private var isInitialized = false
    private var autoLockTimeoutSeconds = 0
    @Volatile private var immediateProfilePackages: Set<String> = emptySet()

    @Volatile private var foregroundTarget: String? = null
    @Volatile private var visibleLockPrompt: String? = null
    private val foregroundStateLock = Any()

    fun setForegroundTarget(packageName: String?): Unit = synchronized(foregroundStateLock) {
        val previous = foregroundTarget
        foregroundTarget = packageName
        if (previous != null && previous != packageName && getAutoLockTimeoutSeconds(previous) == 0) {
            lockPackage(previous)
        }
        if (previous != packageName) _sessionRevision.update { it + 1 }
    }

    fun isForegroundTarget(packageName: String): Boolean = foregroundTarget == packageName

    /** The lock activity belongs to this app; its internal window events are not app switches. */
    internal fun reportLockPromptVisible(packageName: String?) {
        visibleLockPrompt = packageName
    }

    internal fun hasVisibleLockPrompt(): Boolean = visibleLockPrompt != null

    /** A completed authentication is valid only for the still-current foreground request. */
    fun grantForegroundSession(packageName: String): Boolean = synchronized(foregroundStateLock) {
        if (foregroundTarget != packageName || !_serviceConnected.value) return@synchronized false
        temporarilyUnlockPackage(packageName)
        true
    }

    /** Keep owner controls and system functions outside the locker's scope. */
    fun canProtect(context: Context, packageName: String): Boolean {
        if (packageName == context.packageName) return false
        return try {
            val pm = context.packageManager
            val info = pm.getApplicationInfo(packageName, 0)
            if ((info.flags and ApplicationInfo.FLAG_SYSTEM) != 0) return false
            if (pm.getLaunchIntentForPackage(packageName) == null) return false
            val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            pm.queryIntentActivities(home, PackageManager.MATCH_DEFAULT_ONLY)
                .none { it.activityInfo.packageName == packageName }
        } catch (_: Exception) { false }
    }

    @Synchronized
    fun init(context: Context) {
        if (isInitialized) return
        val db = AppDatabase.getDatabase(context)
        isInitialized = true
        CoroutineScope(Dispatchers.IO).launch {
            db.lockedAppDao().getEffectiveLocks().collect { apps ->
                val locked = apps.toSet()
                synchronized(lockedPackages) {
                    lockedPackages.clear()
                    lockedPackages.addAll(locked)
                }
                if (_lockedAppsFlow.value != locked) clearTemporaryUnlocks()
                _lockedAppsFlow.value = locked
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            db.lockedAppDao().getImmediateProfileLocks().collect { packages ->
                val updated = packages.toSet()
                if (immediateProfilePackages != updated) {
                    immediateProfilePackages = updated
                    clearTemporaryUnlocks()
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            db.securityConfigDao().getConfig().collect { config ->
                if (config != null) {
                    val changed = synchronized(this@AppLockManager) {
                        val changed = autoLockTimeoutSeconds != config.autoLockTimeoutSeconds
                        autoLockTimeoutSeconds = config.autoLockTimeoutSeconds
                        changed
                    }
                    if (changed) clearTemporaryUnlocks()
                }
            }
        }
    }

    fun getAutoLockTimeoutSeconds(packageName: String? = null): Int {
        if (packageName != null && packageName in immediateProfilePackages) return 0
        return synchronized(this) {
            autoLockTimeoutSeconds
        }
    }

    fun isPackageLocked(packageName: String): Boolean {
        return synchronized(lockedPackages) {
            lockedPackages.contains(packageName)
        }
    }

    fun isPackageTemporarilyUnlocked(packageName: String): Boolean {
        return synchronized(temporarilyUnlockedPackages) {
            if (!temporarilyUnlockedPackages.contains(packageName)) return false
            val timeout = getAutoLockTimeoutSeconds(packageName)
            if (timeout > 0) {
                val unlockTime = unlockedTimestamps[packageName] ?: return false
                val elapsed = (android.os.SystemClock.elapsedRealtime() - unlockTime) / 1000
                if (elapsed >= timeout) {
                    temporarilyUnlockedPackages.remove(packageName)
                    unlockedTimestamps.remove(packageName)
                    return false
                }
            }
            return true
        }
    }

    internal fun temporarilyUnlockPackage(packageName: String) {
        synchronized(temporarilyUnlockedPackages) {
            temporarilyUnlockedPackages.add(packageName)
            unlockedTimestamps[packageName] = android.os.SystemClock.elapsedRealtime()
        }
        _sessionRevision.update { it + 1 }
    }

    /** Remaining time for an existing timed session, independent of wall-clock changes. */
    fun remainingSessionMillis(packageName: String): Long? = synchronized(temporarilyUnlockedPackages) {
        val timeout = getAutoLockTimeoutSeconds(packageName)
        if (timeout <= 0 || packageName !in temporarilyUnlockedPackages) return@synchronized null
        val started = unlockedTimestamps[packageName] ?: return@synchronized null
        (timeout * 1000L - (android.os.SystemClock.elapsedRealtime() - started)).coerceAtLeast(0L)
    }

    fun lockPackage(packageName: String) {
        synchronized(temporarilyUnlockedPackages) {
            temporarilyUnlockedPackages.remove(packageName)
            unlockedTimestamps.remove(packageName)
        }
        _sessionRevision.update { it + 1 }
    }

    fun clearTemporaryUnlocks() {
        synchronized(temporarilyUnlockedPackages) {
            temporarilyUnlockedPackages.clear()
            unlockedTimestamps.clear()
        }
        _sessionRevision.update { it + 1 }
    }
}
