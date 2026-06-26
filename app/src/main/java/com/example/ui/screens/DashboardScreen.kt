package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarOutline
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.LockedApp
import com.example.ui.PrivacyViewModel
import com.example.ui.components.SimulatedAppIcon
import com.example.ui.theme.*
import com.example.security.SecurityUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: PrivacyViewModel,
    modifier: Modifier = Modifier,
    onLaunchApp: (LockedApp) -> Unit
) {
    val apps by viewModel.allApps.collectAsStateWithLifecycle()
    val score by viewModel.privacyScore.collectAsStateWithLifecycle()
    val config by viewModel.securityConfig.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var isAccessibilityActive by remember { mutableStateOf(false) }
    var isUsageActive by remember { mutableStateOf(false) }
    var isOverlayActive by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAccessibilityActive = SecurityUtils.isAccessibilityServiceEnabled(context)
                isUsageActive = SecurityUtils.isUsageAccessGranted(context)
                isOverlayActive = SecurityUtils.isOverlayPermissionGranted(context)
                // Also trigger a manual sync to check for new apps on resume
                viewModel.refreshInstalledApps()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val categories = listOf("All", "System", "Social", "Finance", "Locked")

    // Filtered apps based on search & category
    val filteredList = remember(apps, viewModel.searchQuery, viewModel.selectedCategory) {
        apps.filter { app ->
            val matchesSearch = app.name.contains(viewModel.searchQuery, ignoreCase = true) ||
                    app.packageName.contains(viewModel.searchQuery, ignoreCase = true)
            val matchesCategory = when (viewModel.selectedCategory) {
                "All" -> true
                "Locked" -> app.isLocked
                else -> app.category.equals(viewModel.selectedCategory, ignoreCase = true)
            }
            matchesSearch && matchesCategory
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
    ) {
        // Dynamic time-based greeting header
        item {
            val greeting = remember {
                val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                when (hour) {
                    in 0..11 -> "Good Morning"
                    in 12..16 -> "Good Afternoon"
                    else -> "Good Evening"
                }
            }
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Your Privacy is Fully Protected",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextWarmGray
                )
            }
        }

        // Privacy Score Indicator Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Privacy Score",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = when {
                                score >= 80 -> "Excellent Protection"
                                score >= 50 -> "Moderate Protection"
                                else -> "Critical Vulnerability"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = if (score >= 80) SoftGreenAccent else if (score >= 50) SoftAmberAccent else MutedRedAccent
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (score >= 90) "Fully secured against typical mobile snoop attacks." else "Complete security recommendations below to hit 100%.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextWarmGray
                        )
                    }

                    // Score Circle Arc simulation
                    Box(
                        modifier = Modifier
                            .size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            progress = { score.toFloat() / 100f },
                            modifier = Modifier.fillMaxSize(),
                            color = if (score >= 80) SoftGreenAccent else if (score >= 50) SoftAmberAccent else MutedRedAccent,
                            strokeWidth = 8.dp,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // System Permissions Check Card (Onboarding flow)
        val showPermissionCard = !isAccessibilityActive || !isUsageActive || !isOverlayActive
        if (showPermissionCard) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "System Permissions Required",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }

                        Text(
                            text = "To enable automatic application locking, please grant the following required system permissions:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Accessibility Service Permission Row
                        PermissionStatusRow(
                            name = "Accessibility Service (Required)",
                            description = "Locks apps immediately as soon as they are launched",
                            isGranted = isAccessibilityActive,
                            onGrantClick = {
                                SecurityUtils.requestAccessibilityPermission(context)
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f))

                        // Usage Access Permission Row
                        PermissionStatusRow(
                            name = "Usage Stats Access (Recommended)",
                            description = "Enhances foreground application scanning accuracy",
                            isGranted = isUsageActive,
                            onGrantClick = {
                                SecurityUtils.requestUsageAccessPermission(context)
                            }
                        )

                        HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.15f))

                        // Overlay Permission Row
                        PermissionStatusRow(
                            name = "Display Over Other Apps (Recommended)",
                            description = "Prevents security bypass overlays",
                            isGranted = isOverlayActive,
                            onGrantClick = {
                                SecurityUtils.requestOverlayPermission(context)
                            }
                        )
                    }
                }
            }
        }

        // Simulated Smartphone App Launcher Preview (Interactive Sandbox)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Interactive Vault Sandbox",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )
                Text(
                    text = "Launch any app icon to simulate sandbox protection",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextWarmGray,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 4x2 App grid preview
                        val launcherApps = remember(apps) {
                            apps.take(8)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            launcherApps.take(4).forEach { app ->
                                SandboxAppLauncherItem(app = app, onClick = { onLaunchApp(app) })
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            launcherApps.drop(4).take(4).forEach { app ->
                                SandboxAppLauncherItem(app = app, onClick = { onLaunchApp(app) })
                            }
                        }
                    }
                }
            }
        }

        // App List Toggles & Filters Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "All Protected Applications",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp, start = 4.dp)
                )

                // Search Bar
                OutlinedTextField(
                    value = viewModel.searchQuery,
                    onValueChange = { viewModel.searchQuery = it },
                    placeholder = { Text("Search apps or packages...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("app_search_input"),
                    shape = RoundedCornerShape(16.dp),
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (viewModel.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Row Horizontal scrolling
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categories) { category ->
                        val selected = viewModel.selectedCategory == category
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            border = null,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
        }

        // Toggles list body
        if (filteredList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = TextWarmGray,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No protected apps found.",
                            color = TextWarmGray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(filteredList, key = { it.packageName }) { app ->
                AppLockListItem(
                    app = app,
                    onLockToggle = { isLocked -> viewModel.toggleAppLock(app.packageName, isLocked) },
                    onFavoriteToggle = { isFav -> viewModel.toggleAppFavorite(app.packageName, isFav) },
                    onPinToggle = { isPinned -> viewModel.toggleAppPinned(app.packageName, isPinned) }
                )
            }
        }
    }
}

/**
 * Single icon launcher for the Sandbox Grid
 */
@Composable
fun SandboxAppLauncherItem(
    app: LockedApp,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(68.dp)
            .clickable(onClick = onClick)
            .padding(4.dp)
            .testTag("sandbox_app_${app.iconName}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            SimulatedAppIcon(iconName = app.iconName, size = 48, packageName = app.packageName)

            // Show a tiny shield overlay if app is locked
            if (app.isLocked) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(MutedRedAccent),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "App is locked",
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = app.name,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Beautiful app lock list row item with toggles and favorite indicators
 */
@Composable
fun AppLockListItem(
    app: LockedApp,
    onLockToggle: (Boolean) -> Unit,
    onFavoriteToggle: (Boolean) -> Unit,
    onPinToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("app_row_${app.packageName}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Icon
            SimulatedAppIcon(iconName = app.iconName, size = 42, packageName = app.packageName)

            Spacer(modifier = Modifier.width(14.dp))

            // Name & Category Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (app.isPinned) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextWarmGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick interaction toolbar (Pin, Lock switch)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Pin toggle button
                IconButton(onClick = { onPinToggle(!app.isPinned) }) {
                    Icon(
                        imageVector = if (app.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                        contentDescription = "Pin to top",
                        tint = if (app.isPinned) MaterialTheme.colorScheme.primary else TextWarmGray.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Lock toggle Switch
                Switch(
                    checked = app.isLocked,
                    onCheckedChange = onLockToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = MaterialTheme.colorScheme.primary,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("lock_switch_${app.packageName}")
                )
            }
        }
    }
}

@Composable
fun PermissionStatusRow(
    name: String,
    description: String,
    isGranted: Boolean,
    onGrantClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (isGranted) SoftGreenAccent else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.labelSmall,
                color = TextWarmGray
            )
        }

        if (isGranted) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Granted",
                tint = SoftGreenAccent,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Button(
                onClick = onGrantClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
