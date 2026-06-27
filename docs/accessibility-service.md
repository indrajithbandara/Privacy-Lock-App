# Privacy Lock Accessibility Service Integration Guide

This document explains the design, integration, event flows, and optimization strategies of **AppAccessibilityService**—the background service responsible for intercepting app launches and protecting user data.

---

## 🏗️ 1. Accessibility Service Lifecycle & Event Loop

`AppAccessibilityService` extends Android's native `AccessibilityService` class. It registers with the system's accessibility framework to receive notification events whenever the user interacts with app windows.

```
                    ┌────────────────────────────┐
                    │      Android System OS     │
                    └─────────────┬──────────────┘
                                  │
                       Dispatches Window Event
                                  ▼
                    ┌────────────────────────────┐
                    │  AppAccessibilityService   │
                    └─────────────┬──────────────┘
                                  │
                         Checks Lock Status
                                  ▼
                    ┌────────────────────────────┐
                    │       AppLockManager       │
                    └─────────────┬──────────────┘
                                  │
                        If App is Locked:
                        Launches Overlay
                                  ▼
                    ┌────────────────────────────┐
                    │        LockActivity        │
                    └────────────────────────────┘
```

---

## 🛠️ 2. Event Handling Logic

The background service monitors active windows on the device and displays the secure lock keypad overlay when protected applications are launched.

### Core Implementation (`AppAccessibilityService.kt`):
```kotlin
override fun onAccessibilityEvent(event: AccessibilityEvent) {
    if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
        val packageName = event.packageName?.toString() ?: return
        
        // Do not lock our own locker app package
        if (packageName == applicationContext.packageName) {
            return
        }

        val last = lastActivePackage
        if (last != null && last != packageName) {
            // If auto-lock is configured for Immediately (0 seconds), lock the previously active package
            if (AppLockManager.getAutoLockTimeoutSeconds() == 0) {
                AppLockManager.lockPackage(last)
            }
        }
        lastActivePackage = packageName

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
```

---

## 🔐 3. Safe Window Launch flags

Launching an overlay activity from a background service requires setting specific flags on the launch intent to ensure the lock screen displays reliably and cannot be bypassed:

* **`FLAG_ACTIVITY_NEW_TASK`**: Required because the overlay is launched outside an active activity context, starting it in a new window stack.
* **`FLAG_ACTIVITY_CLEAR_TOP`**: Closes any existing instances of the lock screen overlay in the task stack before launching a new one. This prevents duplicate overlays from layering over each other.
* **`FLAG_ACTIVITY_SINGLE_TOP`**: Reuse the active lock overlay instance at the top of the window stack rather than recreating it if the user launches the same application multiple times in quick succession.

---

## 🔌 4. Session Expiration & Screen-Off Broadcasts

To protect your device if it is lost or stolen, unlocked application sessions should expire when the device screen turns off.

### Broadcast Receiver Integration:
Inside `onServiceConnected`, the service registers a dynamic, local `BroadcastReceiver` to listen for screen-off system broadcasts:

```kotlin
private val screenOffReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_SCREEN_OFF) {
            AppLockManager.clearTemporaryUnlocks()
        }
    }
}
```

When the device screen turns off, the receiver triggers `AppLockManager.clearTemporaryUnlocks()`. This immediately revokes all active session tokens, ensuring all protected applications are locked again before the user unlocks their device.

---

## ⚡ 5. Performance & Battery Optimization

Running a continuous background service can increase battery usage and CPU overhead. Privacy Lock implements several optimizations to minimize battery drain:

* **Minimized Event Footprint**: Configured to only listen for `TYPE_WINDOW_STATE_CHANGED` events, ignoring other events like scroll movements, text entries, or tap gestures.
* **In-Memory Caching**: Active lock statuses are cached in an in-memory `HashSet` (`lockedPackages`) within `AppLockManager`. This allows the service to check an app's lock status in sub-millisecond times, avoiding slow database queries on every window change.
* **Minimal Thread Allocation**: Intercept checks are executed on low-overhead thread pools using Kotlin Coroutines, keeping the main system thread free for active applications.
