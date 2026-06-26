package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.IntruderSelfie
import com.example.ui.PrivacyViewModel
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun IntruderSelfieScreen(
    viewModel: PrivacyViewModel,
    modifier: Modifier = Modifier
) {
    val selfies by viewModel.intruderSelfies.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Intruder Timeline",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${selfies.size} caught attempts",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextWarmGray
                )
            }

            if (selfies.isNotEmpty()) {
                TextButton(onClick = { viewModel.clearAllSelfies() }) {
                    Text("Delete All", color = MaterialTheme.colorScheme.error)
                }
            }
        }

        if (selfies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "System is fully secure",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "No failed login attempts recorded yet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextWarmGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            // Grid of Intruder Cards
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(selfies, key = { it.id }) { selfie ->
                    IntruderCard(
                        selfie = selfie,
                        onDelete = { viewModel.deleteSelfie(selfie.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun IntruderCard(
    selfie: IntruderSelfie,
    onDelete: () -> Unit
) {
    val dateStr = remember(selfie.timestamp) {
        val sdf = SimpleDateFormat("HH:mm:ss\nMMM dd, yyyy", Locale.getDefault())
        sdf.format(Date(selfie.timestamp))
    }

    // Interactive Snooper Profile setup based on random avatarId
    val snoopProfile = remember(selfie.avatarId) {
        when (selfie.avatarId) {
            1 -> SnoopProfile("The Nosey Colleague", Color(0xFFC76D6D), Icons.Default.SupervisedUserCircle)
            2 -> SnoopProfile("Snooping Sibling", Color(0xFF7DA385), Icons.Default.Face)
            3 -> SnoopProfile("Midnight Crawler", Color(0xFFD4A35F), Icons.Default.NightlightRound)
            4 -> SnoopProfile("Coffee Shop Spy", Color(0xFF4285F4), Icons.Default.Coffee)
            else -> SnoopProfile("The Shadow Walker", Color(0xFF9B51E0), Icons.Default.Psychology)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("intruder_card_${selfie.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Profile Icon & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(snoopProfile.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = snoopProfile.icon,
                        contentDescription = null,
                        tint = snoopProfile.color,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete record",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Snoop Name
            Text(
                text = snoopProfile.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                textAlign = TextAlign.Center
            )

            // Attempted pin code
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Entered Code",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextWarmGray
                )
                Text(
                    text = "'${selfie.failedPinAttempt}'",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Time & Date Stamp
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodySmall,
                color = TextWarmGray,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )
        }
    }
}

data class SnoopProfile(
    val name: String,
    val color: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)
