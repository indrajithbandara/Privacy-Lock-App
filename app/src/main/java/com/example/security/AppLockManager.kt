package com.example.security

import android.content.Context
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private var isInitialized = false
    private var autoLockTimeoutSeconds = 0

    fun init(context: Context) {
        if (isInitialized) return
        isInitialized = true
        val db = AppDatabase.getDatabase(context)
        CoroutineScope(Dispatchers.IO).launch {
            db.lockedAppDao().getAllApps().collect { apps ->
                val locked = apps.filter { it.isLocked }.map { it.packageName }.toSet()
                synchronized(lockedPackages) {
                    lockedPackages.clear()
                    lockedPackages.addAll(locked)
                }
                _lockedAppsFlow.value = locked
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            db.securityConfigDao().getConfig().collect { config ->
                if (config != null) {
                    synchronized(this@AppLockManager) {
                        autoLockTimeoutSeconds = config.autoLockTimeoutSeconds
                    }
                }
            }
        }
    }

    fun getAutoLockTimeoutSeconds(): Int {
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
            val timeout = synchronized(this@AppLockManager) { autoLockTimeoutSeconds }
            if (timeout > 0) {
                val unlockTime = unlockedTimestamps[packageName] ?: return false
                val elapsed = (System.currentTimeMillis() - unlockTime) / 1000
                if (elapsed > timeout) {
                    temporarilyUnlockedPackages.remove(packageName)
                    unlockedTimestamps.remove(packageName)
                    return false
                }
            }
            return true
        }
    }

    fun temporarilyUnlockPackage(packageName: String) {
        synchronized(temporarilyUnlockedPackages) {
            temporarilyUnlockedPackages.add(packageName)
            unlockedTimestamps[packageName] = System.currentTimeMillis()
        }
    }

    fun lockPackage(packageName: String) {
        synchronized(temporarilyUnlockedPackages) {
            temporarilyUnlockedPackages.remove(packageName)
            unlockedTimestamps.remove(packageName)
        }
    }

    fun clearTemporaryUnlocks() {
        synchronized(temporarilyUnlockedPackages) {
            temporarilyUnlockedPackages.clear()
            unlockedTimestamps.clear()
        }
    }
}
