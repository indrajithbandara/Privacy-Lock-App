# Settings Reference

This page provides an in-depth reference for all customizable configurations available in Privacy Lock.

---

## 1. Core Security Configurations

### 1.1 Screenshot Protection
* **Default Value**: `ON` (True)
* **Purpose**: Prevents standard screen captures, system recent-apps previews, and third-party screen recordings of the vault and overlays.
* **How it works**: Programmatically sets the standard `FLAG_SECURE` window layout flags inside `MainActivity.kt` and `LockActivity.kt`.
* **Side effects**: Disables casting the application screen to external monitors via Chromecast or wireless display streaming.

### 1.2 Random Keypad
* **Default Value**: `OFF` (False)
* **Purpose**: Shuffles numeric buttons to protect against shoulder-surfing and fingerprint smudge patterns.
* **How it works**: Randomizes the list positions of digits $1$ through $9$ inside `SecureKeypad`.
* **Side effects**: Increases cognitive load slightly during PIN entries as button positions shift dynamically on each lock.

### 1.3 Biometric Unlock
* **Default Value**: `OFF` (False)
* **Purpose**: Allows unlocking protected apps using the device's hardware-backed fingerprints or face templates.
* **How it works**: Integrates Android Jetpack's `BiometricPrompt` with `BIOMETRIC_STRONG` credentials.
* **Side effects**: Reduces protection if other users have their fingerprints registered on the same device.

---

## 2. Lock Engine Adjustments

### 2.1 Auto-Lock Timeout
* **Default Value**: `0` seconds (Immediately)
* **Purpose**: Specifies how long an unlocked application remains accessible before requiring the PIN again.
* **Options**: `0` (Immediately), `15s`, `30s`, `1m`, `5m`, `10m`, `30m`.
* **How it works**: Tracks elapsed time since successful unlock using `System.currentTimeMillis()` in `AppLockManager`.
* **Side effects**: Setting long timeouts increases convenience but exposes unlocked apps to unauthorized access if the device is left unattended.

### 2.2 Lock Newly Installed Apps
* **Default Value**: `OFF` (False)
* **Purpose**: Automatically applies lock protection to newly installed packages.
* **How it works**: Monitors system-wide app install broadcasts to auto-insert new entries into the `locked_apps` Room database table with `isLocked = true`.
* **Side effects**: Can lead to accidental locks on benign utilities or background system plugins.

---

## 3. Intruder Logger Settings

### 3.1 Intruder Detection
* **Default Value**: `ON` (True)
* **Purpose**: Enables monitoring of failed PIN entry attempts.
* **How it works**: Increments an in-memory error counter inside `LockActivity` on wrong inputs.

### 3.2 Failed Attempt Limit
* **Default Value**: `5`
* **Purpose**: Number of consecutive wrong inputs allowed before logging an intrusion event.
* **Options**: `3`, `5`, `10`
* **How it works**: Triggers an alert callback once the threshold is crossed.

### 3.3 Intruder Selfie Log
* **Default Value**: `ON` (True)
* **Purpose**: Captures and saves the intruder's attempt metadata (timestamp, entered code) and creates a security portrait map.
* **How it works**: Generates a database row inside the `intruder_selfies` Room table and binds a distinct vector avatar representation to the intrusion log.

---

## 4. Personalization

### 4.1 Theme Mode
* **Default Value**: `SYSTEM`
* **Options**: `SYSTEM`, `LIGHT`, `DARK`
* **Purpose**: Sets the application's visual layout palette.
* **How it works**: Binds the root Compose `MaterialTheme` configuration to the chosen value.

### 4.2 Stealth Code
* **Default Value**: `#1234`
* **Purpose**: Launches Privacy Lock when it is configured to be hidden from the standard system launcher tray.
* **How it works**: Registers a phone dialer broadcast receiver to launch `MainActivity` when the user dials the configured code.

---

[[Home]] | [<< Permissions](Permissions) | [[Database >>](Database)]
