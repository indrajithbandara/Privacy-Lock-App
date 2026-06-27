# App Lock Engine

The **App Lock Engine** represents the core operational pipeline of Privacy Lock. It bridges the gap between background event interception and active, high-priority UI overlays.

---

## ⚙️ 1. Engine Core Pipeline

The lock engine coordinates package state evaluation and view rendering within milliseconds of package switches:

```
+------------------+         +------------------+         +------------------+
|   Accessibility  |  Check  |  AppLockManager  | Launch  |   LockActivity   |
|     Service      | ------> |   In-Memory      | ------> |  Secure Overlay  |
| (Package Changed)|         |   State Cache    |         |  (Compose UI)    |
+------------------+         +------------------+         +------------------+
```

---

## 🔒 2. In-Memory State Cache (`AppLockManager`)

To prevent stutters or dropped frames during window transitions, Privacy Lock avoids querying the SQLite database directly from the accessibility event thread. Instead:

1. **State Bootstrapping**: Upon service startup, `AppLockManager` queries the Room database to fetch the list of locked packages and copies them into a synchronized memory hash set (`lockedPackages`).
2. **Instant Match Evaluator**: When a package change occurs, `AppLockManager` evaluates matching logic in $\mathcal{O}(1)$ time against the memory cache.
3. **Reactive Updates**: Whenever a package is locked or unlocked inside the settings panel, the updates are immediately synchronized back to the `lockedPackages` cache, ensuring real-time responsiveness.

---

## ⏱️ 3. Temporary Unlocks and Timeout Policies

When a user enters the valid PIN on the secure keypad overlay, they expect a seamless transition and should not be repeatedly locked out during active use.

* **Temporary Unlock Storage**: Upon successful PIN verification, the package name and timestamp are added to a temporary unlock register:
  ```kotlin
  private val temporarilyUnlockedPackages = mutableSetOf<String>()
  private val unlockedTimestamps = mutableMapOf<String, Long>()
  ```
* **Grace Period Evaluation**: Whenever a target app event is monitored, `AppLockManager` evaluates if the temporary clearance remains active:
  ```kotlin
  fun isPackageTemporarilyUnlocked(packageName: String): Boolean {
      val timeoutSeconds = synchronized(this) { autoLockTimeoutSeconds }
      if (timeoutSeconds <= 0) return false // Immediate Lock policy
      
      val unlockTime = unlockedTimestamps[packageName] ?: return false
      val elapsed = (System.currentTimeMillis() - unlockTime) / 1000
      
      if (elapsed > timeoutSeconds) {
          // Grace period has expired. Re-engage lock.
          temporarilyUnlockedPackages.remove(packageName)
          unlockedTimestamps.remove(packageName)
          return false
      }
      return true
  }
  ```

---

## 🪟 4. Overlay Window Architecture

The overlay blocking screen is rendered via `LockActivity`, a customized fullscreen Jetpack Compose Activity.

### 4.1 Intent Flag Optimization
To spin up a new window context directly from a background Service thread, the launcher intent is configured with precise operational flags:

```kotlin
val intent = Intent(context, LockActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
            Intent.FLAG_ACTIVITY_CLEAR_TOP or 
            Intent.FLAG_ACTIVITY_SINGLE_TOP
    putExtra(LockActivity.EXTRA_PACKAGE_NAME, targetPackage)
}
context.startActivity(intent)
```
* **`FLAG_ACTIVITY_NEW_TASK`**: Grants execution permissions outside the standard task stack hierarchy.
* **`FLAG_ACTIVITY_CLEAR_TOP`**: Safely clears prior instances of the lock screen from memory.
* **`FLAG_ACTIVITY_SINGLE_TOP`**: Reuses the active `LockActivity` instance rather than layering duplicate screens if rapid events occur.

### 4.2 Home-Key and Back-Gesture Shielding
If the user attempts to bypass the lock screen by pressing the hardware back button or swipe gestures:
* **Back-Gesture Capture**: The `LockActivity` captures and intercept back events using Compose back handling, immediately rerouting execution to the Android Home screen. This ensures the protected app is never shown:

```kotlin
onBackPressedDispatcher.addCallback(this) {
    val homeIntent = Intent(Intent.ACTION_MAIN).apply {
        addCategory(Intent.CATEGORY_HOME)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    startActivity(homeIntent)
}
```

---

[[Home]] | [<< Accessibility Service](Accessibility-Service) | [[Database >>](Database)]
