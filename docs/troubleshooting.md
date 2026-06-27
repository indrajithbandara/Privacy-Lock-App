# Privacy Lock Troubleshooting Guide

This guide provides technical solutions and troubleshooting steps for common issues encountered by users and developers of **Privacy Lock**.

---

## 🔌 1. Accessibility Service Stops Running (OEM Battery Closures)

On some Android devices, customized OEM interfaces (e.g., Samsung One UI, Xiaomi MIUI, Oppo ColorOS) may close background services aggressively to save battery.

### Symptoms:
* Locked applications open without displaying the PIN keypad overlay.
* The Privacy Center status indicator displays the Accessibility Service as **Inactive**.

### Solutions:
1. **Disable Battery Optimizations**:
   * Navigate to **System Settings -> Apps -> Privacy Lock -> Battery**.
   * Select **Unrestricted** (or **Do Not Optimize**) to prevent the system from closing the background service.
2. **Exclude from App Standby**:
   * Ensure Privacy Lock is exempt from system-level memory cleaners.
3. **Reference Guidelines**:
   * For device-specific instructions, visit [Don't Kill My App!](https://dontkillmyapp.com/) to configure background permissions correctly for your specific device model.

---

## 🛡️ 2. Lock Overlay Keypad Fails to Display (Overlay Permission Reset)

If the lock screen keypad fails to display, the overlay permission may have been revoked by the system.

### Symptoms:
* The background service is active, but locked applications do not display the passcode overlay.
* A system notification indicates that overlay drawing is disabled.

### Solutions:
1. **Verify Permission Status**:
   * Open the Privacy Center inside the app to check if **Display Over Other Apps** is enabled.
2. **Re-grant Permission**:
   * If disabled, navigate to **Settings -> Display Over Other Apps**, select **Privacy Lock**, and toggle **Allow permission** to **On**.
3. **Android 13+ Restricted Settings**:
   * On newer Android versions, sideloaded applications may have accessibility settings disabled by default. If the setting is greyed out:
     * Navigate to **Settings -> Apps -> Privacy Lock**.
     * Tap the three dots icon in the top right corner and select **Allow restricted settings**.
     * You will now be able to enable the Accessibility Service in settings.

---

## 🔐 3. Biometric Unlock Unavailable

Fingerprint or face unlock options may be unavailable if the system checks do not pass.

### Symptoms:
* The **Verify with Biometrics** option is greyed out or does not display on the lock screen overlay.
* Settings toggle is disabled.

### Solutions:
1. **Enroll Biometric Data**:
   * Verify that you have registered at least one fingerprint or face profile in your device's system settings.
2. **Verify Hardware Integrity**:
   * Ensure your device has compatible biometric hardware and that the sensor is clean and free of obstructions.
3. **Security Invalidation**:
   * If you recently enrolled a new fingerprint or face profile in system settings, Privacy Lock may automatically disable biometric unlock as a security measure. To re-enable it, open the app, enter your Master PIN, and toggle the biometrics setting on again.

---

## 💾 4. Database Schema Version Crashes

This issue can occur on developer builds when modifying database structures.

### Symptoms:
* The application crashes immediately on launch.
* Logs display `Room cannot verify the database table structures` or migration mismatch errors.

### Solutions:
1. **Destructive Migration Fallback**:
   * The database is configured with `fallbackToDestructiveMigration` enabled on developer builds. This resets and updates the tables automatically if a schema mismatch is detected.
2. **Clear Application Data**:
   * If database structures become corrupted, navigate to **Settings -> Apps -> Privacy Lock -> Storage** and tap **Clear Data**. This will reset the app to its default settings and allow you to set up your PIN again.

---

## 📝 5. Backup Import Failures

Issues with importing saved configuration strings.

### Symptoms:
* An error message saying *"Invalid configuration string format"* or *"Checksum mismatch"* displays when attempting to import a backup.

### Solutions:
1. **Verify String Integrity**:
   * Ensure that the backup string is not empty and that you copied the entire string, including all semicolons.
2. **Verify Schema Version Compatibility**:
   * If the backup string was generated on a newer version of Privacy Lock, it may not be compatible with older versions of the app. Update the app to the latest version and try importing the string again.
