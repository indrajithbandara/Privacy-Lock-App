# Security

Privacy Lock implements deep system integration, cryptographic standards, and defensive UI techniques to ensure user-data isolation and device protection. This page outlines the security model, cryptographic properties, and known platform limitations.

---

## 1. Credential Security & Cryptographic Storage

### PIN Hashing Architecture
Privacy Lock never stores PINs in plain text. Instead, they are cryptographically hashed before being written to the Room database.
* **Algorithm**: PIN inputs are processed using standard hashing mechanisms (SHA-256 with a secure randomized salt) to generate a secure 64-character hash:
  ```kotlin
  // Conceptual code implemented inside security helpers
  fun hashPin(pin: String, salt: String): String {
      val messageDigest = MessageDigest.getInstance("SHA-256")
      val combined = (pin + salt).toByteArray(Charsets.UTF_8)
      val bytes = messageDigest.digest(combined)
      return bytes.joinToString("") { "%02x".format(it) }
  }
  ```
* **Decoy and Panic Salts**: Decoy and Panic PINs use independent salts and are checked in sequence to prevent rainbow-table reverse attacks.

---

## 2. Advanced PIN Vectors

### Panic PIN (Emergency Lockdown)
* **Behavior**: If the user is forced to enter a PIN but wishes to execute a silent lockdown, they can enter the registered **Panic PIN**.
* **Effect**: Entering the Panic PIN immediately:
  1. Revokes all current temporary unlocks in `AppLockManager`.
  2. Forces an immediate system exit to the Android Home Screen launcher.
  3. Optionally triggers an alarm event in the local audit logs.

### Decoy PIN (Forced Disclosure Defense)
* **Behavior**: If a third party forces disclosure of the PIN, entering the **Decoy PIN** unlocks the device but triggers a simulated crash or redirects to a secondary decoy profile dashboard with simulated system-only configurations.
* **Effect**: The application acts exactly as if it unlocked but blocks visibility of the real protected settings or vaults, giving the user plausible deniability.

### Random Keypad (Over-the-Shoulder & Smudge Shield)
* **Vulnerability**: standard keyboards leave visual smudges on screen glass and are susceptible to over-the-shoulder visual scanning.
* **Defense**: When **Random Keypad** is enabled:
  * Digits $1$ through $9$ are randomized into unique grids using `SecureRandom` on every overlay presentation.
  * **0** remains centered on the bottom row, with **Clear** on the left and **Backspace** on the right to maintain standardized operational ergonomics.

---

## 3. Dynamic Screenshot Protection Shield

Privacy Lock employs a dynamic security shield to block image capture, video recording, and recent-app caching of sensitive credentials.

### Mechanism: Window flag `FLAG_SECURE`
When Screenshot Protection is toggled **ON** (which is the default configuration), the system applies `WindowManager.LayoutParams.FLAG_SECURE` to the active window context:

```kotlin
fun Activity.toggleScreenshotShield(enabled: Boolean) {
    if (enabled) {
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    } else {
        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}
```

### Protection Coverage
* **Screenshots Blocked**: System-level button combinations or gesture actions are blocked with a toast indicating "Cannot capture screen due to security policy."
* **Screen Recording Inhibited**: Any continuous video capture outputs a pure black screen inside the recorded video file.
* **App Previews Hidden**: When navigating Android's "Recent Apps" or "Task Switcher", the app preview card is masked with a secure solid background, preventing cached data leakage.

---

## 4. Biometric Authentication

* **Platform Integration**: Leverages Android's `BiometricPrompt` system.
* **Security Level**: Configured to require `BIOMETRIC_STRONG` credentials (e.g., standard hardware-backed fingerprint, 3D face unlock).
* **Fallback**: Under the hood, if biometric verification fails multiple times, the prompt closes, requiring the user to fall back onto the primary 6-digit PIN.

---

## 5. Intruder Logger and Selfie Capture

* **Failed Attempt Tracker**: If an unauthorized party attempts to enter a PIN, each incorrect attempt increments a local session counter.
* **Selfie Trigger**: Upon reaching the configured limit (default: 5), the app activates the intruder logging module.
* **Avatar Mapping**: For privacy and compatibility, the system logs the exact timestamp and failed credential, mapping it to a localized avatar profile representing the failed breach within the **Intruder Selfie Timeline**.

---

## 6. Honest Security Limitations

While Privacy Lock provides excellent user-space security, developers and security researchers should be aware of the following system-level constraints:

1. **Accessibility Disabling**: On stock Android, system-level battery optimizations or accessibility settings may occasionally put the service to sleep. Users must whitelist the app from battery optimizations.
2. **Root Access Bypass**: If a device is rooted, an attacker with root privileges can directly inspect the `/data/data/com.example/databases/` directory to read binary databases or forcibly kill the background accessibility service.
3. **System Boot Delay**: Upon cold device boots, there is a short window of time (Direct Boot mode) where background services have not fully loaded. To mitigate this, file storage operations can be migrated to device-protected storage contexts.

---

[[Home]] | [<< App Lock Engine](App-Lock-Engine) | [[Permissions >>](Permissions)]
