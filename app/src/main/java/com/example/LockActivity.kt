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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.LiveRegionMode
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
import com.example.ui.components.InstalledAppIcon
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
    private var activePrompt: BiometricPrompt? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Hide recents screen representation by default for extra security
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        targetPackageName = intent.getStringExtra("TARGET_PACKAGE_NAME") ?: ""

        if (targetPackageName.isEmpty() || !AppLockManager.canProtect(this, targetPackageName)) {
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

            MyApplicationTheme(darkTheme = darkTheme, dynamicColor = config?.themeMode == "DYNAMIC") {
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val requested = intent.getStringExtra("TARGET_PACKAGE_NAME")
        if (requested != targetPackageName) {
            setIntent(intent)
            recreate()
        }
    }

    override fun onResume() {
        super.onResume()
        if (targetPackageName.isNotEmpty() && !isFinishing && !isDestroyed) {
            AppLockManager.reportLockPromptVisible(targetPackageName)
        }
    }

    override fun onPause() {
        AppLockManager.reportLockPromptVisible(null)
        super.onPause()
    }

    private fun unlockApp() {
        if (isFinishing || isDestroyed) return
        if (AppLockManager.grantForegroundSession(targetPackageName)) {
            finish()
        } else {
            // A stale or disconnected request must not reveal the underlying app.
            goHomeAndFinish()
        }
    }

    private fun showBiometricPrompt() {
        if (securityConfig?.biometricsEnabled != true || securityConfig?.hashedPin.isNullOrEmpty()) return
        val biometricManager = BiometricManager.from(this)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )
        if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
            val cipher = com.example.security.BiometricKey.newAuthenticationCipher() ?: run {
                android.widget.Toast.makeText(this, R.string.auth_biometric_missing, android.widget.Toast.LENGTH_LONG).show()
                return
            }
            val executor = ContextCompat.getMainExecutor(this)
            activePrompt?.cancelAuthentication()
            val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    activePrompt = null
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    activePrompt = null
                    try {
                        val authorizedCipher = result.cryptoObject?.cipher ?: return
                        authorizedCipher.doFinal(byteArrayOf(1))
                        unlockApp()
                    } catch (_: Exception) {
                        // The PIN remains available; a failed cryptographic operation is never success.
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                }
            })

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.auth_unlock_app, appName))
                .setSubtitle(getString(R.string.auth_subtitle))
                .setNegativeButtonText(getString(R.string.auth_use_pin))
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                .build()

            activePrompt = biometricPrompt
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        }
    }

    override fun onDestroy() {
        activePrompt?.cancelAuthentication()
        activePrompt = null
        super.onDestroy()
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
    var verifying by remember { mutableStateOf(false) }
    var retryAfter by remember { mutableStateOf(0L) }
    LaunchedEffect(retryAfter) {
        if (retryAfter > 0) {
            kotlinx.coroutines.delay(1000)
            retryAfter--
        }
    }

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
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .windowInsetsPadding(WindowInsets.safeDrawing),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
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
                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.auth_cancel_home))
            }

            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(modifier = Modifier.width(48.dp))
        }

        // The descriptive area can scroll; PIN feedback and keypad have their own
        // layout space so an authentication failure is not clipped by this weight.
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .offset(x = shakeOffset.value.dp)
        ) {
            InstalledAppIcon(
                packageName = packageName,
                size = 56,
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )

            Text(
                text = stringResource(R.string.auth_app_locked, appName),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = stringResource(R.string.auth_enter_six),
                style = MaterialTheme.typography.bodyMedium,
                color = TextWarmGray,
                textAlign = TextAlign.Center
            )

        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // PIN Dots Indicators; accessibility exposes the length, never the value.
            val digitStatus = stringResource(R.string.auth_digits_entered, pinBuffer.length)
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

            Text(digitStatus, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)

            Box(Modifier.fillMaxWidth().heightIn(min = 24.dp), contentAlignment = Alignment.Center) {
              if (retryAfter > 0 || errorMessage != null) {
                Text(
                    text = if (retryAfter > 0) stringResource(R.string.auth_retry_seconds, retryAfter) else errorMessage.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                )
              }
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
                        contentDescription = stringResource(R.string.auth_biometric_description),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.auth_biometric_verify),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }

            SecureKeypad(
                onNumberClick = { num ->
                    if (!verifying && retryAfter == 0L && pinBuffer.length < 6) {
                        errorMessage = null
                        pinBuffer += num

                        if (pinBuffer.length == 6) {
                            val submitted = pinBuffer
                            pinBuffer = ""
                            verifying = true
                            coroutineScope.launch {
                                try {
                                    val db = AppDatabase.getDatabase(context)
                                    val repository = com.example.data.PrivacyRepository(db, context.applicationContext)
                                    val result = repository.verifyPrimaryPin(submitted, appName)
                                    if (result.accepted) {
                                        onUnlockSuccess()
                                    } else {
                                        shakeTrigger++
                                        retryAfter = result.retryAfterSeconds
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
                randomizeLayout = config.randomizeKeypad,
                vibrateOnKeyPress = config.vibrateOnKeyPress,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (config.biometricsEnabled) {
                TextButton(
                    onClick = {
                        onTriggerBiometric()
                    },
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Text(
                        stringResource(R.string.auth_use_biometric),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}
