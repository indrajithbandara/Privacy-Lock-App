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
    
    // Quick cache of locked packages to avoid querying DB on every window change event
    private val lockedPackages = mutableSetOf<String>()

    private val _lockedAppsFlow = MutableStateFlow<Set<String>>(emptySet())
    val lockedAppsFlow = _lockedAppsFlow.asStateFlow()

    private var isInitialized = false

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
    }

    fun isPackageLocked(packageName: String): Boolean {
        return synchronized(lockedPackages) {
            lockedPackages.contains(packageName)
        }
    }

    fun isPackageTemporarilyUnlocked(packageName: String): Boolean {
        return synchronized(temporarilyUnlockedPackages) {
            temporarilyUnlockedPackages.contains(packageName)
        }
    }

    fun temporarilyUnlockPackage(packageName: String) {
        synchronized(temporarilyUnlockedPackages) {
            temporarilyUnlockedPackages.add(packageName)
        }
    }

    fun lockPackage(packageName: String) {
        synchronized(temporarilyUnlockedPackages) {
            temporarilyUnlockedPackages.remove(packageName)
        }
    }

    fun clearTemporaryUnlocks() {
        synchronized(temporarilyUnlockedPackages) {
            temporarilyUnlockedPackages.clear()
        }
    }
}
