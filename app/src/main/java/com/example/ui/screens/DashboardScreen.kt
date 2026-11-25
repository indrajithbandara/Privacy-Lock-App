package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LockedApp
import com.example.security.AppLockManager
import com.example.security.SecurityUtils
import com.example.ui.PrivacyViewModel
import com.example.ui.components.SimulatedAppIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(viewModel: PrivacyViewModel, modifier: Modifier = Modifier, onLockChange: (LockedApp, Boolean) -> Unit, onLaunchApp: (LockedApp) -> Unit) {
    val apps by viewModel.allApps.collectAsStateWithLifecycle()
    val config by viewModel.securityConfig.collectAsStateWithLifecycle()
    val connected by AppLockManager.serviceConnected.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var permission by remember { mutableStateOf(SecurityUtils.isAccessibilityServiceEnabled(context)) }
    var eligible by remember { mutableStateOf<Set<String>>(emptySet()) }
    LaunchedEffect(apps) {
        eligible = withContext(Dispatchers.IO) { apps.filter { AppLockManager.canProtect(context, it.packageName) }.map { it.packageName }.toSet() }
    }
    DisposableEffect(lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permission = SecurityUtils.isAccessibilityServiceEnabled(context)
                viewModel.refreshInstalledApps()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val enrolled = config?.hashedPin?.isNotEmpty() == true
    val configuredCount = apps.count { it.isLocked && it.packageName in eligible }
    val ready = enrolled && permission && connected
    val filtered = apps.filter { app ->
        (app.packageName in eligible) && (app.name.contains(viewModel.searchQuery, true) || app.packageName.contains(viewModel.searchQuery, true)) &&
            when (viewModel.selectedCategory) {
                "Locked" -> app.isLocked
                "Unlocked" -> !app.isLocked
                "Pinned" -> app.isPinned
                else -> true
            }
    }
    LazyColumn(modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 20.dp)) {
        item {
            Text(stringResource(R.string.your_apps), style = MaterialTheme.typography.headlineLarge)
            Text(stringResource(R.string.choose_apps), style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(
                containerColor = if (ready) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(if (ready) Icons.Default.Shield else Icons.Default.Info, contentDescription = null)
                    Text(if (ready) stringResource(R.string.protection_configured) else stringResource(R.string.protection_attention), style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.selected_count, configuredCount), style = MaterialTheme.typography.bodyLarge)
                    Text(stringResource(R.string.platform_limitation), style = MaterialTheme.typography.bodySmall)
                    if (!enrolled) Button(onClick = { viewModel.currentScreen = "settings" }) { Text(stringResource(R.string.setup_pin)) }
                    if (!permission || !connected) OutlinedButton(onClick = { SecurityUtils.requestAccessibilityPermission(context) }) { Text(stringResource(R.string.accessibility_settings)) }
                }
            }
        }
        item {
            OutlinedTextField(value = viewModel.searchQuery, onValueChange = { viewModel.searchQuery = it },
                label = { Text(stringResource(R.string.search_installed)) }, leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true, modifier = Modifier.fillMaxWidth().testTag("app_search_input"), shape = RoundedCornerShape(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "Locked", "Unlocked", "Pinned")) { filter ->
                    FilterChip(selected = viewModel.selectedCategory == filter, onClick = { viewModel.selectedCategory = filter }, label = { Text(stringResource(when (filter) { "Locked" -> R.string.filter_locked; "Unlocked" -> R.string.filter_unlocked; "Pinned" -> R.string.filter_pinned; else -> R.string.filter_all })) })
                }
            }
        }
        if (filtered.isEmpty()) item {
            Text(stringResource(R.string.no_eligible_apps), Modifier.padding(vertical = 24.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        items(filtered, key = { it.packageName }) { app ->
            Card(Modifier.fillMaxWidth().testTag("app_row_${app.packageName}"), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SimulatedAppIcon(iconName = app.iconName, packageName = app.packageName, size = 44)
                    Column(Modifier.weight(1f)) {
                        Text(app.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(if (app.isLocked) stringResource(R.string.lock_configured) else stringResource(R.string.lock_not_configured), style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { onLaunchApp(app) }, contentPadding = PaddingValues(0.dp)) { Text(stringResource(R.string.open_app)) }
                    }
                    IconButton(onClick = { viewModel.toggleAppPinned(app.packageName, !app.isPinned) }) {
                        Icon(Icons.Default.PushPin, if (app.isPinned) stringResource(R.string.unpin_app, app.name) else stringResource(R.string.pin_app, app.name), tint = if (app.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = app.isLocked, enabled = enrolled, onCheckedChange = { onLockChange(app, it) }, modifier = Modifier.testTag("lock_switch_${app.packageName}"))
                }
            }
        }
    }
}
