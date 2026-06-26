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
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PrivacyCenterScreen(
    viewModel: PrivacyViewModel,
    modifier: Modifier = Modifier
) {
    val apps by viewModel.allApps.collectAsStateWithLifecycle()
    val config by viewModel.securityConfig.collectAsStateWithLifecycle()
    val timeline by viewModel.timelineEvents.collectAsStateWithLifecycle()
    val selfies by viewModel.intruderSelfies.collectAsStateWithLifecycle()

    val lockedCount = remember(apps) { apps.count { it.isLocked } }
    val safeCount = remember(apps) { apps.count { !it.isLocked } }

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
                text = "Privacy Statistics",
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
                        Text(text = "Locked Apps", style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
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
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MutedRedAccent, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = "${selfies.size}", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = "Intruders Caught", style = MaterialTheme.typography.bodySmall, color = TextWarmGray)
                    }
                }
            }
        }

        // Actionable Recommendations Card
        item {
            Text(
                text = "Recommended Improvements",
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
                    val screenshotShieldEnabled = config?.screenshotProtection == true
                    RecommendationItem(
                        title = "Enable Screenshot Shield",
                        description = "Blocks other apps and processes from recording or screenshotting your safe space.",
                        scoreValue = "+15 Score",
                        isActive = screenshotShieldEnabled,
                        onResolve = { viewModel.setScreenshotProtection(!screenshotShieldEnabled) {} }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                    // Recommendation 2: Randomized Keypad Layout
                    val keypadRandomized = config?.randomizeKeypad == true
                    RecommendationItem(
                        title = "Activate Randomized Keypad",
                        description = "Shuffles security keys during code entry to prevent hand-tracking snoop attempts.",
                        scoreValue = "+15 Score",
                        isActive = keypadRandomized,
                        onResolve = { viewModel.setRandomizeKeypad(!keypadRandomized) }
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))

                    // Recommendation 3: Decoy Stealth PIN
                    val decoyPinSet = config?.hashedDecoyPin?.isNotEmpty() == true
                    RecommendationItem(
                        title = "Setup Stealth Decoy PIN",
                        description = "Configures an alternative PIN code that unlocks into a fake empty sandbox workspace.",
                        scoreValue = "Secure Mode",
                        isActive = decoyPinSet,
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
                    text = "Security Event Log",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
                if (timeline.isNotEmpty()) {
                    TextButton(onClick = { viewModel.clearTimeline() }) {
                        Text("Clear log", color = MaterialTheme.colorScheme.error)
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
                        text = "No security events logged yet.",
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
                        text = if (isActive) "Active" else scoreValue,
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
                contentDescription = "Resolve",
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
        "INTRUDER" -> Pair(Icons.Default.Warning, MutedRedAccent)
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
                    text = event.description,
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
