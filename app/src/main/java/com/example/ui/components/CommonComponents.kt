package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.LockedApp
import com.example.ui.theme.PrimarySage
import com.example.ui.theme.TextWarmGray
import com.example.ui.theme.SecondarySage
import com.example.ui.theme.TertiarySand
import com.example.ui.theme.MutedRedAccent

/**
 * Custom modern icon helper for simulated apps
 */
@Composable
fun SimulatedAppIcon(
    iconName: String,
    modifier: Modifier = Modifier,
    size: Int = 44,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
) {
    val (vector, color) = when (iconName) {
        "settings" -> Pair(Icons.Default.Settings, PrimarySage)
        "play_store" -> Pair(Icons.Default.Shop, Color(0xFF34A853))
        "installer" -> Pair(Icons.Default.Build, Color(0xFFEA4335))
        "whatsapp" -> Pair(Icons.AutoMirrored.Filled.Chat, Color(0xFF25D366))
        "discord" -> Pair(Icons.Default.Forum, Color(0xFF5865F2))
        "gmail" -> Pair(Icons.Default.Email, Color(0xFFEA4335))
        "messages" -> Pair(Icons.Default.Sms, Color(0xFF4285F4))
        "chrome" -> Pair(Icons.Default.Language, Color(0xFFFBBC05))
        "google_pay" -> Pair(Icons.Default.AccountBalanceWallet, Color(0xFF4285F4))
        "chase" -> Pair(Icons.Default.CreditCard, Color(0xFF005A9C))
        "binance" -> Pair(Icons.Default.CurrencyExchange, Color(0xFFF0B90B))
        "gallery" -> Pair(Icons.Default.Image, Color(0xFFE066FF))
        else -> Pair(Icons.Default.Android, PrimarySage)
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = vector,
            contentDescription = "Simulated $iconName icon",
            tint = color,
            modifier = Modifier.size((size * 0.55f).dp)
        )
    }
}

/**
 * Premium Randomized or Standard Keypad for PIN inputs
 */
@Composable
fun SecureKeypad(
    onNumberClick: (String) -> Unit,
    onDeleteClick: () -> Unit,
    onClearClick: () -> Unit,
    randomizeLayout: Boolean,
    modifier: Modifier = Modifier
) {
    val numbers = remember(randomizeLayout) {
        val list = (0..9).map { it.toString() }.toMutableList()
        if (randomizeLayout) {
            list.shuffle()
        }
        list
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Grid representing keypad
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
            ) {
                for (col in 0..2) {
                    val num = numbers[row * 3 + col]
                    KeypadButton(
                        text = num,
                        onClick = { onNumberClick(num) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Bottom row (Clear, 0/last number, Delete)
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
        ) {
            // Clear Button
            IconButton(
                onClick = onClearClick,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clip(CircleShape)
            ) {
                Text(
                    text = "C",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Zero or remaining number
            val lastNum = numbers[9]
            KeypadButton(
                text = lastNum,
                onClick = { onNumberClick(lastNum) },
                modifier = Modifier.weight(1f)
            )

            // Delete Button
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun KeypadButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .clickable(onClick = onClick)
            .testTag("keypad_btn_$text"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.SansSerif
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Premium authentication keypad modal
 */
@Composable
fun SecureAuthDialog(
    isOpen: Boolean,
    title: String,
    randomizeKeypad: Boolean,
    decoyModeType: String, // "NONE", "FAKE_CRASH"
    isDecoyModeActive: Boolean,
    isPinConfigured: Boolean = true,
    onVerify: (String) -> String, // Returns "SUCCESS_NORMAL", "SUCCESS_DECOY", "FAILED"
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    if (!isOpen) return

    var pinBuffer by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var attemptsCount by remember { mutableStateOf(0) }
    var showFakeCrash by remember { mutableStateOf(false) }

    // Fake crash behavior helper
    if (showFakeCrash) {
        FakeCrashScreen(onClose = {
            showFakeCrash = false
            pinBuffer = ""
            errorMessage = null
            onDismiss()
        })
        return
    }

    Dialog(
        onDismissRequest = {
            pinBuffer = ""
            errorMessage = null
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .windowInsetsPadding(WindowInsets.safeDrawing),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        pinBuffer = ""
                        errorMessage = null
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close auth screen")
                    }

                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )

                    Spacer(modifier = Modifier.width(48.dp)) // Spacer to keep balance
                }

                // Center Title & Pin Indicators
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 22.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = if (isPinConfigured) "Enter privacy vault credentials" else "Default PIN is 1234. Set custom PIN in Settings.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPinConfigured) TextWarmGray else MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PIN Dots indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..4) {
                            val active = pinBuffer.length >= i
                            val dotColor by animateColorAsState(
                                targetValue = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                animationSpec = spring()
                            )
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }

                // Keypad Section
                SecureKeypad(
                    onNumberClick = { num ->
                        if (pinBuffer.length < 4) {
                            errorMessage = null
                            pinBuffer += num

                            // Auto verify on 4 digits
                            if (pinBuffer.length == 4) {
                                when (onVerify(pinBuffer)) {
                                    "SUCCESS_NORMAL" -> {
                                        pinBuffer = ""
                                        errorMessage = null
                                        onSuccess()
                                    }
                                    "SUCCESS_DECOY" -> {
                                        pinBuffer = ""
                                        errorMessage = null
                                        onSuccess()
                                    }
                                    else -> {
                                        attemptsCount++
                                        pinBuffer = ""
                                        errorMessage = "Incorrect security code. Attempt recorded."

                                        // Trigger decoy screen if configured and attempts > 2
                                        if (decoyModeType == "FAKE_CRASH" && attemptsCount >= 3) {
                                            showFakeCrash = true
                                            attemptsCount = 0
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        if (pinBuffer.isNotEmpty()) {
                            pinBuffer = pinBuffer.dropLast(1)
                        }
                    },
                    onClearClick = {
                        pinBuffer = ""
                        errorMessage = null
                    },
                    randomizeLayout = randomizeKeypad,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}

/**
 * Interactive full-screen Fake Crash Decoy Screen
 */
@Composable
fun FakeCrashScreen(onClose: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF1C1B1F)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEA4335).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFEA4335),
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "PrivacyLock has stopped",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "An unexpected error occurred. The application must close. Please try again later.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F3F3F)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Close app", color = Color.White)
            }
        }
    }
}
