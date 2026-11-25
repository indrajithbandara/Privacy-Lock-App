package com.example.security

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import androidx.core.content.ContextCompat
import com.example.LockActivity
import com.example.data.AppDatabase
import kotlinx.coroutines.*

class AppAccessibilityService : AccessibilityService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var pending: Job? = null
    private var registered = false
    private var lastActivePackage: String? = null

    private fun clearState() {
        pending?.cancel()
        pending = null
        lastActivePackage = null
        AppLockManager.clearTemporaryUnlocks()
        AppLockManager.setForegroundTarget(null)
    }

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) clearState()
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        clearState()
        AppLockManager.init(applicationContext)
        if (!registered) {
            ContextCompat.registerReceiver(this, screenOffReceiver,
                IntentFilter(Intent.ACTION_SCREEN_OFF), ContextCompat.RECEIVER_NOT_EXPORTED)
            registered = true
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val name = event.packageName?.toString() ?: return
        if (name == packageName && event.className?.toString() == LockActivity::class.java.name) return

        pending?.cancel()
        val previous = lastActivePackage
        lastActivePackage = name
        AppLockManager.setForegroundTarget(name)
        pending = scope.launch {
            try {
                val db = withContext(Dispatchers.IO) { AppDatabase.getDatabase(applicationContext) }
                val configuration = db.securityConfigDao().getConfigDirect()
                if (previous != null && previous != name && configuration?.autoLockTimeoutSeconds == 0) {
                    AppLockManager.lockPackage(previous)
                }
                if (!AppLockManager.canProtect(applicationContext, name)) return@launch
                // Read the current row instead of treating an empty asynchronous cache as authoritative.
                val record = db.lockedAppDao().getApp(name)
                if (lastActivePackage != name || record?.isLocked != true ||
                    AppLockManager.isPackageTemporarilyUnlocked(name)) return@launch
                startActivity(Intent(this@AppAccessibilityService, LockActivity::class.java).apply {
                    putExtra("TARGET_PACKAGE_NAME", name)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                })
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Platform or database errors leave protection degraded; do not trap the device owner.
                AppLockManager.clearTemporaryUnlocks()
            }
        }
    }

    override fun onInterrupt() = clearState()

    override fun onDestroy() {
        clearState()
        scope.cancel()
        if (registered) unregisterReceiver(screenOffReceiver)
        registered = false
        super.onDestroy()
    }
}
