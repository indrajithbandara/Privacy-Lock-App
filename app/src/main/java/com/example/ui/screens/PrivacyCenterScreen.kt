package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.example.R
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LockedApp
import com.example.data.TimelineEvent
import com.example.ui.PrivacyViewModel
import com.example.ui.components.SecureAuthDialog
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PrivacyCenterScreen(
    viewModel: PrivacyViewModel,
    modifier: Modifier = Modifier
) {
    val apps by viewModel.allApps.collectAsStateWithLifecycle()
    val effectiveLocks by viewModel.effectiveLocks.collectAsStateWithLifecycle()
    val config by viewModel.securityConfig.collectAsStateWithLifecycle()
    val timeline by viewModel.timelineEvents.collectAsStateWithLifecycle()
    val failures = timeline.count { it.type == "AUTH_FAILURE" }
    var confirmClear by remember { mutableStateOf(false) }
    var authenticateClear by remember { mutableStateOf(false) }

    val lockedCount = apps.count { it.packageName in effectiveLocks }
    val safeCount = apps.count { it.packageName !in effectiveLocks }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Core Statistics Grid Rows
        item {
            Text(
                text = stringResource(R.string.privacy_statistics),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Locked Apps Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(PrimarySage.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = PrimarySage, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "$lockedCount", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = stringResource(R.string.lock_preferences), style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                    }
                }

                // Intruders Blocked Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MutedRedAccent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MutedRedAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "$failures", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = stringResource(R.string.recent_pin_failures), style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                    }
                }
            }
        }

        // Actionable Recommendations Card
        item {
            Text(
                text = stringResource(R.string.recommendations),
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
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Recommendation 1: Screenshot Shield
                    val screenshotShieldEnabled by viewModel.isScreenshotProtectionEnabled.collectAsStateWithLifecycle()
                    RecommendationItem(
                        title = stringResource(R.string.enable_screenshot),
                        description = stringResource(R.string.screenshot_desc),
                        scoreValue = stringResource(R.string.off),
                        isActive = screenshotShieldEnabled,
                        onResolve = { viewModel.currentScreen = "settings" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                    // Recommendation 2: Randomized Keypad Layout
                    val keypadRandomized = config?.randomizeKeypad == true
                    RecommendationItem(
                        title = stringResource(R.string.enable_randomized),
                        description = stringResource(R.string.randomized_recommendation),
                        scoreValue = stringResource(R.string.off),
                        isActive = keypadRandomized,
                        onResolve = { viewModel.currentScreen = "settings" }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                    RecommendationItem(
                        title = stringResource(R.string.configure_auth),
                        description = stringResource(R.string.configure_auth_desc),
                        scoreValue = stringResource(R.string.required),
                        isActive = config?.hashedPin?.isNotEmpty() == true,
                        onResolve = { viewModel.currentScreen = "settings" }
                    )
                }
            }
        }

        // Timeline History
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.event_log),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
                if (timeline.isNotEmpty()) {
                    TextButton(onClick = { confirmClear = true }) {
                        Text(stringResource(R.string.clear_log), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        if (timeline.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_events),
                        color = TextWarmGray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            items(timeline, key = { it.id }) { event ->
                TimelineLogItem(event = event)
            }
        }
    }
    if (confirmClear) {
        AlertDialog(onDismissRequest = { confirmClear = false }, title = { Text(stringResource(R.string.clear_events_confirm)) },
            text = { Text(stringResource(R.string.clear_events_warning)) },
            confirmButton = { TextButton(onClick = { confirmClear = false; authenticateClear = true }) { Text(stringResource(R.string.continue_action)) } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.common_cancel)) } })
    }
    SecureAuthDialog(isOpen = authenticateClear, title = stringResource(R.string.confirm_event_deletion), randomizeKeypad = config?.randomizeKeypad == true,
        isPinConfigured = config?.hashedPin?.isNotEmpty() == true, onVerify = viewModel::verifyPin,
        onDismiss = { authenticateClear = false }, onSuccess = { authenticateClear = false; viewModel.clearTimeline() })
}

/**
 * Beautiful Actionable improvement row
 */
@Composable
fun RecommendationItem(
    title: String,
    description: String,
    scoreValue: String,
    isActive: Boolean,
    onResolve: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isActive) SoftGreenAccent.copy(alpha = 0.15f) else SoftAmberAccent.copy(alpha = 0.15f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isActive) stringResource(R.string.active) else scoreValue,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                        color = if (isActive) SoftGreenAccent else SoftAmberAccent
                    )
                }
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextWarmGray,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        IconButton(onClick = onResolve) {
            Icon(
                imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.AddCircle,
                contentDescription = stringResource(R.string.resolve),
                tint = if (isActive) SoftGreenAccent else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * Log entry details for history timeline
 */
@Composable
fun TimelineLogItem(event: TimelineEvent) {
    val date = remember(event.timestamp) {
        val sdf = SimpleDateFormat("HH:mm - MMM dd", Locale.getDefault())
        sdf.format(Date(event.timestamp))
    }

    val (icon, tint) = when (event.type) {
        "LOCK" -> Pair(Icons.Default.Lock, PrimarySage)
        "UNLOCK" -> Pair(Icons.Default.LockOpen, SoftGreenAccent)
        "AUTH_FAILURE", "AUTH_LOCKOUT" -> Pair(Icons.Default.Warning, MutedRedAccent)
        else -> Pair(Icons.Default.Info, SoftAmberAccent)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when (event.type) {
                        "AUTH_FAILURE" -> stringResource(R.string.auth_event_failure)
                        "AUTH_LOCKOUT" -> stringResource(R.string.auth_event_lockout)
                        "PROFILE_CREATED" -> stringResource(R.string.event_profile_created, event.description)
                        "PROFILE_UPDATED" -> stringResource(R.string.event_profile_updated, event.description)
                        "PROFILE_ACTIVATED" -> stringResource(R.string.event_profile_activated, event.description)
                        "PROFILE_DEACTIVATED" -> stringResource(R.string.event_profile_deactivated, event.description)
                        "PROFILE_DELETED" -> stringResource(R.string.event_profile_deleted, event.description)
                        else -> event.description
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextWarmGray,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
