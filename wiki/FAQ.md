# Frequently Asked Questions (FAQ)

This index compiles 30 distinct, practical, and highly detailed questions and answers addressing configuration, execution, permissions, and security properties of Privacy Lock.

---

## Setup & Onboarding

### Q1: What is Privacy Lock?
Privacy Lock is an offline-first Android security application that locks target applications with a customized PIN, Decoy PIN, Panic PIN, and Android Biometrics, using an Accessibility Service overlay.

### Q2: What are the minimum system requirements for running the app?
The application requires Android 8.0 (API Level 26) or newer and a minimum of 2GB of RAM to prevent the OS from killing background services.

### Q3: Why does Privacy Lock require three major system permissions?
The app needs Accessibility (to monitor active apps), Display Over Other Apps (to render the keypad overlay), and Usage Statistics (to classify and filter applications by category).

### Q4: Does the application connect to any cloud server?
No. Privacy Lock is strictly offline-first. All configurations, logs, and databases reside exclusively inside secure local device storage.

### Q5: How do I change the default Vault PIN on first launch?
Navigate to **Settings > Change PIN**, enter your current PIN (default: `123456`), select a new 6-digit numeric combination, and tap Save.

### Q6: Can I use a 4-digit PIN instead of a 6-digit PIN?
No. To maintain strong defensive entropy against brute-force attempts, Privacy Lock strictly enforces a 6-digit numeric pattern.

---

## Credentials & Unlocking

### Q7: What is the Decoy PIN and when should I use it?
The Decoy PIN is for forced-disclosure situations. Entering it mimics a realistic crash (e.g., "App Stopped Responding") or opens a simulated profile dashboard with system-only settings, keeping your real vault secure.

### Q8: What is the Panic PIN?
The Panic PIN is an emergency combination that instantly revokes all active temporary unlock clearances in memory and exits directly to the Android Home Screen launcher, leaving zero active traces of secure vault states.

### Q9: Can I enable both Biometrics and PIN security?
Yes. When Biometric Lock is enabled, the app opens a secure system biometric prompt. If biometric verification fails, you can fall back to entering your 6-digit PIN.

### Q10: How do I recover a forgotten primary PIN?
Because Privacy Lock runs entirely client-side and offline, **your PIN cannot be recovered if lost**. There is no "Forgot Password" or recovery email server.

### Q11: How long does a temporary application unlock last?
The duration depends on your configured **Auto-Lock Timeout** (e.g., Immediately, 15 seconds, 1 minute, etc.). 

### Q12: Why is the numeric keypad randomized?
When **Random Keypad** is ON, digits $1$ through $9$ are shuffled randomly on every presentation. This protects against over-the-shoulder visual sniffing and glass-smudge pattern attacks.

---

## App Lock Engine & Background Behavior

### Q13: Does the App Lock Engine run in the background constantly?
Yes. The engine relies on Android's event-driven `AccessibilityService`, which remains resident in memory. However, it only processes events when active window state changes occur, making it highly battery-efficient.

### Q14: Why do some applications show content briefly before the lock overlay triggers?
This occurs on devices with low RAM or heavy CPU throttling. To minimize lag, Privacy Lock caches locked packages in-memory inside `AppLockManager`, reducing lookup times to under 5 milliseconds.

### Q15: How does Privacy Lock handle split-screen or multi-window mode?
`LockActivity` uses specific window flags that handle resize operations, ensuring the secure keypad scales correctly and remains overlayed in split-screen or multi-window modes.

### Q16: What happens to locked apps when I restart my device?
The background Accessibility Service is automatically restarted by the Android System upon reboot, restoring protection as soon as you unlock your device (Direct Boot mode).

### Q17: Does the app automatically lock newly installed applications?
Yes, if **Lock Newly Installed Apps** is enabled. It monitors system-wide app install broadcasts to automatically register new apps as locked in the database.

### Q18: Can I lock system-critical applications like Settings or Google Play Services?
Yes. However, be cautious when locking the Android Settings app, as doing so requires entering your PIN to modify system settings or uninstall Privacy Lock.

---

## Security & System Hardening

### Q19: How is my PIN secured in the database?
PINs are cryptographically hashed using standard hashing algorithms (SHA-256 with a secure randomized salt) before being written to local storage. Plaintext PINs are never stored.

### Q20: What is Screenshot Protection?
Screenshot Protection dynamically sets the standard `FLAG_SECURE` window flags on the vault and overlays. This blocks screenshots, screen recording, and masks app previews with a secure solid background in the Recent Apps list.

### Q21: Can a screen recorder capture my PIN entry?
No. When Screenshot Protection is enabled, system-level and third-party screen recorders only capture a solid black screen over any Privacy Lock windows.

### Q22: What is the Intruder Logger?
The Intruder Logger tracks failed PIN entry attempts. Once the failed attempt threshold (e.g., 5 attempts) is crossed, it records the incident with a timestamp, the code tried, and maps it to a stylized vector avatar.

### Q23: Can Privacy Lock prevent its own uninstallation?
To prevent uninstallation, you can lock system applications like **Settings** and the **Google Play Store**, which blocks access to the device's uninstall manager.

### Q24: What are the security limitations on a rooted device?
On a rooted device, an attacker with root privileges can inspect the database folder directly, bypass the Accessibility Service, or kill background processes.

---

## Troubleshooting & Support

### Q25: Why does the App Lock stop working after a few hours?
This is typically caused by aggressive brand-specific battery optimizers killing background services. To fix this, set Privacy Lock's battery usage to **Unrestricted / Don't Optimize**.

### Q26: Why is the Accessibility Service option grayed out with a "Restricted Setting" warning on Android 13+?
This is an Android security feature for sideloaded apps. Go to **Settings > Apps > Privacy Lock**, tap the three dots in the top-right corner, select **Allow restricted settings**, and verify with your device PIN.

### Q27: How can I back up my settings?
Go to **Settings > Backup Configurations** to generate a secure serialized text backup string of your settings, which you can save in a secure offline location.

### Q28: How do I restore my settings from a backup?
Go to **Settings > Restore Configurations**, paste your secure backup string, and tap Restore. The parser will reconstruct your database configurations.

### Q29: What happens to locked apps if I uninstall Privacy Lock?
Uninstalling Privacy Lock deletes the application and its local database, instantly restoring normal access to all previously locked apps.

### Q30: How can I report a security vulnerability?
Review our **[[Security Policy]]** page and submit a detailed vulnerability report to the security team via secure email as outlined.

---

[[Home]] | [<< User Guide](User-Guide) | [[Troubleshooting >>](Troubleshooting)]
