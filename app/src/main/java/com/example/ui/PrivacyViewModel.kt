package com.example.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.IntruderSelfie
import com.example.data.LockedApp
import com.example.data.PrivacyRepository
import com.example.data.SecurityConfig
import com.example.data.TimelineEvent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "privacy_settings")
private val SCREENSHOT_PROTECTION_KEY = booleanPreferencesKey("screenshot_protection")

class PrivacyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PrivacyRepository

    // Screen State
    var currentScreen by mutableStateOf("dashboard") // "dashboard", "privacy_center", "intruders", "settings"

    // Search and Filters
    var searchQuery by mutableStateOf("")
    var selectedCategory by mutableStateOf("All") // "All", "System", "Social", "Finance", "Locked"


    // Screenshot Protection Live State Flow from DataStore (Default: true / ON)
    val isScreenshotProtectionEnabled: StateFlow<Boolean> = application.applicationContext.dataStore.data
        .map { preferences ->
            preferences[SCREENSHOT_PROTECTION_KEY] ?: true
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PrivacyRepository(db, application.applicationContext)

        viewModelScope.launch {
            repository.prepopulateIfNeeded(application)
            
            // Sync DataStore preference with Room database SecurityConfig on launch
            try {
                val dataStoreEnabled = application.applicationContext.dataStore.data
                    .map { preferences -> preferences[SCREENSHOT_PROTECTION_KEY] ?: true }
                    .first()
                val current = repository.getConfigDirect()
                if (current.screenshotProtection != dataStoreEnabled) {
                    repository.updateConfig { it.copy(screenshotProtection = dataStoreEnabled) }
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Scan and sync currently installed physical applications on the device.
     */
    fun refreshInstalledApps() {
        viewModelScope.launch {
            repository.syncInstalledApps(getApplication())
        }
    }

    // Main Flows from DB
    val allApps: StateFlow<List<LockedApp>> = repository.allApps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val securityConfig: StateFlow<SecurityConfig?> = repository.securityConfig
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val timelineEvents: StateFlow<List<TimelineEvent>> = repository.timelineEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val intruderSelfies: StateFlow<List<IntruderSelfie>> = repository.intruderSelfies
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profiles = repository.profiles.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val profileMembers = repository.profileMembers.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val effectiveLocks = repository.effectiveLocks.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveProfile(id: Long?, name: String, packages: Set<String>, immediateRelock: Boolean, result: (Boolean) -> Unit) = profileChange(result) {
        repository.saveProfile(id, name, packages, immediateRelock)
    }
    fun setProfileActive(id: Long, active: Boolean, result: (Boolean) -> Unit) = profileChange(result) {
        repository.setProfileActive(id, active)
    }
    fun deleteProfile(id: Long, result: (Boolean) -> Unit) = profileChange(result) { repository.deleteProfile(id) }
    private fun profileChange(result: (Boolean) -> Unit, action: suspend () -> Unit) {
        viewModelScope.launch {
            try { action(); result(true) }
            catch (cancelled: kotlinx.coroutines.CancellationException) { throw cancelled }
            catch (_: Exception) { result(false) }
        }
    }



    // --- Actions ---

    fun toggleAppLock(packageName: String, isLocked: Boolean) {
        viewModelScope.launch {
            repository.toggleAppLock(packageName, isLocked)
        }
    }

    fun toggleAppFavorite(packageName: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleAppFavorite(packageName, isFavorite)
        }
    }

    fun toggleAppPinned(packageName: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.toggleAppPinned(packageName, isPinned)
        }
    }

    fun clearTimeline() {
        viewModelScope.launch {
            repository.clearAllTimeline()
        }
    }

    var eventRetentionLimit by mutableStateOf(0)
        private set

    fun loadEventRetention() {
        eventRetentionLimit = repository.eventRetentionLimit()
    }

    fun setEventRetention(limit: Int, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                repository.setEventRetentionLimit(limit)
                eventRetentionLimit = limit
                onResult(true)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                onResult(false)
            }
        }
    }

    fun deleteSelfie(id: Int) {
        viewModelScope.launch {
            repository.deleteIntruderSelfie(id)
        }
    }

    fun clearAllSelfies() {
        viewModelScope.launch {
            repository.clearAllSelfies()
        }
    }

    /**
     * Setup a new standard PIN.
     */
    fun setupPin(currentPin: String, newPin: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = try {
                repository.changePrimaryPin(currentPin, newPin)
            } catch (_: Exception) {
                false
            }
            onComplete(success)
        }
    }


    /**
     * Updates randomized keypad preference.
     */
    fun setRandomizeKeypad(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateConfig { it.copy(randomizeKeypad = enabled) }
            val desc = if (enabled) "Randomized PIN keypad layout enabled." else "Standard PIN keypad layout enabled."
            repository.logEvent("CONFIG", desc)
        }
    }

    /**
     * Updates screenshot protection.
     */
    fun setScreenshotProtection(enabled: Boolean, onUpdated: (Boolean) -> Unit) {
        viewModelScope.launch {
            // Write to DataStore
            getApplication<Application>().applicationContext.dataStore.edit { preferences ->
                preferences[SCREENSHOT_PROTECTION_KEY] = enabled
            }
            repository.updateConfig { it.copy(screenshotProtection = enabled) }
            val desc = if (enabled) "Screenshot and screen-recording shield activated." else "Screenshot shield deactivated."
            repository.logEvent("CONFIG", desc)
            onUpdated(enabled)
        }
    }



    /**
     * Updates biometrics enabled preference.
     */
    fun setBiometricsEnabled(enabled: Boolean, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val current = repository.getConfigDirect()
                if (enabled) {
                    check(current.hashedPin.isNotEmpty())
                    check(androidx.biometric.BiometricManager.from(getApplication<Application>())
                        .canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG) == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS)
                    withContext(Dispatchers.IO) { com.example.security.BiometricKey.enroll() }
                } else {
                    withContext(Dispatchers.IO) { com.example.security.BiometricKey.remove() }
                }
                repository.updateConfig { it.copy(biometricsEnabled = enabled) }
                repository.logEvent("CONFIG", if (enabled) "Biometric authentication enabled." else "Biometric authentication disabled.")
                onResult(true)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) { onResult(false) }
        }
    }

    /**
     * Updates auto-lock timeout.
     */
    fun setAutoLockTimeoutSeconds(seconds: Int) {
        viewModelScope.launch {
            require(seconds in setOf(-1, 0, 15, 30, 60, 300, 600))
            repository.updateConfig { it.copy(autoLockTimeoutSeconds = seconds) }
            val label = when (seconds) {
                0 -> "Immediately"
                15 -> "15 seconds"
                30 -> "30 seconds"
                60 -> "1 minute"
                300 -> "5 minutes"
                600 -> "10 minutes"
                else -> "Screen Off only"
            }
            repository.logEvent("CONFIG", "Auto lock timeout changed to $label.")
        }
    }

    /**
     * Updates vibrate on key press.
     */
    fun setVibrateOnKeyPress(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateConfig { it.copy(vibrateOnKeyPress = enabled) }
        }
    }

    /**
     * Updates lock newly installed apps.
     */
    fun setLockNewlyInstalledApps(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateConfig { it.copy(lockNewlyInstalledApps = enabled, autoLockNewApps = enabled) }
            val desc = if (enabled) "Auto-lock for future newly installed apps activated." else "Auto-lock for newly installed apps deactivated."
            repository.logEvent("CONFIG", desc)
        }
    }



    /**
     * Updates failed attempt limit.
     */
    fun setFailedAttemptLimit(limit: Int) {
        viewModelScope.launch {
            require(limit in setOf(3, 5, 10))
            repository.updateConfig { it.copy(failedAttemptLimit = limit) }
            repository.logEvent("CONFIG", "Failed PIN entry limit threshold set to $limit attempts.")
        }
    }


    /**
     * Serializes current SecurityConfig to a backup string format.
     */
    fun exportBackup(onExport: (String) -> Unit) {
        viewModelScope.launch {
            val config = repository.getConfigDirect()
            val backup = "themeMode:${config.themeMode};vibrateOnKeyPress:${config.vibrateOnKeyPress}"
            onExport(backup)
        }
    }

    /**
     * Restores SecurityConfig from an exported backup string format.
     */
    fun restoreBackup(backupStr: String, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val parts = backupStr.split(";")
                var config = repository.getConfigDirect()
                for (part in parts) {
                    val kv = part.split(":")
                    if (kv.size == 2) {
                        val key = kv[0]
                        val value = kv[1]
                        config = when (key) {
                            "themeMode" -> {
                                require(value in setOf("SYSTEM", "LIGHT", "DARK", "DYNAMIC"))
                                config.copy(themeMode = value)
                            }
                            "vibrateOnKeyPress" -> {
                                require(value == "true" || value == "false")
                                config.copy(vibrateOnKeyPress = value.toBoolean())
                            }
                            else -> config
                        }
                    }
                }
                repository.updateConfig { it.copy(themeMode = config.themeMode, vibrateOnKeyPress = config.vibrateOnKeyPress) }
                repository.logEvent("CONFIG", "Privacy space configurations restored successfully from local backup.")
                onComplete(true)
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }

    /**
     * Updates theme mode.
     */
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            require(mode in setOf("SYSTEM", "LIGHT", "DARK", "DYNAMIC"))
            repository.updateConfig { it.copy(themeMode = mode) }
        }
    }

    /**
     * Attempts to verify PIN.
     * Returns:
     * - "SUCCESS_NORMAL" if matches standard PIN.
     * - "FAILED" if no match.
     */
    suspend fun verifyPin(pin: String): String {
        val result = repository.verifyPrimaryPin(pin)
        if (result.accepted) {
            return "SUCCESS_NORMAL"
        }
        return if (result.retryAfterSeconds > 0) "LOCKED:${result.retryAfterSeconds}" else "FAILED"
    }

}
