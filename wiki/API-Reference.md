# API Reference

This technical API reference documents the key classes, interfaces, objects, and data access methods in Privacy Lock.

---

## 1. AppLockManager (`com.example.security`)

A thread-safe, in-memory state manager that caches locked packages and tracks temporary unlock sessions.

```kotlin
object AppLockManager
```

### Methods

#### `init`
Initializes the cache by reading the `locked_apps` list and `security_config` from the local Room database using coroutines.
* **Parameters**: `context: Context`
* **Signature**:
  ```kotlin
  fun init(context: Context)
  ```

#### `isPackageLocked`
Checks the in-memory cache to see if the specified package is locked.
* **Parameters**: `packageName: String`
* **Returns**: `Boolean` — `true` if the package is registered as locked in the database cache.
* **Signature**:
  ```kotlin
  fun isPackageLocked(packageName: String): Boolean
  ```

#### `isPackageTemporarilyUnlocked`
Verifies if a package has a valid temporary unlock clearance and has not exceeded the auto-lock timeout.
* **Parameters**: `packageName: String`
* **Returns**: `Boolean`
* **Signature**:
  ```kotlin
  fun isPackageTemporarilyUnlocked(packageName: String): Boolean
  ```

#### `temporarilyUnlockPackage`
Registers a package as temporarily unlocked and records the current timestamp to track the session duration.
* **Parameters**: `packageName: String`
* **Signature**:
  ```kotlin
  fun temporarilyUnlockPackage(packageName: String)
  ```

#### `lockPackage`
Revokes any active temporary unlock clearance for the specified package, requiring a PIN entry on its next launch.
* **Parameters**: `packageName: String`
* **Signature**:
  ```kotlin
  fun lockPackage(packageName: String)
  ```

#### `clearTemporaryUnlocks`
Revokes all active temporary unlock clearances. This is typically invoked when the device screen turns off.
* **Signature**:
  ```kotlin
  fun clearTemporaryUnlocks()
  ```

---

## 2. AppAccessibilityService (`com.example.security`)

The background service that monitors active app windows and triggers the lock screen overlay when needed.

```kotlin
class AppAccessibilityService : AccessibilityService()
```

### Methods

#### `onAccessibilityEvent`
Called by the system when a window state change occurs. Extracts the foreground package name and launches `LockActivity` if the app is locked.
* **Parameters**: `event: AccessibilityEvent`
* **Signature**:
  ```kotlin
  override fun onAccessibilityEvent(event: AccessibilityEvent)
  ```

---

## 3. PrivacyViewModel (`com.example.ui`)

The unified controller and state container that manages the app's UI state.

```kotlin
class PrivacyViewModel(application: Application) : AndroidViewModel(application)
```

### Flow Properties

#### `isScreenshotProtectionEnabled`
A `StateFlow<Boolean>` that emits the current screenshot protection status from the persistent Preferences DataStore.
* **Signature**:
  ```kotlin
  val isScreenshotProtectionEnabled: StateFlow<Boolean>
  ```

#### `securityConfig`
A `StateFlow<SecurityConfig?>` containing the current global security settings from Room.
* **Signature**:
  ```kotlin
  val securityConfig: StateFlow<SecurityConfig?>
  ```

### Methods

#### `setScreenshotProtection`
Updates the screenshot protection setting in both the DataStore and the Room database.
* **Parameters**:
  * `enabled: Boolean`: The desired setting.
  * `onUpdated: (Boolean) -> Unit`: Callback triggered upon successful write.
* **Signature**:
  ```kotlin
  fun setScreenshotProtection(enabled: Boolean, onUpdated: (Boolean) -> Unit)
  ```

---

## 4. DAOs (`com.example.data`)

### LockedAppDao
Manages database operations for the `locked_apps` table.

* **`getAllApps()`**: `Flow<List<LockedApp>>` — Streams all installed applications.
* **`updateLockedStatus(packageName: String, isLocked: Boolean)`**: Updates an application's lock status.

### SecurityConfigDao
Manages database operations for the `security_config` table.

* **`getConfig()`**: `Flow<SecurityConfig?>` — Streams the global security configuration.
* **`insertOrUpdate(config: SecurityConfig)`**: Writes or updates the configuration.

---

[[Home]] | [<< Roadmap](Roadmap) | [[Developer Guide >>](Developer-Guide)]
