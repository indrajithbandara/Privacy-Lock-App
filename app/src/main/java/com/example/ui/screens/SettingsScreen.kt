package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.SecurityConfig
import com.example.ui.PrivacyViewModel
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: PrivacyViewModel,
    modifier: Modifier = Modifier,
    onScreenshotShieldToggle: (Boolean) -> Unit
) {
    val config by viewModel.securityConfig.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var showPinSetup by remember { mutableStateOf(false) }
    var pinSetupType by remember { mutableStateOf("standard") } // "standard", "decoy", "panic"
    var pinSetupInput by remember { mutableStateOf("") }
    var pinSetupError by remember { mutableStateOf<String?>(null) }

    var showTimeoutSelector by remember { mutableStateOf(false) }
    var showLimitSelector by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    
    var backupStringInput by remember { mutableStateOf("") }
    var showBackupCode by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        item {
            Text(
                text = "Privacy Preferences",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        // Credentials Configuration
        item {
            Text(
                text = "Security Credentials",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Standard PIN Config
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimarySage.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = PrimarySage, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Primary Vault PIN", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(
                                text = if (config?.hashedPin?.isNotEmpty() == true) "Secure 6-digit PIN configured" else "Default PIN (123456) active. Setup required.",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (config?.hashedPin?.isNotEmpty() == true) SoftGreenAccent else MutedRedAccent
                            )
                        }
                        Button(
                            onClick = {
                                pinSetupType = "standard"
                                pinSetupInput = ""
                                pinSetupError = null
                                showPinSetup = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text("Setup")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                    // Decoy PIN Config
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SoftAmberAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Masks, contentDescription = null, tint = SoftAmberAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Stealth Decoy PIN", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(
                                text = if (config?.hashedDecoyPin?.isNotEmpty() == true) "Decoy PIN active" else "Optional. Set 6-digit fake entry.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextWarmGray
                            )
                        }
                        Button(
                            onClick = {
                                pinSetupType = "decoy"
                                pinSetupInput = ""
                                pinSetupError = null
                                showPinSetup = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text("Configure")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                    // Panic PIN Config
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MutedRedAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MutedRedAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Panic Security PIN", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(
                                text = if (config?.hashedPanicPin?.isNotEmpty() == true) "Panic PIN armed" else "Instant emergency close PIN.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextWarmGray
                            )
                        }
                        Button(
                            onClick = {
                                pinSetupType = "panic"
                                pinSetupInput = ""
                                pinSetupError = null
                                showPinSetup = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text("Arm")
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                    // Biometrics Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimarySage.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Fingerprint, contentDescription = null, tint = PrimarySage, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Biometric Authentication", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "Fingerprint / Face Unlock bypass", style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }
                        val biometricEnabled = config?.biometricsEnabled == true
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.setBiometricsEnabled(enabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // Lock Customizations & Options
        item {
            Text(
                text = "Lock Configuration",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Auto-Lock Timeout Selector
                    val currentTimeout = config?.autoLockTimeoutSeconds ?: 0
                    val timeoutLabel = when (currentTimeout) {
                        0 -> "Immediately"
                        15 -> "15 seconds"
                        30 -> "30 seconds"
                        60 -> "1 minute"
                        300 -> "5 minutes"
                        600 -> "10 minutes"
                        else -> "Screen Off only"
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimeoutSelector = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimarySage.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = PrimarySage, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Auto Lock Timeout", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "Current: $timeoutLabel", style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextWarmGray)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                    // Lock Newly Installed Apps
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SoftAmberAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AppRegistration, contentDescription = null, tint = SoftAmberAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Lock New Apps", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "Auto-lock future app installations", style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }
                        val lockNewActive = config?.lockNewlyInstalledApps == true
                        Switch(
                            checked = lockNewActive,
                            onCheckedChange = { active ->
                                viewModel.setLockNewlyInstalledApps(active)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                    // Vibrate on Key Press
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimarySage.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Vibration, contentDescription = null, tint = PrimarySage, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Haptic Vibration", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "Vibrate on secure keypad inputs", style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }
                        val vibrateActive = config?.vibrateOnKeyPress == true
                        Switch(
                            checked = vibrateActive,
                            onCheckedChange = { active ->
                                viewModel.setVibrateOnKeyPress(active)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        // Advanced Guard Protections
        item {
            Text(
                text = "Advanced Shields",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Screenshot Shield
                    val screenshotEnabled by viewModel.isScreenshotProtectionEnabled.collectAsStateWithLifecycle()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MutedRedAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Block, contentDescription = null, tint = MutedRedAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Screenshot Shield", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "Blocks system screen captures", style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }
                        Switch(
                            checked = screenshotEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.setScreenshotProtection(enabled, onScreenshotShieldToggle)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("screenshot_shield_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                    // Randomized Keypad layout
                    val randomizeEnabled = config?.randomizeKeypad == true
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimarySage.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.GridOn, contentDescription = null, tint = PrimarySage, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Randomized Keypad", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "Shuffles PIN keyboard layout", style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }
                        Switch(
                            checked = randomizeEnabled,
                            onCheckedChange = { enabled -> viewModel.setRandomizeKeypad(enabled) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("random_keypad_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                    // Decoy Mode selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SoftAmberAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, tint = SoftAmberAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Decoy Lock screen", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "Simulate mock app stops after failures", style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }
                        val decoyModeActive = config?.decoyModeType == "FAKE_CRASH"
                        Switch(
                            checked = decoyModeActive,
                            onCheckedChange = { active ->
                                viewModel.setDecoyModeType(if (active) "FAKE_CRASH" else "NONE")
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("decoy_crash_switch")
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                    // Intruder Detection Threshold
                    val failedLimit = config?.failedAttemptLimit ?: 5
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showLimitSelector = true },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MutedRedAccent.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PersonSearch, contentDescription = null, tint = MutedRedAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Intruder Attempt Limit", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "Current: $failedLimit failed attempts", style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextWarmGray)
                    }
                }
            }
        }

        // Appearance Configuration
        item {
            Text(
                text = "Aesthetic Styles",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val currentTheme = config?.themeMode ?: "SYSTEM"

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Visual Interface Vibe", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "Light & dark theme switching", style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }

                        // Theme Mode buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("SYSTEM", "LIGHT", "DARK").forEach { mode ->
                                val selected = currentTheme == mode
                                Button(
                                    onClick = { viewModel.setThemeMode(mode) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier.height(34.dp)
                                ) {
                                    Text(
                                        text = mode,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 11.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Local Storage Backup & Restore Settings
        item {
            Text(
                text = "Backup & Recovery",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimarySage.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = PrimarySage, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Security Configuration Backup", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = "Export or restore secure credentials and locks", style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }
                        Button(
                            onClick = {
                                viewModel.exportBackup { backup ->
                                    showBackupCode = backup
                                    backupStringInput = backup
                                    showBackupRestoreDialog = true
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text("Manage")
                        }
                    }
                }
            }
        }
    }

    // PIN Setup Dialog
    if (showPinSetup) {
        Dialog(onDismissRequest = { showPinSetup = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    val setupTitle = when (pinSetupType) {
                        "standard" -> "Set Primary 6-Digit PIN"
                        "decoy" -> "Set Stealth Decoy PIN"
                        else -> "Set Emergency Panic PIN"
                    }
                    val setupDesc = when (pinSetupType) {
                        "standard" -> "Please set a secure 6-digit code to guard your applications and privacy space."
                        "decoy" -> "Set a 6-digit code that opens a fake crash screen to fool snoops when forced to unlock."
                        else -> "Set a 6-digit code that instantly closes the app and goes home in case of emergency."
                    }

                    Text(
                        text = setupTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = setupDesc,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWarmGray,
                        textAlign = TextAlign.Center
                    )

                    OutlinedTextField(
                        value = pinSetupInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                pinSetupInput = it
                                pinSetupError = null
                            }
                        },
                        placeholder = { Text("Enter 6 digits PIN") },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_setup_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    if (pinSetupError != null) {
                        Text(
                            text = pinSetupError!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showPinSetup = false },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = {
                                if (pinSetupInput.length != 6) {
                                    pinSetupError = "PIN must be exactly 6 digits."
                                } else {
                                    when (pinSetupType) {
                                        "standard" -> viewModel.setupPin(pinSetupInput)
                                        "decoy" -> viewModel.setupDecoyPin(pinSetupInput)
                                        else -> viewModel.setupPanicPin(pinSetupInput)
                                    }
                                    showPinSetup = false
                                    Toast.makeText(context, "Credentials Updated Successfully!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Confirm")
                        }
                    }
                }
            }
        }
    }

    // Auto Lock Timeout Selector Dialog
    if (showTimeoutSelector) {
        AlertDialog(
            onDismissRequest = { showTimeoutSelector = false },
            title = { Text("Select Auto Lock Timeout", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val timeouts = listOf(
                        0 to "Immediately",
                        15 to "15 seconds",
                        30 to "30 seconds",
                        60 to "1 minute",
                        300 to "5 minutes",
                        600 to "10 minutes",
                        -1 to "Screen Off only"
                    )
                    timeouts.forEach { (sec, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAutoLockTimeoutSeconds(sec)
                                    showTimeoutSelector = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (config?.autoLockTimeoutSeconds ?: 0) == sec,
                                onClick = {
                                    viewModel.setAutoLockTimeoutSeconds(sec)
                                    showTimeoutSelector = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimeoutSelector = false }) {
                    Text("Dismiss")
                }
            }
        )
    }

    // Intruder Failed Attempt Limit Dialog
    if (showLimitSelector) {
        AlertDialog(
            onDismissRequest = { showLimitSelector = false },
            title = { Text("Select Failed Attempt Limit", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val limits = listOf(3, 5, 10)
                    limits.forEach { limit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setFailedAttemptLimit(limit)
                                    showLimitSelector = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (config?.failedAttemptLimit ?: 5) == limit,
                                onClick = {
                                    viewModel.setFailedAttemptLimit(limit)
                                    showLimitSelector = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$limit attempts", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLimitSelector = false }) {
                    Text("Dismiss")
                }
            }
        )
    }

    // Backup & Restore Dialog
    if (showBackupRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showBackupRestoreDialog = false },
            title = { Text("Manage Configuration Backup", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Exported Backup Key (Keep this secret and secure):",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWarmGray
                    )

                    OutlinedTextField(
                        value = showBackupCode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Backup Configuration Key") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                    Text(
                        "To restore configurations, paste your backup key below and tap Restore:",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWarmGray
                    )

                    OutlinedTextField(
                        value = backupStringInput,
                        onValueChange = { backupStringInput = it },
                        label = { Text("Paste Backup Key") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreBackup(backupStringInput) { success ->
                            if (success) {
                                Toast.makeText(context, "Configurations Restored Successfully!", Toast.LENGTH_SHORT).show()
                                showBackupRestoreDialog = false
                            } else {
                                Toast.makeText(context, "Invalid Backup Key Format!", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupRestoreDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
