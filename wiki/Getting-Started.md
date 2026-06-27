# Getting Started

This guide details the end-to-end flow for setting up Privacy Lock on a physical Android device for the first time.

---

## 1. Initial Device Onboarding

When Privacy Lock is launched for the first time, you are guided through a multi-step verification and configuration wizard.

```
+---------------------------------------------------+
|               PRIVACY LOCK STARTUP                |
|                                                   |
|   1. Set Primary 6-Digit Vault PIN                |
|      [ _ _ _ _ _ _ ] (Default: 123456)             |
|                                                   |
|   2. Enable Required System Permissions:          |
|      [x] Accessibility Service                     |
|      [ ] Usage Statistics                          |
|      [ ] Draw Over Other Apps                      |
|                                                   |
|   3. Verify Lock Preferences                       |
|                                                   |
|   [ Get Started ]                                 |
+---------------------------------------------------+
```

### 1.1 Establish Your Primary Vault PIN
* By default, a fallback PIN of `123456` is configured inside the database.
* Upon first startup, you will be prompted with a setup dialog to establish your unique, secure 6-digit Vault PIN.
* Choose a code that is memorizable but difficult to guess. Avoid repeating numbers (e.g., `111111`) or simple sequences (e.g., `123456`).

---

## 2. Setting Up System Permissions

For security applications to run correctly on modern Android versions, three system-level permissions must be manually granted. The app's onboarding panel links directly to these system configuration pages:

### 2.1 Accessibility Service (Critical)
The application relies on this service to detect when other apps are opened, enabling real-time overlays.
1. Tap the **Enable Accessibility** card inside Privacy Lock.
2. The Android System Settings panel opens. Navigate to **Installed Apps** (or **Downloaded Services**).
3. Select **Privacy Lock Service**.
4. Toggle the switch to **ON** and confirm the safety prompt.

### 2.2 Usage Access Permission
Allows the application to read active app categories and background process transitions.
1. Tap the **Usage Statistics** card.
2. Find **Privacy Lock** in the system list.
3. Toggle the permission to **Allow usage tracking**.

### 2.3 Display Over Other Apps (Overlay)
Enables the custom keypad to draw a secure screen overlay blocking access to locked apps.
1. Tap the **System Overlay** card.
2. Select **Privacy Lock** from the list.
3. Toggle the permission to **Allow display over other apps**.

---

## 3. Locking Your First App

Once permissions are correctly configured, you will land on the **Privacy Center / Dashboard** screen:

1. Locate the **Application List** or **Shield Categories** on the dashboard.
2. Use the Search bar to find an application you wish to protect (e.g., *WhatsApp*, *Gmail*, or *Google Photos*).
3. Tap the **Lock Icon** (toggle switch) adjacent to the application card.
4. The icon color transitions to green, and a status entry is appended to the local **Audit Timeline** database, signifying that protection is active.

---

## 4. Unlocking Applications

Once an app is locked:

1. Open the protected application on your Android home screen.
2. Within milliseconds, the `AppAccessibilityService` detects the transition and launches the **Lock Screen Overlay** (`LockActivity`).
3. Enter your secure 6-digit PIN on the randomized or standard keypad.
4. On entering the correct code:
   - The overlay finishes.
   - The package is registered as temporarily unlocked.
   - Access to the target application is immediately restored.

---

[[Home]] | [<< Installation](Installation) | [[Architecture >>](Architecture)]
