# Privacy Center Integration Guide

This document describes the design, implementation, permission diagnostics, and security logs managed by the **Privacy Center** screen (`PrivacyCenterScreen.kt`).

---

## 🏗️ 1. Privacy Center Architecture Overview

The Privacy Center acts as a secure dashboard that provides real-time status diagnostics, security logs, and intruder detection details.

```
                      ┌──────────────────────────┐
                      │    PrivacyCenterScreen   │
                      └─────────────┬────────────┘
                                    │
         ┌──────────────────────────┼──────────────────────────┐
         ▼                          ▼                          ▼
┌─────────────────┐        ┌─────────────────┐        ┌─────────────────┐
│   Permissions   │        │  Security Logs  │        │  Intruder Logs  │
│   Diagnostics   │        │    Timeline     │        │   (Selfies)     │
└─────────────────┘        └─────────────────┘        └─────────────────┘
```

* **Permissions Diagnostics**: Shows the status of required system permissions (Accessibility, Usage Access, Overlay) and provides links to grant them.
* **Security Logs Timeline**: Displays a chronological list of security events (e.g., successful unlocks, setting modifications, blocked access attempts).
* **Intruder Logs**: Displays a list of failed authentication attempts, pairing each incident with a unique on-device silhouette icon.

---

## 🛠️ 2. Real-Time Permissions Diagnostics

The Privacy Center verifies that all required system permissions are active to ensure uninterrupted protection.

### Diagnostic Verification Logic (`SecurityUtils.kt`):
The app checks permissions using native system APIs:
* **Accessibility Service**: Checked by verifying if `AppAccessibilityService` is listed in the system's active accessibility services list.
* **Usage Access**: Checked by querying the system's `AppOpsManager` for `OPSTR_GET_USAGE_STATS`.
* **Overlay Permission**: Checked using `Settings.canDrawOverlays()`.

---

## 🪵 3. Chronological Security Timeline

Security logs are stored in the `timeline_events` table and displayed as a scrollable, chronological timeline list in the Privacy Center.

### Timeline Event Schema:
* **Timestamp**: Captured in epoch milliseconds and formatted locally (e.g., *YYYY-MM-DD HH:MM:SS*).
* **Event Type**: Categorized to support quick filtering:
  * `LOCK`: Logged when an app is secured.
  * `UNLOCK`: Logged when a protected app is unlocked successfully.
  * `INTRUDER`: Logged when a failed authentication attempt is blocked.
  * `CONFIG`: Logged when global settings are modified.
* **Description**: Detailed description of the event (e.g., *"Intruder attempt blocked on Settings app!"*).

---

## 📸 4. Intruder Detection Center

Failed authentication attempts on the lock overlay screen trigger an intruder logging flow. Because Privacy Lock does not use network or camera permissions, it logs intruder details inside local database tables:

* **Masked Passcode Entry**: Logs invalid PIN entries using masked characters (`******`) to prevent the intruder's attempted code from being visible in logcat.
* **Local Avatars**: Assigns a unique, illustrated silhouette icon (1 to 5) to each failed attempt to help organize and display incidents clearly.
* **Thread-Safe Insertion**: Intruder events are inserted asynchronously in the background using Kotlin Coroutines to prevent UI lags:
  ```kotlin
  coroutineScope.launch(Dispatchers.IO) {
      val db = AppDatabase.getDatabase(context)
      db.intruderSelfieDao().insertSelfie(
          IntruderSelfie(failedPinAttempt = "******", avatarId = randomAvatarId)
      )
  }
  ```
