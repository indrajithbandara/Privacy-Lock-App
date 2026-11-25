package com.example.ui.components

import android.content.Context
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
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import com.example.R
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

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.foundation.Image

/**
 * Displays the installed application's real icon, or a neutral fallback.
 */
@Composable
fun InstalledAppIcon(
    modifier: Modifier = Modifier,
    size: Int = 44,
    packageName: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)
) {
    val context = LocalContext.current
    val pm = remember { context.packageManager }
    val drawable = remember(packageName) {
        if (packageName != null) {
            try {
                pm.getApplicationIcon(packageName)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    if (drawable != null) {
        val bitmap = remember(drawable) {
            try {
                drawable.toBitmap().asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "App Icon",
                modifier = modifier
                    .size(size.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            return
        }
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Android,
            contentDescription = "Generic application icon",
            tint = MaterialTheme.colorScheme.primary,
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
    vibrateOnKeyPress: Boolean = true,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator }

    val triggerVibration = {
        if (vibrateOnKeyPress) {
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator?.vibrate(android.os.VibrationEffect.createOneShot(35, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(35)
                }
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    val numbers = remember(randomizeLayout) {
        val list = (1..9).map { it.toString() }.toMutableList()
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
        // Grid representing keypad (Digits 1-9)
        for (row in 0..2) {
            Row(
                modifier = Modifier.fillMaxWidth(0.85f),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
            ) {
                for (col in 0..2) {
                    val num = numbers[row * 3 + col]
                    KeypadButton(
                        text = num,
                        onClick = {
                            triggerVibration()
                            onNumberClick(num)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Bottom row (Clear, 0, Delete)
        Row(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally)
        ) {
            // Clear Button
            val clearDescription = stringResource(R.string.auth_clear)
            IconButton(
                onClick = {
                    triggerVibration()
                    onClearClick()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clip(CircleShape)
                    .semantics { contentDescription = clearDescription }
                    .testTag("keypad_btn_clear")
            ) {
                Text(
                    text = "C",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Zero is always in the center of the bottom row
            KeypadButton(
                text = "0",
                onClick = {
                    triggerVibration()
                    onNumberClick("0")
                },
                modifier = Modifier.weight(1f)
            )

            // Delete Button
            IconButton(
                onClick = {
                    triggerVibration()
                    onDeleteClick()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .clip(CircleShape)
                    .testTag("keypad_btn_delete")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = stringResource(R.string.auth_delete),
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
            .clickable(role = Role.Button, onClick = onClick)
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
    isPinConfigured: Boolean = true,
    vibrateOnKeyPress: Boolean = true,
    onVerify: suspend (String) -> String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    if (!isOpen) return
    val context = LocalContext.current

    val verifyScope = rememberCoroutineScope()
    var verifying by remember { mutableStateOf(false) }
    var retryAfter by remember { mutableStateOf(0L) }
    LaunchedEffect(retryAfter) {
        if (retryAfter > 0) {
            kotlinx.coroutines.delay(1000)
            retryAfter--
        }
    }

    var pinBuffer by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

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
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.auth_close))
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
                        text = if (isPinConfigured) stringResource(R.string.auth_primary_pin) else stringResource(R.string.auth_no_pin),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPinConfigured) TextWarmGray else MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // PIN Dots indicators; accessibility exposes only the number of entered digits.
                    val digitStatus = stringResource(R.string.auth_digits_entered, pinBuffer.length)
                    Row(
                        modifier = Modifier.semantics { stateDescription = digitStatus },
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 1..6) {
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

                    if (retryAfter > 0 || errorMessage != null) {
                        Text(
                            text = if (retryAfter > 0) stringResource(R.string.auth_retry_seconds, retryAfter) else errorMessage.orEmpty(),
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
                        if (isPinConfigured && !verifying && retryAfter == 0L && pinBuffer.length < 6) {
                            errorMessage = null
                            pinBuffer += num

                            // Auto verify on 6 digits
                            if (pinBuffer.length == 6) {
                                val submitted = pinBuffer
                                pinBuffer = ""
                                verifying = true
                                verifyScope.launch {
                                    try {
                                        val result = onVerify(submitted)
                                        if (result == "SUCCESS_NORMAL") {
                                            errorMessage = null
                                            onSuccess()
                                        } else {
                                            retryAfter = result.substringAfter("LOCKED:", "0").toLongOrNull() ?: 0L
                                            errorMessage = context.getString(R.string.auth_denied)
                                        }
                                    } finally {
                                        verifying = false
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
                    vibrateOnKeyPress = vibrateOnKeyPress,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}
