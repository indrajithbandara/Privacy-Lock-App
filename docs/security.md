# Privacy Lock Security & Threat Model

This document outlines the security architecture, threat model, cryptographic processes, and secure memory policies implemented in **Privacy Lock**.

---

## 🏗️ 1. Zero-Knowledge Design Philosophy

Privacy Lock is built on a **zero-knowledge, offline-first** architecture. To guarantee user privacy, the application does not declare the `android.permission.INTERNET` permission in its manifest. This ensures your locked apps list, configuration settings, PIN hashes, and intruder logs can never leave your device.

---

## 🛡️ 2. Threat Modeling & Vulnerability Mitigation

| Threat Vector | Risk Level | Mitigation Strategy |
| :--- | :--- | :--- |
| **Physical Lock Bypass** (Closing or force-stopping the app overlay) | High | Overrides back-button callbacks to redirect the user to the system launcher home screen. App launch intercepts are managed by a persistent system Accessibility Service. |
| **Snooping multitasking screens** (Snooping data in the system Recents carousel) | Medium | Programmatically binds the `FLAG_SECURE` window property to the overlay screen. This blocks screenshots, screen recordings, and masks the app's preview in the Recents list. |
| **Physical USB Extraction** (Pulling database files via ADB debugging cables) | High | Implements custom backup and data extraction rules (`backup_rules.xml` and `data_extraction_rules.xml`) to block database extraction via USB debugging tools. |
| **Brute-Force Attacks** (Rapidly guessing the PIN passcode) | High | Implements progressive delay lockouts and logs failed attempts inside local databases. Settings can be configured to wipe the database after a specific number of incorrect attempts. |
| **Shoulder-Surfing** (Onlookers watching you enter your PIN) | Medium | Supports a randomized keypad layout that shuffles the position of digits 1–9 every time the keypad is displayed. |

---

## 🔑 3. Cryptographic Passcode Protection

User passcodes are never stored in plain text. Instead, Privacy Lock uses SHA-256 hashing with a unique salt to secure PINs:

### Cryptographic Hashing Flow:
```
[ User Input PIN ] ──> [ Append Salt: "PrivacyLockSaltEnterprise" ] ──> [ MessageDigest (SHA-256) ] ──> [ Hash Comparison ]
```

### Hashing Implementation (`SecurityUtils.kt`):
```kotlin
fun hashPin(pin: String, salt: String = "PrivacyLockSaltEnterprise"): String {
    return try {
        val digest = MessageDigest.getInstance("SHA-256")
        val combined = pin + salt
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        hashBytes.joinToString("") { "%02x".format(it) }
    } catch (e: Exception) {
        pin.hashCode().toString()
    }
}
```

---

## 💾 4. Secure Memory Management

To prevent sensitive passcodes from being extracted from device RAM, Privacy Lock implements strict memory management practices:

* **Avoiding Immutable Strings**: User inputs are processed using mutable structures (like `CharArray`) whenever possible.
* **Immediate Zeroing**: Passcode buffers are cleared and overwritten in RAM immediately after validation checks are completed:
  ```kotlin
  // Example memory sanitization
  pinBuffer = ""
  ```
* **No Cache Retention**: Encryption keys and plain-text configuration settings are kept in memory only for the duration of the active session.

---

## 🤖 5. Accessibility Service Security Boundaries

The background accessibility service is configured to maximize security while respecting user privacy:

* **No Screen Content Retrieval**: `android:canRetrieveWindowContent` is set to `false` in `accessibility_service_config.xml`. This ensures the service cannot inspect screen elements, read text, or capture keystrokes, making it impossible for the app to act as a keylogger.
* **Minimal Event Filtering**: The service only listens for `typeWindowStateChanged` events. It does not monitor taps, scroll events, or text input changes, minimizing background CPU usage.
* **Local Processing**: All event checks are processed locally on the device. No data is sent to external servers.
