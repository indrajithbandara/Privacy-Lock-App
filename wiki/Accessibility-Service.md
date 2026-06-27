# Accessibility Service Integration

The **Accessibility Service** represents the core daemon and interceptor pipeline of Privacy Lock. It intercepts package changes at the OS level to ensure that target applications are locked deterministically without battery drain.

---

## 🛠️ 1. Why an Accessibility Service?

On the Android platform, monitoring foreground applications can be accomplished in multiple ways, but most methods come with heavy trade-offs:

| Method | Battery Consumption | Response Latency | Setup Complexity | Reliability |
| :--- | :--- | :--- | :--- | :--- |
| **Polling `UsageStatsManager`** | 🔴 High (Requires a tight constant thread loop) | 🔴 Slow (Slight delay allows content flash) | 🟢 Low | 🟡 Medium |
| **Active Service Polling** | 🔴 Extreme (Keeps CPU awake) | 🔴 Slow | 🟡 Medium | 🔴 Low (Killed by OS) |
| **Accessibility Service** | 🟢 Low (Event-Driven) | 🟢 Instant (Deterministic) | 🔴 High (Requires manual user grant) | 🟢 High (Restarted by OS) |

By implementing `AccessibilityService` (`AppAccessibilityService`), Privacy Lock runs in a highly optimized, event-driven state. The service sleeps until the Android System broadcasts a package transition, consuming zero battery in the background.

---

## 🔄 2. Event Interception Lifecycle

The diagram below traces how a system-level event flows through the `AppAccessibilityService`:

```
+-----------------------------------------------------------+
|              SYSTEM ACTION: User launches App             |
+-----------------------------------------------------------+
                              |
                              v  AccessibilityEvent
+-----------------------------------------------------------+
| AppAccessibilityService: onAccessibilityEvent()           |
| - Verify eventType == TYPE_WINDOW_STATE_CHANGED           |
| - Extract packageName (e.g. "com.whatsapp")               |
+-----------------------------------------------------------+
                              |
                              v
+-----------------------------------------------------------+
| AppLockManager.isPackageLocked("com.whatsapp")            |
+-----------------------------------------------------------+
         |                                         |
         v (Is Locked & NOT Temporarily Unlocked)   v (Is Unlocked)
+---------------------------------------+   +-----------------------+
| Start LockActivity Secure Overlay     |   | Allow User Access     |
| - Set FLAG_ACTIVITY_NEW_TASK          |   +-----------------------+
| - Set FLAG_ACTIVITY_CLEAR_TOP         |
| - Record "LOCK" event in Database     |
+---------------------------------------+
```

### Core Code Snippet
The core handler inside `AppAccessibilityService.kt`:

```kotlin
override fun onAccessibilityEvent(event: AccessibilityEvent) {
    if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
        val packageName = event.packageName?.toString() ?: return
        
        // Prevent intercepting our own package to avoid recursive loops
        if (packageName == this.packageName) return

        if (AppLockManager.isPackageLocked(packageName)) {
            if (!AppLockManager.isPackageTemporarilyUnlocked(packageName)) {
                launchLockOverlay(packageName)
            }
        }
    }
}
```

---

## 🔒 3. Service Security & Privacy Guarantees

Because the Accessibility Service is a highly privileged Android system feature, Privacy Lock enforces strict privacy barriers:

1. **Zero Text Capture**: The service configures its capabilities in XML (`res/xml/accessibility_service_config.xml`) to only request **window state changes**. It is strictly incapable of reading text inputs, observing button clicks, or scanning passwords in other apps.
2. **Zero Network Permissions**: The application does not declare the `android.permission.INTERNET` flag in its manifest. Because it has no internet capabilities, it is physically impossible for the app to exfiltrate private user information or app usage data.
3. **Local Evaluation**: All matching evaluations, package lookups, and state determinations are computed inside local JVM memory using thread-safe caches, with no server dependencies.

---

## ⚙️ 4. Whitelisting and Maintenance

On certain Android custom skins (e.g., Samsung One UI, Xiaomi MIUI/HyperOS), background services are aggressively killed to conserve battery. To ensure stable operation, configure the following settings on your device:

* **Battery Optimization**: Set Privacy Lock's battery usage profile to **"Unrestricted"**.
* **Background Auto-Start**: Enable background auto-start permissions if supported by your OEM.
* **Lock in Recents**: Open Android's Recent Apps view, tap on the Privacy Lock app icon, and select **"Lock this app"** to prevent memory clearing.

---

[[Home]] | [<< Security Model](Security-Model) | [[Lock Engine >>](Lock-Engine)]
