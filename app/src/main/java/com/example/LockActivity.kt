package com.example

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.data.AppDatabase
import com.example.data.SecurityConfig
import com.example.security.AppLockManager
import com.example.security.SecurityUtils
import com.example.ui.components.SecureKeypad
import com.example.ui.components.SimulatedAppIcon
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextWarmGray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.Executor

class LockActivity : FragmentActivity() {

    private var targetPackageName: String = ""
    private var appName: String = "Application"
    private var securityConfig: SecurityConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hide recents screen representation by default for extra security
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        targetPackageName = intent.getStringExtra("TARGET_PACKAGE_NAME") ?: ""

        if (targetPackageName.isEmpty()) {
            finish()
            return
        }

        val pm = packageManager
        appName = try {
            val appInfo = pm.getApplicationInfo(targetPackageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            targetPackageName
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goHomeAndFinish()
            }
        })

        setContent {
            var config by remember { mutableStateOf<SecurityConfig?>(null) }

            // Load configuration from Room directly
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(applicationContext)
                    val dbConfig = db.securityConfigDao().getConfigDirect() ?: SecurityConfig()
                    withContext(Dispatchers.Main) {
                        config = dbConfig
                        securityConfig = dbConfig
                        if (dbConfig.biometricsEnabled) {
                            showBiometricPrompt()
                        }
                    }
                }
            }

            val darkTheme = when (config?.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    config?.let { sc ->
                        LockScreenContent(
                            appName = appName,
                            packageName = targetPackageName,
                            config = sc,
                            onUnlockSuccess = {
                                unlockApp()
                            },
                            onCancel = {
                                goHomeAndFinish()
                            },
                            onTriggerBiometric = {
                                showBiometricPrompt()
                            }
                        )
                    } ?: Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    private fun unlockApp() {
        AppLockManager.temporarilyUnlockPackage(targetPackageName)
        finish()
    }

    private fun showBiometricPrompt() {
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val executor = ContextCompat.getMainExecutor(this)
            val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    unlockApp()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock $appName")
                .setSubtitle("Authenticate to access locked application")
                .setNegativeButtonText("Use PIN")
                .build()

            biometricPrompt.authenticate(promptInfo)
        }
    }

    private fun goHomeAndFinish() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }
}

@Composable
fun LockScreenContent(
    appName: String,
    packageName: String,
    config: SecurityConfig,
    onUnlockSuccess: () -> Unit,
    onCancel: () -> Unit,
    onTriggerBiometric: () -> Unit
) {
    var pinBuffer by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Shake animation state
    var shakeTrigger by remember { mutableStateOf(0) }
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(shakeTrigger) {
        if (shakeTrigger > 0) {
            for (i in 1..3) {
                shakeOffset.animateTo(20f, spring(dampingRatio = 0.2f, stiffness = 1200f))
                shakeOffset.animateTo(-20f, spring(dampingRatio = 0.2f, stiffness = 1200f))
            }
            shakeOffset.animateTo(0f, spring(dampingRatio = 0.2f, stiffness = 1200f))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier.testTag("lock_close_button")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Cancel and go home")
            }

            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        // App Details and PIN display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .weight(1f, fill = false)
                .offset(x = shakeOffset.value.dp)
        ) {
            SimulatedAppIcon(
                iconName = "android",
                packageName = packageName,
                size = 84,
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )

            Text(
                text = "$appName is Locked",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Enter secure 6-digit privacy PIN to access",
                style = MaterialTheme.typography.bodyMedium,
                color = TextWarmGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // PIN Dots Indicators
            Row(
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
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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

        // Custom Security Keypad Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (config.biometricsEnabled) {
                TextButton(
                    onClick = onTriggerBiometric,
                    modifier = Modifier.testTag("biometric_trigger_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Authenticate with fingerprint or face",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Verify with Biometrics",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            SecureKeypad(
                onNumberClick = { num ->
                    if (pinBuffer.length < 6) {
                        errorMessage = null
                        pinBuffer += num

                        if (pinBuffer.length == 6) {
                            val targetHash = if (config.hashedPin.isEmpty()) {
                                SecurityUtils.hashPin("123456")
                            } else {
                                config.hashedPin
                            }
                            val decoyHash = config.hashedDecoyPin
                            val panicHash = config.hashedPanicPin
                            val inputHash = SecurityUtils.hashPin(pinBuffer)

                            when {
                                inputHash == targetHash -> {
                                    onUnlockSuccess()
                                }
                                decoyHash.isNotEmpty() && inputHash == decoyHash -> {
                                    // Decoy PIN unlocks silently but puts it in decoy state
                                    onUnlockSuccess()
                                }
                                panicHash.isNotEmpty() && inputHash == panicHash -> {
                                    // Panic PIN triggers emergency exit or safety close
                                    onCancel()
                                }
                                else -> {
                                    // Log Intruder Attempt asynchronously in background
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val db = AppDatabase.getDatabase(context)
                                        val randomAvatarId = (1..5).random()
                                        val maskedAttempt = "*".repeat(6)
                                        db.intruderSelfieDao().insertSelfie(
                                            com.example.data.IntruderSelfie(
                                                failedPinAttempt = maskedAttempt,
                                                avatarId = randomAvatarId
                                            )
                                        )
                                        db.timelineEventDao().insertEvent(
                                            com.example.data.TimelineEvent(
                                                type = "INTRUDER",
                                                description = "Intruder attempt blocked on $appName! Safe lock activated."
                                            )
                                        )
                                    }
                                    pinBuffer = ""
                                    shakeTrigger++
                                    errorMessage = "Incorrect security PIN. Access denied."
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
                randomizeLayout = config.randomizeKeypad,
                vibrateOnKeyPress = config.vibrateOnKeyPress,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (config.biometricsEnabled) {
                // Forgot PIN option with biometric bypass
                TextButton(
                    onClick = {
                        onTriggerBiometric()
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        "Forgot PIN? Recover with Biometrics",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}
