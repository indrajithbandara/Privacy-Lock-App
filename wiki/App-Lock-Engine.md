# App Lock Engine

The **App Lock Engine** is the core operational kernel of Privacy Lock. It intercepts package transitions at the OS level, analyzes safety rules, and draws secure visual blocking interfaces without lag or performance degradation.

---

## 1. Interception Engine (Android Accessibility Service)

Instead of using resource-intensive and battery-draining polling services (such as inspecting `UsageStatsManager` on a constant thread loop), Privacy Lock relies on an Android `AccessibilityService` (`AppAccessibilityService`).

```
+-------------------------------------------------------------+
|                     ANDROID SYSTEM BUS                      |
+-------------------------------------------------------------+
                              |
                              v  AccessibilityEvent (TYPE_WINDOW_STATE_CHANGED)
+-------------------------------------------------------------+
|                AppAccessibilityService                      |
+-------------------------------------------------------------+
                              |
                              |  Extract packageName
                              v
+-------------------------------------------------------------+
|                    AppLockManager                           |
|                                                             |
|   1. Is package in locked list? (TRUE)                      |
|   2. Is package temporarily unlocked? (FALSE)               |
+-------------------------------------------------------------+
                              |
                              v  Package is locked and blocked
+-------------------------------------------------------------+
|                 Launch LockActivity                         |
|   - FLAG_ACTIVITY_NEW_TASK                                  |
|   - FLAG_ACTIVITY_CLEAR_TOP                                 |
+-------------------------------------------------------------+
```

### Why Accessibility Service?
1. **Real-time Performance**: Receives system events immediately upon window state changes.
2. **Deterministic Blocking**: Guarantees that the target app window cannot render content before the security overlay replaces it.
3. **Battery Efficiency**: Operates event-driven. Consumes negligible power compared to active background process scanning.

---

## 2. In-Memory State Cache (`AppLockManager`)

Database queries represent synchronous IO operations that can freeze frames if executed during window transformations. To prevent UI stutter:
* `AppLockManager` acts as a thread-safe, memory-cached lookup system.
* During system startup (`onServiceConnected`), it collects the current set of protected packages from the SQLite database via a cold Flow and copies them into a thread-safe hash set:
  ```kotlin
  private val lockedPackages = mutableSetOf<String>()
  ```
* Subsequent package matching checks are executed directly against this in-memory hash set in $\mathcal{O}(1)$ complexity:
  ```kotlin
  fun isPackageLocked(packageName: String): Boolean {
      return synchronized(lockedPackages) {
          lockedPackages.contains(packageName)
      }
  }
  ```

---

## 3. Temporary Unlock and Timeout Lifecycle

When a package is successfully unlocked by entering the valid PIN, it must not trigger an overlay loop every time the user shifts screens within the same app. 

### Unlock Flow
1. Upon successful authentication, the `LockActivity` invokes:
   ```kotlin
   AppLockManager.temporarilyUnlockPackage(packageName)
   ```
2. The package and current timestamp are cached in memory:
   ```kotlin
   temporarilyUnlockedPackages.add(packageName)
   unlockedTimestamps[packageName] = System.currentTimeMillis()
   ```

### Timeout Expiry Verification
On subsequent window events for that package, `AppLockManager` verifies if the temporary unlock session remains active:
```kotlin
fun isPackageTemporarilyUnlocked(packageName: String): Boolean {
    return synchronized(temporarilyUnlockedPackages) {
        if (!temporarilyUnlockedPackages.contains(packageName)) return false
        val timeout = synchronized(this) { autoLockTimeoutSeconds }
        if (timeout > 0) {
            val unlockTime = unlockedTimestamps[packageName] ?: return false
            val elapsed = (System.currentTimeMillis() - unlockTime) / 1000
            if (elapsed > timeout) {
                // Session expired, remove unlock clearance
                temporarilyUnlockedPackages.remove(packageName)
                unlockedTimestamps.remove(packageName)
                return false
            }
        }
        return true
    }
}
```

---

## 4. Overlay Window Architecture (`LockActivity`)

`LockActivity` is a dedicated fullscreen Android Activity configured with specific window parameters:

### Launcher Flags
To launch an Activity from a background Accessibility Service, the intent is configured with:
* `FLAG_ACTIVITY_NEW_TASK`: Required for launch operations outside standard activity task trees.
* `FLAG_ACTIVITY_CLEAR_TOP`: Assures any existing instances of the lock screen are cleared.
* `FLAG_ACTIVITY_SINGLE_TOP`: Reuses existing instances to prevent multiple lock screens stacking if window changes trigger rapidly.

### Double-Back Protection
To prevent users from dismissing the lock overlay by simply pressing the system back gesture/button, `LockActivity` handles the back event explicitly to exit back to the Home Screen:
```kotlin
onBackPressedDispatcher.addCallback(this) {
    // Force direct exit to Android desktop launcher
    val intent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(intent)
}
```

---

[[Home]] | [<< Project Structure](Project-Structure) | [[Security >>](Security)]
