package com.example

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.PrivacyViewModel
import com.example.ui.components.SecureAuthDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.IntruderSelfieScreen
import com.example.ui.screens.PrivacyCenterScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SoftGreenAccent
import com.example.ui.theme.SecondarySage

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        com.example.security.AppLockManager.init(applicationContext)

        setContent {
            val viewModel: PrivacyViewModel = viewModel()
            val config by viewModel.securityConfig.collectAsStateWithLifecycle()

            // Dynamic screenshot protection shield
            LaunchedEffect(config?.screenshotProtection) {
                val shieldActive = config?.screenshotProtection == true
                toggleScreenshotShield(shieldActive)
            }

            // Centralized Theme state
            val darkTheme = when (config?.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PrivacyLockApp(
                        viewModel = viewModel,
                        onScreenshotShieldToggle = { enabled -> toggleScreenshotShield(enabled) }
                    )
                }
            }
        }
    }

    private fun toggleScreenshotShield(enabled: Boolean) {
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyLockApp(
    viewModel: PrivacyViewModel,
    onScreenshotShieldToggle: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val config by viewModel.securityConfig.collectAsStateWithLifecycle()
    val isDecoyActive = viewModel.isDecoyModeActive

    var showLaunchSuccessSnackbar by remember { mutableStateOf(false) }
    var launchedAppName by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("app_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = viewModel.currentScreen == "dashboard",
                    onClick = { viewModel.currentScreen = "dashboard" },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Dashboard", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_item_dashboard")
                )

                NavigationBarItem(
                    selected = viewModel.currentScreen == "privacy_center",
                    onClick = { viewModel.currentScreen = "privacy_center" },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Privacy Center") },
                    label = { Text("Center", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_item_center")
                )

                NavigationBarItem(
                    selected = viewModel.currentScreen == "intruders",
                    onClick = { viewModel.currentScreen = "intruders" },
                    icon = { Icon(Icons.Default.PhotoCamera, contentDescription = "Caught Snoops") },
                    label = { Text("Intruders", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_item_intruders")
                )

                NavigationBarItem(
                    selected = viewModel.currentScreen == "settings",
                    onClick = { viewModel.currentScreen = "settings" },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Preferences") },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Decoy alert banner (stealthily masked as an offline cloud backup warning)
            AnimatedVisibility(visible = isDecoyActive) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Cloud sync offline. Running in secure local sandbox mode.",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        TextButton(
                            onClick = { 
                                viewModel.exitDecoyMode()
                                Toast.makeText(context, "Secure sync established. Main vault unlocked.", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Retry", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // Main Screens routing
            Box(modifier = Modifier.weight(1f)) {
                when (viewModel.currentScreen) {
                    "dashboard" -> {
                        DashboardScreen(
                            viewModel = viewModel,
                            onLaunchApp = { app ->
                                // Trigger authenticate overlay on locked apps
                                if (app.isLocked && !isDecoyActive) {
                                    viewModel.simulatedAppToLaunch = app
                                    viewModel.authDialogTitle = "Authenticate for ${app.name}"
                                    viewModel.authSuccessCallback = {
                                        launchedAppName = app.name
                                        showLaunchSuccessSnackbar = true
                                    }
                                    viewModel.isAuthDialogOpen = true
                                } else {
                                    // Direct launch simulation
                                    launchedAppName = app.name
                                    showLaunchSuccessSnackbar = true
                                }
                            }
                        )
                    }
                    "privacy_center" -> {
                        PrivacyCenterScreen(viewModel = viewModel)
                    }
                    "intruders" -> {
                        IntruderSelfieScreen(viewModel = viewModel)
                    }
                    "settings" -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            onScreenshotShieldToggle = onScreenshotShieldToggle
                        )
                    }
                }
            }
        }

        // Authenticate dialog overlay
        SecureAuthDialog(
            isOpen = viewModel.isAuthDialogOpen,
            title = viewModel.authDialogTitle,
            randomizeKeypad = config?.randomizeKeypad == true,
            decoyModeType = config?.decoyModeType ?: "NONE",
            isDecoyModeActive = isDecoyActive,
            isPinConfigured = config?.hashedPin?.isNotEmpty() == true,
            onVerify = { pin -> viewModel.verifyPin(pin) },
            onDismiss = {
                viewModel.isAuthDialogOpen = false
                viewModel.simulatedAppToLaunch = null
            },
            onSuccess = {
                viewModel.isAuthDialogOpen = false
                viewModel.authSuccessCallback?.invoke()
                viewModel.simulatedAppToLaunch = null
            }
        )

        // Sandbox app launch success overlay
        if (showLaunchSuccessSnackbar) {
            AlertDialog(
                onDismissRequest = { showLaunchSuccessSnackbar = false },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SoftGreenAccent, modifier = Modifier.size(36.dp)) },
                title = { Text("Secure Workspace Launched") },
                text = {
                    Text(
                        text = "Simulated application '$launchedAppName' is running inside Privacy Lock's offline sandbox container.",
                        textAlign = TextAlign.Center
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showLaunchSuccessSnackbar = false }) {
                        Text("Confirm")
                    }
                },
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}
