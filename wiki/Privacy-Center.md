# Privacy Center & Incident Center

The **Privacy Center** is the centralized auditing dashboard within Privacy Lock. It provides users with full visibility into local security events, configuration changes, and unauthorized access attempts.

---

## 📈 1. Interactive Auditing Dashboard

The Privacy Center Home screen organizes device telemetry into elegant, readable Material 3 cards:

* **Security Health Score**: Calculates a dynamic safety metric based on the number of locked high-risk apps, setup credentials, randomized keypad toggles, and screen-shield states.
* **Timeline Events**: Presents a reverse-chronological list of security events (e.g., app locks, settings modifications, PIN changes, and failed entry events).
* **Intruder Alerts**: Displays a warning badge on the dashboard immediately upon application launch if new intrusion attempts have been logged.

---

## 📝 2. Local Event Logger & DB Flow

All security audits are logged to the local Room database under the `timeline_events` table:

```
                  +-----------------------------------+
                  |           Security Event          |
                  +-----------------------------------+
                                    |
                                    v
                  +-----------------------------------+
                  |        TimelineEvent Entity       |
                  | - id: Integer (Auto-generated)    |
                  | - timestamp: Long (Epoch time)    |
                  | - type: String (LOCK/UNLOCK/etc.) |
                  | - description: String             |
                  | - appName: String? (Optional)     |
                  +-----------------------------------+
                                    |
                                    v
                  +-----------------------------------+
                  |         SQLite Database           |
                  +-----------------------------------+
```

### Flow Consumption
Because DAOs utilize asynchronous Kotlin Flows, the Privacy Center UI automatically recomposes to display new events the instant they are written to the database:

```kotlin
// Emitted dynamically to the Compose UI layer
val allEvents: Flow<List<TimelineEvent>> = timelineEventDao.getAllEventsFlow()
```

---

## 🛑 3. Intruder Center (Failed Attempt Tracker)

When unauthorized personnel attempt to bypass application locks, the **Intruder Center** activates defensive logging:

### 3.1 Failed Entry Thresholds
The global configuration file establishes how many failed PIN entry events are permitted before logging an incident (the default limit is 5 attempts).

### 3.2 Visual Incident Logger
For privacy and runtime system compatibility, Privacy Lock replaces physical camera selfies with a creative, secure **Visual Incident Silhouette System**:
1. Upon reaching the failed PIN entry limit, a detailed security entry is logged with the incorrect PIN attempted and the precise timestamp.
2. The entry is associated with a dynamically selected, distinct **Decorative Silhouette Vector Avatar** inside the database:
   ```kotlin
   data class IntruderSelfie(
       val id: Int = 0,
       val timestamp: Long,
       val failedPinAttempt: String,
       val avatarId: Int // Maps to decorative silhouette vector draws
   )
   ```
3. When the user logs in with their master Primary PIN, a prominent warning card and badge appear, alerting them to check the Intruder Center timeline.

---

## 💾 4. Portable Backup Security

The Privacy Center provides a text-based, encrypted backup mechanism allowing users to securely transfer or backup settings.

### 4.1 Backup Payload Schema
The backup engine compiles the configuration database into a highly organized, semi-colon-separated text payload string:
```
hashedPin:e2c97a5f...;hashedDecoyPin:3fc411b...;randomizeKeypad:true;vibrateOnKeyPress:false
```

### 4.2 Safe Import Engine
During restoration:
1. The backup string is split into individual key-value properties.
2. The configuration engine validates each key's structure against strict system defaults.
3. If valid, changes are saved to the SQLite database via a unified database transaction, immediately updating `AppLockManager` caches in real-time.

---

[[Home]] | [<< Database](Database) | [[Settings >>](Settings)]
