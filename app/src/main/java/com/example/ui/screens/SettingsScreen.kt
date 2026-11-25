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
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.SecurityConfig
import com.example.ui.PrivacyViewModel
import com.example.ui.components.SecureAuthDialog
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    viewModel: PrivacyViewModel,
    modifier: Modifier = Modifier,
    onScreenshotShieldToggle: (Boolean) -> Unit
) {
    val config by viewModel.securityConfig.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) { viewModel.loadEventRetention() }
    var pendingRetention by remember { mutableStateOf<Int?>(null) }
    var pendingSecurityChange by remember { mutableStateOf<(() -> Unit)?>(null) }
    val requestSecurityChange: (() -> Unit) -> Unit = { action ->
        if (config?.hashedPin?.isNotEmpty() == true) pendingSecurityChange = action
        else Toast.makeText(context, R.string.settings_pin_first, Toast.LENGTH_SHORT).show()
    }

    var showPinSetup by remember { mutableStateOf(false) }
    var currentPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var savingPin by remember { mutableStateOf(false) }
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
                text = stringResource(R.string.preferences),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )
        }

        // Credentials Configuration
        item {
            Text(
                text = stringResource(R.string.security_credentials),
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
                            Text(text = stringResource(R.string.primary_pin), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(
                                text = if (config?.hashedPin?.isNotEmpty() == true) stringResource(R.string.pin_configured) else stringResource(R.string.pin_not_configured),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (config?.hashedPin?.isNotEmpty() == true) SoftGreenAccent else MutedRedAccent
                            )
                        }
                        Button(
                            onClick = {
                                currentPinInput = ""
                                confirmPinInput = ""
                                pinSetupInput = ""
                                pinSetupError = null
                                showPinSetup = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text(stringResource(R.string.setup))
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
                            Text(text = stringResource(R.string.biometric_auth), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = stringResource(R.string.biometric_setting_desc), style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }
                        val biometricEnabled = config?.biometricsEnabled == true
                        Switch(
                            checked = biometricEnabled,
                            enabled = config?.hashedPin?.isNotEmpty() == true,
                            onCheckedChange = { enabled ->
                                requestSecurityChange {
                                    viewModel.setBiometricsEnabled(enabled) { success ->
                                        if (!success) Toast.makeText(context, context.getString(R.string.biometric_unavailable), Toast.LENGTH_LONG).show()
                                    }
                                }
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
                text = stringResource(R.string.lock_configuration),
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
                        0 -> stringResource(R.string.immediately)
                        15 -> stringResource(R.string.seconds_count, 15)
                        30 -> stringResource(R.string.seconds_count, 30)
                        60 -> stringResource(R.string.minute_one)
                        300 -> stringResource(R.string.minutes_count, 5)
                        600 -> stringResource(R.string.minutes_count, 10)
                        else -> stringResource(R.string.screen_off_only)
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
                            Text(text = stringResource(R.string.lock_timeout), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = stringResource(R.string.current_value, timeoutLabel), style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
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
                            Text(text = stringResource(R.string.lock_new_apps), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = stringResource(R.string.lock_new_desc), style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }
                        val lockNewActive = config?.lockNewlyInstalledApps == true
                        Switch(
                            checked = lockNewActive,
                            onCheckedChange = { active ->
                                requestSecurityChange { viewModel.setLockNewlyInstalledApps(active) }
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
                            Text(text = stringResource(R.string.haptic), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = stringResource(R.string.haptic_desc), style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
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
                text = stringResource(R.string.advanced_controls),
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
                            Text(text = stringResource(R.string.screenshot_shield), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = stringResource(R.string.screenshot_desc), style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }
                        Switch(
                            checked = screenshotEnabled,
                            onCheckedChange = { enabled ->
                                requestSecurityChange { viewModel.setScreenshotProtection(enabled, onScreenshotShieldToggle) }
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
                            Text(text = stringResource(R.string.randomized_keypad), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = stringResource(R.string.randomized_desc), style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }
                        Switch(
                            checked = randomizeEnabled,
                            onCheckedChange = { enabled -> requestSecurityChange { viewModel.setRandomizeKeypad(enabled) } },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("random_keypad_switch")
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
                            Text(text = stringResource(R.string.failed_limit), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = stringResource(R.string.failed_limit_count, failedLimit), style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextWarmGray)
                    }
                }
            }
        }

        // Appearance Configuration
        item {
            Text(
                text = stringResource(R.string.appearance),
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

                    Text(stringResource(R.string.theme_mode), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.theme_desc), style = MaterialTheme.typography.bodySmall)
                    listOf("SYSTEM" to R.string.theme_system, "LIGHT" to R.string.theme_light,
                        "DARK" to R.string.theme_dark, "DYNAMIC" to R.string.theme_dynamic).forEach { (mode, label) ->
                        if (mode != "DYNAMIC" || android.os.Build.VERSION.SDK_INT >= 31) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = currentTheme == mode, onClick = { viewModel.setThemeMode(mode) })
                                Text(stringResource(label), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.event_retention), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.retention_desc))
                    listOf(0, 100, 500, 1000).forEach { limit ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = viewModel.eventRetentionLimit == limit, onClick = { pendingRetention = limit })
                            Text(if (limit == 0) stringResource(R.string.keep_all) else stringResource(R.string.keep_latest, limit))
                        }
                    }
                }
            }
        }

        // Local Storage Backup & Restore Settings
        item {
            Text(
                text = stringResource(R.string.backup_recovery),
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
                            Text(text = stringResource(R.string.appearance_backup), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
                            Text(text = stringResource(R.string.appearance_backup_desc), style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
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
                            Text(stringResource(R.string.manage))
                        }
                    }
                }
            }
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.about), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.app_name), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.version_text, com.example.BuildConfig.VERSION_NAME))
                    Text(stringResource(R.string.privacy_summary), style = MaterialTheme.typography.bodyMedium)
                    Text(stringResource(R.string.accessibility_disclosure), style = MaterialTheme.typography.bodySmall)
                    Text(stringResource(R.string.platform_limitation), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }

    SecureAuthDialog(isOpen = pendingSecurityChange != null, title = stringResource(R.string.settings_confirm_security), randomizeKeypad = config?.randomizeKeypad == true,
        isPinConfigured = config?.hashedPin?.isNotEmpty() == true, onVerify = viewModel::verifyPin,
        onDismiss = { pendingSecurityChange = null }, onSuccess = {
            val action = pendingSecurityChange
            pendingSecurityChange = null
            action?.invoke()
        })

    pendingRetention?.let { limit ->
        AlertDialog(onDismissRequest = { pendingRetention = null }, title = { Text(stringResource(R.string.retention_change)) },
            text = { Text(if (limit == 0) stringResource(R.string.retention_all_confirm) else stringResource(R.string.retention_limit_confirm)) },
            confirmButton = { TextButton(onClick = {
                requestSecurityChange {
                    viewModel.setEventRetention(limit) { success ->
                        if (!success) Toast.makeText(context, context.getString(R.string.retention_error), Toast.LENGTH_SHORT).show()
                    }
                }
                pendingRetention = null
            }) { Text(stringResource(R.string.common_apply)) } }, dismissButton = { TextButton(onClick = { pendingRetention = null }) { Text(stringResource(R.string.common_cancel)) } })
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
                    val setupTitle = stringResource(R.string.settings_set_pin)
                    val setupDesc = stringResource(R.string.settings_pin_instructions)

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

                    if (config?.hashedPin?.isNotEmpty() == true) {
                        OutlinedTextField(
                            value = currentPinInput,
                            onValueChange = { if (it.length <= 6 && it.all { ch -> ch in '0'..'9' }) currentPinInput = it },
                            label = { Text(stringResource(R.string.settings_current_pin)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = pinSetupInput,
                        onValueChange = {
                            if (it.length <= 6 && it.all { char -> char in '0'..'9' }) {
                                pinSetupInput = it
                                pinSetupError = null
                            }
                        },
                        placeholder = { Text(stringResource(R.string.settings_enter_pin)) },
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                        ),
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_setup_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = confirmPinInput,
                        onValueChange = { if (it.length <= 6 && it.all { ch -> ch in '0'..'9' }) confirmPinInput = it },
                        label = { Text(stringResource(R.string.settings_confirm_pin)) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
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
                            Text(stringResource(R.string.common_cancel))
                        }

                        Button(
                            onClick = {
                                if (pinSetupInput.length != 6 || pinSetupInput != confirmPinInput) {
                                    pinSetupError = context.getString(R.string.settings_pin_matching)
                                } else if (!savingPin) {
                                    savingPin = true
                                    viewModel.setupPin(currentPinInput, pinSetupInput) { success ->
                                        savingPin = false
                                        if (success) {
                                            currentPinInput = ""
                                            pinSetupInput = ""
                                            confirmPinInput = ""
                                            showPinSetup = false
                                            Toast.makeText(context, R.string.settings_pin_saved, Toast.LENGTH_SHORT).show()
                                        } else {
                                            pinSetupError = context.getString(R.string.settings_pin_update_failed)
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(stringResource(R.string.common_confirm))
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
            title = { Text(stringResource(R.string.select_timeout), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val timeouts = listOf(
                        0 to stringResource(R.string.immediately),
                        15 to stringResource(R.string.seconds_count, 15),
                        30 to stringResource(R.string.seconds_count, 30),
                        60 to stringResource(R.string.minute_one),
                        300 to stringResource(R.string.minutes_count, 5),
                        600 to stringResource(R.string.minutes_count, 10),
                        -1 to stringResource(R.string.screen_off_only)
                    )
                    timeouts.forEach { (sec, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    requestSecurityChange { viewModel.setAutoLockTimeoutSeconds(sec) }
                                    showTimeoutSelector = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (config?.autoLockTimeoutSeconds ?: 0) == sec,
                                onClick = {
                                    requestSecurityChange { viewModel.setAutoLockTimeoutSeconds(sec) }
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
                    Text(stringResource(R.string.common_dismiss))
                }
            }
        )
    }

    // Intruder Failed Attempt Limit Dialog
    if (showLimitSelector) {
        AlertDialog(
            onDismissRequest = { showLimitSelector = false },
            title = { Text(stringResource(R.string.select_failed_limit), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val limits = listOf(3, 5, 10)
                    limits.forEach { limit ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    requestSecurityChange { viewModel.setFailedAttemptLimit(limit) }
                                    showLimitSelector = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (config?.failedAttemptLimit ?: 5) == limit,
                                onClick = {
                                    requestSecurityChange { viewModel.setFailedAttemptLimit(limit) }
                                    showLimitSelector = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.attempt_count, limit), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLimitSelector = false }) {
                    Text(stringResource(R.string.common_dismiss))
                }
            }
        )
    }

    // Backup & Restore Dialog
    if (showBackupRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showBackupRestoreDialog = false },
            title = { Text(stringResource(R.string.manage_appearance), fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        stringResource(R.string.backup_exported),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWarmGray
                    )

                    OutlinedTextField(
                        value = showBackupCode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.backup_key)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                    Text(
                        stringResource(R.string.backup_restore_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWarmGray
                    )

                    OutlinedTextField(
                        value = backupStringInput,
                        onValueChange = { backupStringInput = it },
                        label = { Text(stringResource(R.string.paste_backup)) },
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
                                Toast.makeText(context, context.getString(R.string.restore_success), Toast.LENGTH_SHORT).show()
                                showBackupRestoreDialog = false
                            } else {
                                Toast.makeText(context, context.getString(R.string.restore_invalid), Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.restore))
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupRestoreDialog = false }) {
                    Text(stringResource(R.string.common_close))
                }
            }
        )
    }
}
