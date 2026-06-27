# Database Schema & Storage

Privacy Lock is designed as an offline-first utility. All configuration variables, locking states, event logs, and intruder records are stored locally in an embedded SQLite database managed by the **Android Room Persistence Library**.

---

## 1. Database Architecture & Schema

The database consists of four tables defined under `AppDatabase.kt`. 

```
+----------------------------------------------------------------------------+
|                                APPDATABASE                                 |
+----------------------------------------------------------------------------+
       |                  |                    |                   |
       v                  v                    v                   v
+--------------+   +-----------------+   +------------------+   +------------+
|  locked_apps |   | security_config |   |  timeline_events |   |  intruder_ |
|              |   |                 |   |                  |   |   selfies  |
+--------------+   +-----------------+   +------------------+   +------------+
```

---

## 2. Table Specifications (Entities)

### 2.1 Table: `locked_apps`
Stores the lock states and sorting preferences of installed packages.

* **Kotlin Class**: `LockedApp`
* **Primary Key**: `packageName: String`

| Column Name | SQL Type | Nullable | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| `packageName` | `TEXT` | No | *None* | Unique application package ID (e.g., `com.whatsapp`). |
| `name` | `TEXT` | No | *None* | User-friendly app name (e.g., "WhatsApp"). |
| `category` | `TEXT` | No | *None* | Categorization label ("System", "Social", "Finance", "Utilities", "Other"). |
| `isLocked` | `INTEGER` | No | `0` (False) | Represents if the application requires PIN authentication. |
| `isFavorite` | `INTEGER` | No | `0` (False) | True if the app is pinned to the top of the list. |
| `isPinned` | `INTEGER` | No | `0` (False) | Sorting preference. |
| `iconName` | `TEXT` | No | *None* | Maps to the visual drawable resource of the app logo. |

---

### 2.2 Table: `security_config`
Contains global variables and security parameters.

* **Kotlin Class**: `SecurityConfig`
* **Primary Key**: `id: String` (Pinned to `"global"`)

| Column Name | SQL Type | Nullable | Default | Description |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `TEXT` | No | `"global"` | Singleton primary key. |
| `hashedPin` | `TEXT` | No | `""` | SHA-256 hashed Primary PIN string. |
| `hashedDecoyPin` | `TEXT` | No | `""` | SHA-256 hashed Decoy PIN string. |
| `hashedPanicPin` | `TEXT` | No | `""` | SHA-256 hashed Panic PIN string. |
| `biometricsEnabled` | `INTEGER` | No | `0` (False) | Enables biometric bypass. |
| `autoLockTimeoutSeconds` | `INTEGER` | No | `0` | Lock expiry timeout value. |
| `randomizeKeypad` | `INTEGER` | No | `0` (False) | Enables secure dynamic keypad randomizer. |
| `vibrateOnKeyPress` | `INTEGER` | No | `1` (True) | Enables button haptics. |
| `lockNewlyInstalledApps` | `INTEGER` | No | `0` (False) | Enables monitoring of app install broadcasts. |
| `intruderDetectionEnabled` | `INTEGER` | No | `1` (True) | Tracks failed credential entry attempts. |
| `intruderSelfieEnabled` | `INTEGER` | No | `1` (True) | Log and capture intrusion attempts. |
| `failedAttemptLimit` | `INTEGER` | No | `5` | Error limit before lockouts or alerts occur. |
| `screenshotProtection` | `INTEGER` | No | `1` (True) | Sets dynamic system `FLAG_SECURE` window shields. |
| `autoLockNewApps` | `INTEGER` | No | `0` (False) | Identical behavior to auto-locking installs. |
| `temporaryUnlockDurationMinutes`| `INTEGER` | No | `0` | In-memory unlock duration fallback limit. |
| `decoyModeType` | `TEXT` | No | `"NONE"` | Determines decoy behaviors ("NONE", "CRASH", "DECOY"). |
| `themeMode` | `TEXT` | No | `"SYSTEM"` | App skin configurations ("SYSTEM", "LIGHT", "DARK"). |
| `stealthCode` | `TEXT` | No | `"#1234"` | Special dialer bypass code. |

---

### 2.3 Table: `timeline_events`
Chronological logs of locks, configurations, and validation attempts.

* **Kotlin Class**: `TimelineEvent`
* **Primary Key**: `id: INTEGER (Auto-Generated)`

| Column Name | SQL Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| `id` | `INTEGER` | No | Auto-incrementing unique index. |
| `timestamp` | `INTEGER` | No | Epoch timestamp in milliseconds. |
| `type` | `TEXT` | No | Categorized actions ("LOCK", "UNLOCK", "INTRUDER", "CONFIG"). |
| `description` | `TEXT` | No | Human-readable explanation of the occurrence. |
| `appName` | `TEXT` | Yes | App reference name if the event relates to a target app. |

---

### 2.4 Table: `intruder_selfies`
Logs localized metadata for failed unlock attempts.

* **Kotlin Class**: `IntruderSelfie`
* **Primary Key**: `id: INTEGER (Auto-Generated)`

| Column Name | SQL Type | Nullable | Description |
| :--- | :--- | :--- | :--- |
| `id` | `INTEGER` | No | Auto-incrementing unique index. |
| `timestamp` | `INTEGER` | No | Epoch timestamp in milliseconds. |
| `failedPinAttempt` | `TEXT` | No | The invalid PIN string entered by the intruder. |
| `avatarId` | `INTEGER` | No | Local resource mapping id for displaying custom vector silhouettes. |

---

## 3. Data Access Objects (DAOs) and Flow Emissions

All queries are declared inside `data/DAOs.kt` and emit data reactively via Kotlin **Flows**. This ensures that whenever database entries are mutated (e.g., locking an application), the changes stream to the corresponding collectors (such as `AppLockManager` or `DashboardScreen`) without requiring manual polling.

---

## 4. Database Migration Strategy

To support ongoing development across software releases, Privacy Lock handles database versioning using Room's built-in migration pathways:

```kotlin
// Declared in AppDatabase.kt
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE security_config ADD COLUMN screenshotProtection INTEGER NOT NULL DEFAULT 1")
    }
}
```
* **Fallback Behavior**: If a schema transition lacks a registered migration path during system upgrades, the database executes `.fallbackToDestructiveMigration()`. This recreates the SQLite database tables from scratch, protecting against crashes at the expense of resetting settings back to defaults.

---

## 5. Security Configuration Backup & Serialization

Privacy Lock provides a robust text-based backup and restore mechanism to serialize current configurations:

* **Serialization**: Converts all `SecurityConfig` properties into a single semi-colon-separated key-value payload:
  ```
  hashedPin:a1b2c3d4...;hashedDecoyPin:e5f6g7h8...;biometricsEnabled:true;...
  ```
* **Parsing & Restoration**: The parser splits settings by `;` and recovers configuration states incrementally, validating entries against structural defaults to prevent corrupted imports.

---

[[Home]] | [<< Settings](Settings) | [[User Guide >>](User-Guide)]
