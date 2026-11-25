package com.example

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.security.AppLockManager
import com.example.security.SecurityUtils
import com.example.ui.PrivacyViewModel
import com.example.ui.screens.*
import com.example.ui.components.SecureAuthDialog
import com.example.data.LockedApp
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        setContent {
            var ready by remember { mutableStateOf(false) }
            var failed by remember { mutableStateOf(false) }
            var attempt by remember { mutableIntStateOf(0) }
            LaunchedEffect(attempt) {
                failed = false
                try {
                    withContext(Dispatchers.IO) {
                        AppDatabase.getDatabase(applicationContext)
                        AppLockManager.init(applicationContext)
                    }
                    ready = true
                } catch (cancelled: kotlinx.coroutines.CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    failed = true
                }
            }
            if (!ready) {
                MyApplicationTheme(darkTheme = isSystemInDarkTheme()) {
                    Surface(Modifier.fillMaxSize()) {
                        Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center) {
                            if (failed) {
                                Text(stringResource(R.string.storage_unavailable), style = MaterialTheme.typography.headlineSmall)
                                Text(stringResource(R.string.storage_preserved))
                                Button(onClick = { attempt++ }) { Text(stringResource(R.string.retry)) }
                            } else {
                                CircularProgressIndicator()
                                Text(stringResource(R.string.opening_storage), Modifier.padding(top = 16.dp))
                            }
                        }
                    }
                }
            } else {
                val vm: PrivacyViewModel = viewModel()
                val config by vm.securityConfig.collectAsStateWithLifecycle()
                val shield by vm.isScreenshotProtectionEnabled.collectAsStateWithLifecycle()
                LaunchedEffect(shield) { toggleScreenshotShield(shield) }
                val dark = when (config?.themeMode) { "LIGHT" -> false; "DARK" -> true; else -> isSystemInDarkTheme() }
                MyApplicationTheme(darkTheme = dark) {
                    PrivacyLockApp(vm, ::toggleScreenshotShield)
                }
            }
        }
    }

    private fun toggleScreenshotShield(enabled: Boolean) {
        if (enabled) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}

@Composable
fun PrivacyLockApp(viewModel: PrivacyViewModel, onScreenshotShieldToggle: (Boolean) -> Unit) {
    val context = LocalContext.current
    val config by viewModel.securityConfig.collectAsStateWithLifecycle()
    var pendingChange by remember { mutableStateOf<Pair<LockedApp, Boolean>?>(null) }
    Scaffold(bottomBar = {
        NavigationBar(modifier = Modifier.testTag("app_navigation_bar")) {
            val destinations = listOf(
                Triple("dashboard", stringResource(R.string.nav_apps), Icons.Default.Dashboard),
                Triple("privacy_center", stringResource(R.string.nav_center), Icons.Default.Shield),
                Triple("intruders", stringResource(R.string.nav_events), Icons.Default.History),
                Triple("settings", stringResource(R.string.nav_settings), Icons.Default.Settings)
            )
            destinations.forEach { (route, label, icon) ->
                NavigationBarItem(selected = viewModel.currentScreen == route,
                    onClick = { viewModel.currentScreen = route },
                    icon = { Icon(icon, contentDescription = null) }, label = { Text(label) })
            }
        }
    }) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (viewModel.currentScreen) {
                "dashboard" -> DashboardScreen(viewModel, onLockChange = { app, locked ->
                    pendingChange = app to locked
                }, onLaunchApp = { app ->
                    if (app.isLocked && (!SecurityUtils.isAccessibilityServiceEnabled(context) || !AppLockManager.serviceConnected.value)) {
                        Toast.makeText(context, context.getString(R.string.protection_unavailable), Toast.LENGTH_LONG).show()
                    } else {
                        val launch = context.packageManager.getLaunchIntentForPackage(app.packageName)
                        if (launch == null) Toast.makeText(context, context.getString(R.string.app_unavailable), Toast.LENGTH_SHORT).show()
                        else try { context.startActivity(launch) } catch (_: Exception) {
                            Toast.makeText(context, context.getString(R.string.cannot_open_app), Toast.LENGTH_SHORT).show()
                        }
                    }
                })
                "privacy_center" -> PrivacyCenterScreen(viewModel)
                "intruders" -> IntruderSelfieScreen(viewModel)
                "settings" -> SettingsScreen(viewModel, onScreenshotShieldToggle = onScreenshotShieldToggle)
            }
        }
        SecureAuthDialog(isOpen = pendingChange != null, title = stringResource(R.string.confirm_protection_change),
            randomizeKeypad = config?.randomizeKeypad == true,
            isPinConfigured = config?.hashedPin?.isNotEmpty() == true,
            onVerify = viewModel::verifyPin,
            onDismiss = { pendingChange = null }, onSuccess = {
                pendingChange?.let { (app, locked) -> viewModel.toggleAppLock(app.packageName, locked) }
                pendingChange = null
            })
    }
}
