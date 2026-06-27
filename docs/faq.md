# Privacy Lock Frequently Asked Questions (FAQ)

This FAQ answers common questions about the security, privacy, and configuration options of **Privacy Lock**.

---

## 🔒 1. Security & Privacy

### Q1: Is Privacy Lock really offline-first?
**Yes.** Privacy Lock does not declare the `android.permission.INTERNET` permission in its `AndroidManifest.xml`. This ensures that your configuration settings, PIN hashes, and intruder logs can never leave your device.

### Q2: Can Privacy Lock read my screen contents or keystrokes?
**No.** In `/app/src/main/res/xml/accessibility_service_config.xml`, the parameter `android:canRetrieveWindowContent` is set to `false`. This ensures that the background service cannot read screen elements, copy text, or capture keystrokes, making it impossible for the app to act as a keylogger.

### Q3: How is my PIN secured in the database?
Your PIN is never stored in plain text. Privacy Lock uses **SHA-256 hashing with a unique salt** to secure passcodes:
`hash = SHA-256(PIN + "PrivacyLockSaltEnterprise")`
This ensures that your passcodes are protected, even if someone gains access to the database.

---

## 🛠️ 2. Core Features & Configurations

### Q4: Why does the app require Accessibility permissions?
The **Accessibility Service** is the most reliable way to monitor app launches locally on Android. It allows the app to detect when a protected application is opened and display the lock screen overlay immediately, without using high-overhead CPU polling.

### Q5: Can I lock system applications like Settings?
**Yes.** You can secure both user and system applications (including Settings, Play Store, and package installers) to prevent other users from changing your settings or uninstalling Privacy Lock.

### Q6: How does the Decoy PIN work?
The **Decoy PIN** is an alternative passcode that successfully unlocks the app overlay but hides your actual locked apps list. This protects your privacy if you are forced or coerced into unlocking your device.

### Q7: What is the Panic PIN?
The **Panic PIN** is an emergency passcode that instantly closes the app overlay and returns you to the home screen. This allows you to quickly abort authentication attempts in emergency situations.

---

## 🔧 3. Troubleshooting & Recovery

### Q8: What if I forget my Master PIN?
Because Privacy Lock runs entirely offline and does not use cloud recovery, there is no automatic password reset option:
* **Decoy PIN / Backup Access**: If you set up a Decoy PIN, you can use it to access the app's basic settings and export a backup string.
* **Dialer Stealth Code**: If configured, you can launch the app recovery interface by dialing your stealth code (e.g., `#1234`) in your device's phone app.
* **Restore from Backup**: If you saved a backup string, you can clear the app's data in system settings and import your backup to restore your settings and passcode.

### Q9: What happens if I uninstall Privacy Lock?
Uninstalling Privacy Lock will remove the application and its local SQLite database. All locked applications will immediately become unlocked and accessible. To prevent unauthorized uninstallation, we recommend locking system settings and package managers inside Privacy Lock.
