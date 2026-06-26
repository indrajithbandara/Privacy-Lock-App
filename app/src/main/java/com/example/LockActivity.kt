package com.example

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

class LockActivity : ComponentActivity() {

    private var targetPackageName: String = ""
    private var appName: String = "Application"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
            var securityConfig by remember { mutableStateOf<SecurityConfig?>(null) }

            // Load configuration from Room directly
            LaunchedEffect(Unit) {
                withContext(Dispatchers.IO) {
                    val db = AppDatabase.getDatabase(applicationContext)
                    val config = db.securityConfigDao().getConfigDirect() ?: SecurityConfig()
                    withContext(Dispatchers.Main) {
                        securityConfig = config
                        if (config.screenshotProtection) {
                            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                        }
                    }
                }
            }

            val darkTheme = when (securityConfig?.themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = darkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    securityConfig?.let { config ->
                        LockScreenContent(
                            appName = appName,
                            packageName = targetPackageName,
                            config = config,
                            onUnlockSuccess = {
                                AppLockManager.temporarilyUnlockPackage(targetPackageName)
                                finish()
                            },
                            onCancel = {
                                goHomeAndFinish()
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
    onCancel: () -> Unit
) {
    var pinBuffer by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

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
            IconButton(onClick = onCancel) {
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
            modifier = Modifier.weight(1f, fill = false)
        ) {
            SimulatedAppIcon(
                iconName = "android",
                packageName = packageName,
                size = 72,
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
                text = "Enter security PIN to access application",
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
                for (i in 1..4) {
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

            Spacer(modifier = Modifier.height(16.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Custom Security Keypad
        SecureKeypad(
            onNumberClick = { num ->
                if (pinBuffer.length < 4) {
                    errorMessage = null
                    pinBuffer += num

                    if (pinBuffer.length == 4) {
                        val targetHash = if (config.hashedPin.isEmpty()) {
                            SecurityUtils.hashPin("1234")
                        } else {
                            config.hashedPin
                        }
                        val decoyHash = config.hashedDecoyPin
                        val inputHash = SecurityUtils.hashPin(pinBuffer)

                        if (inputHash == targetHash) {
                            onUnlockSuccess()
                        } else if (decoyHash.isNotEmpty() && inputHash == decoyHash) {
                            // Decoy Pin unlocks
                            onUnlockSuccess()
                        } else {
                            // Handle Intruder Selfie Logging asynchronously inside database
                            coroutineScope.launch(Dispatchers.IO) {
                                val db = AppDatabase.getDatabase(context)
                                val randomAvatarId = (1..5).random()
                                val maskedAttempt = "*".repeat(4)
                                db.intruderSelfieDao().insertSelfie(
                                    com.example.data.IntruderSelfie(
                                        failedPinAttempt = maskedAttempt,
                                        avatarId = randomAvatarId
                                    )
                                )
                                db.timelineEventDao().insertEvent(
                                    com.example.data.TimelineEvent(
                                        type = "INTRUDER",
                                        description = "Intruder attempt blocked on $appName! Secure capture recorded."
                                    )
                                )
                            }
                            pinBuffer = ""
                            errorMessage = "Incorrect security PIN. Access denied."
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
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}
