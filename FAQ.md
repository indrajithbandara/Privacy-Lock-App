# Frequently Asked Questions (FAQ)

This FAQ provides answers to common questions about Privacy Lock, grouped by audience.

---

## 📱 For End Users

### Q1: Does Privacy Lock require an internet connection?
No. Privacy Lock is built as an offline-first security tool. It does not declare the `android.permission.INTERNET` permission in its `AndroidManifest.xml`. This physically prevents the app from sending any data off the device.

### Q2: How does the Decoy PIN feature work?
The Decoy PIN is an alternative 6-digit credential that you can set in the Settings screen. When entered on the lock screen, it unlocks the overlay and logs a normal access event. This helps you avoid suspicion under coercion or shoulder-surfing without exposing your actual Master PIN.

### Q3: What happens if I forget my Master PIN?
Because Privacy Lock stores credentials as salted SHA-256 hashes locally on your device with no cloud sync, there is no password recovery mechanism. If you forget your Master PIN, you must clear the app's storage via Android Settings (`Settings > Apps > Privacy Lock > Storage > Clear Data`). This resets the application, but you will need to re-configure your locked apps and settings.

### Q4: Why does the app require the Accessibility Service permission?
To intercept app launches and show the security overlay before the target app displays its content, Privacy Lock must detect window state changes. The standard, most reliable way to achieve this on modern Android versions is through a background Accessibility Service.

### Q5: Does the app collect or upload my biometric data?
No. Privacy Lock uses the official Android BiometricPrompt API. All biometric enrollment, matching, and hardware interactions are handled securely by the Android OS kernel and Secure Element / Trusted Execution Environment (TEE). The app only receives a cryptographic success/fail callback.

### Q6: Why does the background lock service sometimes stop working?
Some Android device manufacturers use aggressive battery-saving algorithms that terminate long-running background services. If you experience this:
1. Navigate to your device's battery settings.
2. Select **Privacy Lock**.
3. Change battery optimization to **Unrestricted**.
4. Ensure the accessibility service remains enabled under `Settings > Accessibility > Installed Services`.

### Q7: What is the purpose of the "Random Keypad" feature?
The Random Keypad scrambles the positions of digits 1–9 on every entry attempt while keeping `0`, `Clear`, and `Backspace` in their standard bottom positions. This prevents shoulder-surfers from guessing your PIN based on hand movements and mitigates smudge-pattern analysis on your physical screen.

---

## 🛠️ For Developers

### Q1: How does the app intercept background launches?
The `AppAccessibilityService` listens for `TYPE_WINDOW_STATE_CHANGED` events. When a window change occurs, the service retrieves the package name of the foreground activity, checks it against the local Room database via `AppLockManager`, and starts `LockActivity` if the app is locked and not currently in an unlocked session.

### Q2: What is the architectural relationship between the service and ViewModel?
The `AppAccessibilityService` and `LockActivity` interact with `AppLockManager` (a thread-safe singleton) to check and update session-level lock states. The `PrivacyViewModel` interacts with `PrivacyRepository` to query and mutate database records representing user preferences, locked packages, and timeline logs.

### Q3: How is screenshot protection implemented?
We programmatically apply the standard window flag `WindowManager.LayoutParams.FLAG_SECURE` to the `LockActivity` window within its `onCreate` callback if the configuration setting is enabled:
```kotlin
if (screenshotProtectionEnabled) {
    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
}
```
This blocks system screenshot actions, screen recorders, and task preview caching in the Recents screen.

### Q4: Can I run unit tests without an Android device?
Yes. The project includes local JVM-level unit and Robolectric tests in `app/src/test`. These simulate the Android environment on the JVM, enabling fast verification of the database, view models, and utility classes without launching an emulator.

### Q5: What database library is used?
We use Android Jetpack Room with Kotlin Symbol Processing (KSP) to compile-time verify SQLite schemas and generate high-performance data access objects (DAOs).

---

## 🔒 For Security Researchers

### Q1: How are PINs stored?
PINs are never stored in plaintext. They are hashed using SHA-256 with a unique, cryptographically secure salt generated via `java.security.SecureRandom`. The salt is stored alongside the hash in the local database.

### Q2: How is database access secured?
Database files are located in the application's protected private directory (`/data/data/com.example/databases/`). Additionally, we explicitly set `allowBackup="false"` and exclude the database directory in `backup_rules.xml` and `data_extraction_rules.xml` to prevent physical extraction via ADB backup or device migration cables.

### Q3: How is the RNG seeded?
We use `java.security.SecureRandom` instead of `kotlin.random.Random` for all security-sensitive operations, including random keypad generation. This ensures that the generated sequences are cryptographically strong and unpredictable.
