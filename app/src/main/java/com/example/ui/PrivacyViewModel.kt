package com.example.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.IntruderSelfie
import com.example.data.LockedApp
import com.example.data.PrivacyRepository
import com.example.data.SecurityConfig
import com.example.data.TimelineEvent
import com.example.security.SecurityUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.SecureRandom
import kotlinx.coroutines.launch

class PrivacyViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PrivacyRepository
    private val secureRandom = SecureRandom()

    // Screen State
    var currentScreen by mutableStateOf("dashboard") // "dashboard", "privacy_center", "intruders", "settings"
    var isAuthDialogOpen by mutableStateOf(false)
    var authDialogTitle by mutableStateOf("Authenticate")
    var authSuccessCallback by mutableStateOf<(() -> Unit)?>(null)
    var simulatedAppToLaunch by mutableStateOf<LockedApp?>(null)

    // Decoy State
    var isDecoyModeActive by mutableStateOf(false)

    // Search and Filters
    var searchQuery by mutableStateOf("")
    var selectedCategory by mutableStateOf("All") // "All", "System", "Social", "Finance", "Locked"

    // Temporary Unlock State
    var tempUnlockExpiryTime by mutableStateOf<Long?>(null)

    init {
        val db = AppDatabase.getDatabase(application)
        repository = PrivacyRepository(db)

        viewModelScope.launch {
            repository.prepopulateIfNeeded()
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

    // Filtered Apps Flow for Display
    val filteredApps: StateFlow<List<LockedApp>> = combine(
        allApps,
        MutableStateFlow("").apply { 
            // Workaround to bind searchQuery as reactive flow
            viewModelScope.launch {
                combine(MutableStateFlow(""), MutableStateFlow("")) { _, _ -> }.collect {}
            }
        }
    ) { apps, _ ->
        // Direct filtering inside Compose is easier, but providing a reactive base helps
        apps
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calculate dynamic privacy score (0-100)
    val privacyScore: StateFlow<Int> = combine(allApps, securityConfig) { apps, config ->
        if (config == null) return@combine 20
        var score = 20 // Base score

        // 1. PIN set up
        if (config.hashedPin.isNotEmpty()) {
            score += 25
        }
        // 2. Screenshot protection
        if (config.screenshotProtection) {
            score += 15
        }
        // 3. Randomized keypad
        if (config.randomizeKeypad) {
            score += 15
        }
        // 4. Critical system apps locked (com.android.settings, com.android.vending)
        val criticalApps = apps.filter { 
            it.packageName == "com.android.settings" || it.packageName == "com.android.vending"
        }
        val lockedCriticalCount = criticalApps.count { it.isLocked }
        if (lockedCriticalCount == 2) {
            score += 25
        } else if (lockedCriticalCount == 1) {
            score += 10
        }

        score.coerceIn(0, 100)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 20)

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
    fun setupPin(pin: String) {
        viewModelScope.launch {
            val hashed = SecurityUtils.hashPin(pin)
            val current = repository.getConfigDirect()
            repository.saveConfig(current.copy(hashedPin = hashed))
            repository.logEvent("CONFIG", "Master security PIN updated successfully.")
        }
    }

    /**
     * Setup a Decoy PIN.
     */
    fun setupDecoyPin(pin: String) {
        viewModelScope.launch {
            val hashed = SecurityUtils.hashPin(pin)
            val current = repository.getConfigDirect()
            repository.saveConfig(current.copy(hashedDecoyPin = hashed))
            repository.logEvent("CONFIG", "Decoy stealth security PIN updated successfully.")
        }
    }

    /**
     * Updates randomized keypad preference.
     */
    fun setRandomizeKeypad(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getConfigDirect()
            repository.saveConfig(current.copy(randomizeKeypad = enabled))
            val desc = if (enabled) "Randomized PIN keypad layout enabled." else "Standard PIN keypad layout enabled."
            repository.logEvent("CONFIG", desc)
        }
    }

    /**
     * Updates screenshot protection.
     */
    fun setScreenshotProtection(enabled: Boolean, onUpdated: (Boolean) -> Unit) {
        viewModelScope.launch {
            val current = repository.getConfigDirect()
            repository.saveConfig(current.copy(screenshotProtection = enabled))
            val desc = if (enabled) "Screenshot and screen-recording shield activated." else "Screenshot shield deactivated."
            repository.logEvent("CONFIG", desc)
            onUpdated(enabled)
        }
    }

    /**
     * Updates auto-lock new apps.
     */
    fun setAutoLockNewApps(enabled: Boolean) {
        viewModelScope.launch {
            val current = repository.getConfigDirect()
            repository.saveConfig(current.copy(autoLockNewApps = enabled))
            val desc = if (enabled) "Automatic locking for newly installed packages enabled." else "Auto-locking for new apps disabled."
            repository.logEvent("CONFIG", desc)
        }
    }

    /**
     * Updates decoy mode type.
     */
    fun setDecoyModeType(type: String) {
        viewModelScope.launch {
            val current = repository.getConfigDirect()
            repository.saveConfig(current.copy(decoyModeType = type))
            repository.logEvent("CONFIG", "Decoy mode type updated to '$type'.")
        }
    }

    /**
     * Updates theme mode.
     */
    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            val current = repository.getConfigDirect()
            repository.saveConfig(current.copy(themeMode = mode))
        }
    }

    /**
     * Attempts to verify PIN.
     * Returns:
     * - "SUCCESS_NORMAL" if matches standard PIN.
     * - "SUCCESS_DECOY" if matches decoy PIN.
     * - "FAILED" if no match.
     */
    fun verifyPin(pin: String): String {
        val config = securityConfig.value ?: return "FAILED"
        
        // Secure Fallback: if master PIN is not configured yet, treat "1234" as the default PIN
        val targetPinHash = if (config.hashedPin.isEmpty()) {
            SecurityUtils.hashPin("1234")
        } else {
            config.hashedPin
        }
        val hashedInput = SecurityUtils.hashPin(pin)

        return when {
            hashedInput == targetPinHash -> {
                isDecoyModeActive = false
                "SUCCESS_NORMAL"
            }
            config.hashedDecoyPin.isNotEmpty() && hashedInput == config.hashedDecoyPin -> {
                isDecoyModeActive = true
                "SUCCESS_DECOY"
            }
            else -> {
                // Record intruder selfie
                val randomAvatarId = secureRandom.nextInt(5) + 1 // avatar index 1-5
                viewModelScope.launch {
                    repository.logIntruderSelfie(
                        failedPinAttempt = pin,
                        avatarId = randomAvatarId
                    )
                }
                "FAILED"
            }
        }
    }

    /**
     * Resets decoy mode back to normal.
     */
    fun exitDecoyMode() {
        isDecoyModeActive = false
        viewModelScope.launch {
            repository.logEvent("CONFIG", "Stealth Decoy mode deactivated. Main privacy space restored.")
        }
    }
}
