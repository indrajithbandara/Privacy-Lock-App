# Privacy Lock Database & Storage Model

This document explains the data storage architecture, database tables, DAO queries, and local file configurations of the **Privacy Lock** Android app.

---

## 🏗️ 1. Room Database Architecture

Privacy Lock uses the native Android **Room Persistence Library** as an abstraction layer over a local, high-performance SQLite database. 

### Database Configuration:
* **Database Name**: `privacy_lock_db`
* **Room Schema Version**: `2`
* **Export Schema**: `false`
* **Concurrency**: Thread-safe database access is managed using Kotlin Coroutines on `Dispatchers.IO`.

---

## 🗂️ 2. Database Tables & Schemas

The database contains four core tables to store configuration settings, application lists, security events, and intruder logs:

```
                  ┌──────────────────────────────────────┐
                  │          privacy_lock_db             │
                  └──────────────────┬───────────────────┘
         ┌───────────────────────────┼───────────────────────────┐
         ▼                           ▼                           ▼
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│   locked_apps   │         │ security_config │         │ timeline_events │
└─────────────────┘         └─────────────────┘         └─────────────────┘
```

### 1. `locked_apps` Table
Stores the list of applications detected on the device and their lock statuses.
* **Primary Key**: `packageName: String`
* **Properties**:
  * `name: String` (User-facing name of the application)
  * `category: String` (*System, Social, Finance, Utilities, Other*)
  * `isLocked: Boolean` (Whether the app requires PIN verification to open)
  * `isFavorite: Boolean` (Saves the app to the top of the list)
  * `isPinned: Boolean` (Pinning status)
  * `iconName: String` (Maps to vector drawable icon helpers)

### 2. `security_config` Table
Stores global configuration settings and passcode hashes. This table only ever contains a single row with a key of `"global"`.
* **Primary Key**: `id: String` (Default value: `"global"`)
* **Properties**:
  * `hashedPin: String` (SHA-256 hash of the Master PIN)
  * `hashedDecoyPin: String` (SHA-256 hash of the Decoy PIN)
  * `hashedPanicPin: String` (SHA-256 hash of the Panic PIN)
  * `biometricsEnabled: Boolean` (Enables fingerprint or face unlock option)
  * `autoLockTimeoutSeconds: Int` (Delay in seconds before locking an app again)
  * `randomizeKeypad: Boolean` (Shuffles numbers on the keypad)
  * `vibrateOnKeyPress: Boolean` (Provides haptic feedback on button taps)
  * `lockNewlyInstalledApps: Boolean` (Automatically locks newly installed apps)
  * `intruderDetectionEnabled: Boolean` (Enables logging failed access attempts)
  * `intruderSelfieEnabled: Boolean` (Captures intruder details on failed logins)
  * `failedAttemptLimit: Int` (Failed attempts before locking input)
  * `screenshotProtection: Boolean` (Enables FLAG_SECURE on screens)
  * `autoLockNewApps: Boolean` (Automatically locks newly installed apps)
  * `temporaryUnlockDurationMinutes: Int` (Temporary unlock session length)
  * `decoyModeType: String` (*NONE, FILTER, FULL*)
  * `themeMode: String` (*LIGHT, DARK, SYSTEM*)
  * `stealthCode: String` (Passcode to launch the app via dialer)

### 3. `timeline_events` Table
Stores chronological security logs, displayed in the Privacy Center.
* **Primary Key**: `id: Int` (Auto-incremented)
* **Properties**:
  * `timestamp: Long` (Epoch milliseconds timestamp)
  * `type: String` (*LOCK, UNLOCK, INTRUDER, CONFIG*)
  * `description: String` (Details of the logged event)
  * `appName: String?` (Optional name of the app related to the event)

### 4. `intruder_selfies` Table
Logs failed authentication attempts to protect against physical intruders.
* **Primary Key**: `id: Int` (Auto-incremented)
* **Properties**:
  * `timestamp: Long` (Epoch milliseconds timestamp)
  * `failedPinAttempt: String` (The incorrect passcode, masked as `"******"`)
  * `avatarId: Int` (Maps to an illustrated silhouette icon for the attempt)

---

## 🚀 3. Data Access Object (DAO) Queries

Room uses customized Data Access Objects to interact with database tables securely.

### DAO Implementation Highlights (`Daos.kt`):
* **Reactive Streams**: Select queries return Kotlin `Flow` lists. This ensures the UI updates automatically in real-time when underlying database values change.
* **Conflict Resolution**: Writes and updates use `OnConflictStrategy.REPLACE` to ensure configuration settings are updated safely without duplicating database records.

---

## 🛠️ 4. Concurrency & Performance

To prevent database operations from slowing down the UI, Privacy Lock implements strict concurrency policies:

* **Background Dispatchers**: All database transactions are executed on the IO thread pool using `Dispatchers.IO`:
  ```kotlin
  CoroutineScope(Dispatchers.IO).launch {
      db.timelineEventDao().insertEvent(event)
  }
  ```
* **Write-Ahead Logging (WAL)**: The SQLite database is configured with Write-Ahead Logging enabled. This allows read operations to execute in parallel with write transactions, preventing database access bottlenecks.
* **In-Memory Cache**: Active lock statuses are cached in an in-memory `HashSet` (`lockedPackages`) within `AppLockManager.kt`. This allows the accessibility service to verify an app's lock status in sub-millisecond times, avoiding slow database queries on every window transition.
