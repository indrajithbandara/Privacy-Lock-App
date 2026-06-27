# User Guide

This guide is a comprehensive reference manual designed to walk you through every feature and functional workflow of Privacy Lock.

---

## 1. Primary Workflows & User Interfaces

```
+--------------------------------------------------------+
|                      DASHBOARD                         |
|                                                        |
|   Search apps...                                       |
|   +------------------------------------------------+   |
|   | [X] Social      [ ] Finance       [ ] System   |   |
|   +------------------------------------------------+   |
|                                                        |
|   App List:                                            |
|   [Shield] WhatsApp       [ Locked Toggle: ON ]        |
|   [Shield] Gmail          [ Locked Toggle: ON ]        |
|   [Shield] Photos         [ Locked Toggle: OFF]        |
|                                                        |
+--------------------------------------------------------+
```

### 1.1 Managing the App Lock List
* **App Lock Status**: The **Dashboard** lists all installed applications sorted alphabetically, by categories (e.g., *Social*, *Finance*), or by favorites.
* **Toggling Locks**: Simply tap the **Switch** next to any app. When the switch glows in the Material 3 primary theme color, the app is protected.
* **Instant Protection**: The change is applied immediately. The background Accessibility Service updates its cache, and launching the newly locked app will trigger the keypad within milliseconds.

---

## 2. Advanced Security Credential Modes

Privacy Lock supports three distinct credentials, each triggering a different system response depending on the situation:

```
                  +--------------------------+
                  |    Keypad PIN Entry      |
                  +--------------------------+
                               |
       +-----------------------+-----------------------+
       |                       |                       |
       v                       v                       v
 [ Primary PIN ]        [ Decoy PIN ]           [ Panic PIN ]
       |                       |                       |
  Unlock Vault           Trigger Simulated       Immediate System Exit
  Normal Access           Crash / Profile        Revoke All clearances
```

### 2.1 The Primary PIN (Your Secret Vault Key)
* **Default**: `123456`
* **Format**: 6-Digit Numeric
* **Purpose**: Fully authenticates you, giving you normal access to protected applications and the primary settings dashboard.

### 2.2 The Decoy PIN (Plausible Deniability)
* **Purpose**: Used when you are forced or coerced to unlock your device by an external party.
* **Behavior**:
  * Displays a realistic, simulated "App Stopped Responding" crash dialog or navigates to a secondary decoy profile dashboard with simulated system-only configurations.
  * Masks your real data and shields your protected settings entirely, giving you plausible deniability.

### 2.3 The Panic PIN (Instant Emergency Lockdown)
* **Purpose**: Used during high-risk scenarios where you need to lock down all secure partitions instantly.
* **Behavior**:
  * Immediately clears all active temporary unlock authorizations in memory.
  * Directs the OS back to the system launcher (Android desktop), leaving zero trace of active vault processes.

---

## 3. Reviewing Security Logs and Intruders

Privacy Lock includes on-device monitoring to track unauthorized access attempts.

```
+--------------------------------------------------------+
|                  INTRUDER HISTORY                      |
|                                                        |
|   Failed Attempt Log:                                  |
|   +------------------------------------------------+   |
|   |  [Avatar]  Timestamp: 10:42:15 AM              |   |
|   |            Code Tried: 882911                  |   |
|   |            Status: Logged Failed Entry         |   |
|   +------------------------------------------------+   |
|                                                        |
+--------------------------------------------------------+
```

### 3.1 The Security Audit Timeline
* Located on the **Timeline** screen.
* Records a historical trail of system modifications, app lock updates, setting changes, and unlock operations.
* Entries are sorted chronologically, helping you verify that no configuration changes occurred without your knowledge.

### 3.2 Intruder Logs & Silhouette Icons
* Whenever a user enters an incorrect PIN multiple times (based on your configuration, e.g., 5 attempts):
  * The app registers an intrusion attempt.
  * The attempt details (timestamp, tried code) are recorded.
  * The log displays a unique, stylized vector silhouette representing the intruder, allowing you to quickly scan and review security incidents.

---

## 4. Troubleshooting and Recovery

### 4.1 What if I forget my primary PIN?
* Because Privacy Lock runs entirely client-side and offline, **your PIN cannot be recovered if lost**. There is no "Forgot Password" server connection.
* **Recommendation**: Store your Primary PIN or configuration backup string securely inside an offline password manager.

### 4.2 The App Lock is not blocking apps anymore
* This is almost always caused by Android suspending the background Accessibility Service due to aggressive battery optimization.
* **Fix**:
  1. Open **Settings > App Info > Privacy Lock**.
  2. Tap **Battery** and select **Unrestricted / Don't Optimize**.
  3. Re-enable the service in **Settings > Accessibility**.

---

[[Home]] | [<< Database](Database) | [[FAQ >>](FAQ)]
