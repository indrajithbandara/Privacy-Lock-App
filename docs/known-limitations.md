# Privacy Lock Known Limitations

This document outlines the technical limitations, system boundaries, and compatibility constraints of **Privacy Lock** under specific Android system environments.

---

## 🏗️ 1. Core System Limitations

Because Privacy Lock operates as a local system utility without root privileges, its behavior is bound by standard Android security policies and APIs:

| Boundary Class | Constraint | Technical Impact |
| :--- | :--- | :--- |
| **System Bootup** | Direct Boot restrictions | The Accessibility Service cannot start until the user unlocks their device for the first time after a bootup. Applications launched before this initial unlock cannot be locked. |
| **OEM Background Management** | Aggressive battery managers | Some Android systems (e.g., Samsung, Xiaomi, Oppo) may close background services aggressively to save battery. |
| **Sideload Restricted Settings** | Android 13+ security rules | Sideloaded applications are restricted from enabling Accessibility Services by default on Android 13+. Users must manually allow "Restricted Settings" in system settings. |
| **Hardware Compatibility** | Older Biometric Sensors | Older devices may not support strong biometric authentication APIs, preventing fingerprint or face unlock from being used. |

---

## 🔌 2. Detailed Technical Explanations

### 1. Direct Boot Restrictions
Android uses file-based encryption (FBE). Until the user enters their PIN on device bootup, the credential-encrypted database cannot be decrypted:
* **Impact**: Privacy Lock cannot verify lock configurations or access its database before the initial device unlock, meaning applications are temporarily unprotected immediately after a reboot.
* **Mitigation**: We recommend setting a secure system lock screen PIN alongside Privacy Lock.

### 2. OEM Background Management
Some manufacturer skins use aggressive memory cleaners that close background accessibility services to save battery and RAM.
* **Impact**: If closed, the app will fail to intercept application launches, leaving protected apps temporarily accessible.
* **Mitigation**: Users must manually disable battery optimization for Privacy Lock and set battery usage to **Unrestricted** in system settings.

### 3. Android 13+ Sideload Restricted Settings
To protect devices from malware, Android 13+ disables accessibility settings by default for applications installed from outside official app stores:
* **Impact**: Users cannot toggle the Accessibility Service switch on when they first install the APK.
* **Mitigation**: Users must navigate to **Settings -> Apps -> Privacy Lock**, tap the three dots icon in the top right corner, and select **Allow restricted settings** before they can enable the service.

### 4. Older Biometric Sensors
Devices running older Android versions may not support the modern `androidx.biometric` API library, or may lack compatible biometric sensors:
* **Impact**: Fingerprint and face unlock options will be unavailable on these devices.
* **Mitigation**: The app automatically disables biometric options on incompatible hardware and defaults to PIN passcode verification.
