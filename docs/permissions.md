# Privacy Lock Permissions Guide

This document describes the permission model of **Privacy Lock**, explaining why each permission is required, how they are declared in the manifest, and how they are requested dynamically at runtime.

---

## 🛠️ 1. Permission Architecture Overview

Privacy Lock operates as a system utility. It requires specific system-level permissions to monitor active applications, draw lock overlays, and authenticate users securely:

| Permission Name | API Level | Type | Purpose |
| :--- | :--- | :--- | :--- |
| **`android.permission.BIND_ACCESSIBILITY_SERVICE`** | API 24+ | Signature/System | Allows the background service to monitor active window changes. |
| **`android.permission.SYSTEM_ALERT_WINDOW`** | API 24+ | Special | Allows drawing the secure lock keypad overlay over other running applications. |
| **`android.permission.PACKAGE_USAGE_STATS`** | API 24+ | Special | Used as a reliable fallback to verify active app packages. |
| **`android.permission.USE_BIOMETRIC`** | API 28+ | Normal | Enables fingerprint and face unlock options. |
| **`android.permission.POST_NOTIFICATIONS`** | API 33+ | Dangerous | Used to display the persistent foreground service notification. |

---

## 🔐 2. Detailed Permission Descriptions

### 1. Bind Accessibility Service
This permission binds the background service to the system's accessibility framework. It allows the app to receive `TYPE_WINDOW_STATE_CHANGED` events whenever the active foreground app changes.
* **Manifest Declaration**:
  ```xml
  <service android:name=".security.AppAccessibilityService"
      android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
      android:exported="true">
      <intent-filter>
          <action android:name="android.accessibilityservice.AccessibilityService" />
      </intent-filter>
      <meta-data android:name="android.accessibilityservice"
          android:resource="@xml/accessibility_service_config" />
  </service>
  ```
* **Security Scope**: This is a system-level signature permission. The service can only be enabled manually by the user in system settings. It cannot be enabled programmatically by applications.

### 2. System Alert Window (Draw Over Other Apps)
Required to display the secure passcode keypad overlay over locked applications, blocking access until the correct PIN is entered.
* **Manifest Declaration**:
  ```xml
  <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
  ```
* **Verification Logic**: Verified at runtime using:
  ```kotlin
  Settings.canDrawOverlays(context)
  ```

### 3. Package Usage Stats (Usage Access)
Used as a reliable fallback to verify active app packages on custom Android interfaces (e.g., Samsung One UI, Xiaomi MIUI) where standard accessibility event delivery can be delayed.
* **Manifest Declaration**:
  ```xml
  <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
      tools:ignore="ProtectedPermissions" />
  ```
* **Verification Logic**: Verified at runtime using `AppOpsManager`.

### 4. Use Biometric / Fingerprint
Required to prompt the system's secure biometric authentication dialog when fingerprint or face unlock is enabled.
* **Manifest Declaration**:
  ```xml
  <uses-permission android:name="android.permission.USE_BIOMETRIC" />
  <uses-permission android:name="android.permission.USE_FINGERPRINT" android:maxSdkVersion="28" />
  ```

### 5. Post Notifications
Required on Android 13+ (API 33+) to display the persistent foreground service notification. This notification keeps the background accessibility service running when system memory is low.
* **Manifest Declaration**:
  ```xml
  <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
  ```

---

## 🚀 3. Dynamic Permission Request Flows

To comply with Android design guidelines, permissions must be requested using a transparent, multi-step process:

1. **Check Permission Status**: Verify if permissions are granted using helper methods in `SecurityUtils.kt`.
2. **Display Rationales**: Show a custom dialog explaining why the permission is needed before redirecting the user to system settings.
3. **Launch System Settings Intents**:
   * **Accessibility Settings**:
     ```kotlin
     val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
     ```
   * **Usage Access Settings**:
     ```kotlin
     val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
     ```
   * **Overlay Settings**:
     ```kotlin
     val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
     ```
4. **Re-evaluate Status**: Check permission status again when the user returns to the app to update the dashboard indicators dynamically.
