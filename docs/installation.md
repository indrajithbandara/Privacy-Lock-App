# Privacy Lock Installation Guide

This guide provides step-by-step instructions for downloading, verifying, and installing **Privacy Lock** on Android devices.

---

## 📥 1. Downloading Official Release Packages

To protect your device from tampered or malicious builds, only download Privacy Lock from our official distribution channels:

* **GitHub Releases**: [Privacy Lock GitHub Releases](https://github.com/example/privacy-lock/releases)
* **F-Droid Client (Upcoming)**: Search for **Privacy Lock** in the F-Droid app catalog.
* **Google Play Store (Upcoming)**: Direct secure installer.

Every official release includes:
* `privacy-lock-release.apk`: The optimized production application package.
* `privacy-lock-release.apk.sha256`: A cryptographic checksum file used to verify the integrity of the downloaded APK.

---

## 🛡️ 2. Verifying File Integrity (SHA-256 Checksum)

Before installing a sideloaded APK, verify its integrity using its SHA-256 checksum. This ensures the file has not been modified or tampered with.

### How to Verify the Checksum:
1. Download `privacy-lock-release.apk` and copy the published SHA-256 hash from the release notes.
2. Open your terminal or command prompt and run the appropriate command for your operating system:
   * **Linux**:
     ```bash
     sha256sum privacy-lock-release.apk
     ```
   * **macOS**:
     ```bash
     shasum -a 256 privacy-lock-release.apk
     ```
   * **Windows (PowerShell)**:
     ```powershell
     Get-FileHash privacy-lock-release.apk -Algorithm SHA256
     ```
3. Compare the printed hash with the official hash on the GitHub Releases page. If they match exactly, the file is authentic and safe to install.

---

## 🚀 3. Sideloading & Installing the APK

Android restricts installing applications from outside official app stores by default. To install the downloaded APK, you must grant the "Install Unknown Apps" permission to your browser or file manager.

### Step-by-Step Walkthrough:

#### On Android 10, 11, 12, 13, 14, & 15:
1. Tap the downloaded `privacy-lock-release.apk` file in your browser or file manager.
2. If a security prompt appears saying *"For your security, your phone is not allowed to install unknown apps from this source"*, tap **Settings**.
3. Toggle the **Allow from this source** switch to **On**.
4. Return to the installation dialog and tap **Install**.
5. Once complete, tap **Open** to launch Privacy Lock.

#### On Older Android Versions (Android 8.0 & 9.0):
1. Navigate to **System Settings -> Apps & Notifications -> Advanced -> Special App Access -> Install Unknown Apps**.
2. Select your browser or file manager and toggle **Allow from this source** to **On**.
3. Open your file manager, find the downloaded APK, and tap **Install**.

---

## 🔄 4. Upgrading Existing Installations

Upgrading Privacy Lock to a newer version does not require uninstalling the previous version. Sideloading a newer version will update the app while preserving your settings, PIN configurations, and intruder logs.

### Safe Upgrade Procedures:
1. Verify that the new APK's SHA-256 checksum matches the official release hash.
2. Run the installer. Your device's Package Installer will ask: *"Do you want to install an update to this existing app? Your existing data will not be lost."*
3. Tap **Update** to complete the upgrade.
4. **Do not uninstall the old app first**, as doing so will permanently delete your database configurations and reset your master PIN.

---

## 🔧 5. Troubleshooting Installation Failures

If you encounter errors during installation, check the following common solutions:

* **"App Not Installed" / Package Conflict**: This error occurs if there is a signature conflict between the installed app and the new APK (e.g., trying to install a debug build over a release build). To resolve this, back up your settings, uninstall the existing version, and install the new APK.
* **"Parse Error" (Incorrect SDK version)**: Occurs if your device is running an unsupported Android version. Privacy Lock requires **Android 8.0 (API 26)** or higher.
* **Insufficient Storage**: Ensure your device has at least 50MB of free space to compile and cache the application database.
