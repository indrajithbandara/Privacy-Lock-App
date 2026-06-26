package com.example.security

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import com.example.LockActivity

class AppAccessibilityService : AccessibilityService() {

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                AppLockManager.clearTemporaryUnlocks()
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        AppLockManager.init(applicationContext)
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenOffReceiver, filter)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            
            // Do not lock our own locker app package
            if (packageName == applicationContext.packageName) {
                return
            }

            // Verify if package is locked and has not been temporarily unlocked
            if (AppLockManager.isPackageLocked(packageName)) {
                if (!AppLockManager.isPackageTemporarilyUnlocked(packageName)) {
                    val intent = Intent(this, LockActivity::class.java).apply {
                        putExtra("TARGET_PACKAGE_NAME", packageName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
                    startActivity(intent)
                }
            }
        }
    }

    override fun onInterrupt() {
        // No-op
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(screenOffReceiver)
        } catch (e: Exception) {
            // Safe fallback
        }
        super.onDestroy()
    }
}
