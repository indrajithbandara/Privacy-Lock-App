# Troubleshooting Guide

This diagnostic directory contains solutions for permission blocks, system-service terminations, biometric validation issues, and build/installation failures.

---

## 1. Permission and System Service Failures

### 1.1 Accessibility Service Randomly Disables
* **Symptom**: The accessibility service turns off spontaneously, and locked applications open without triggering the keypad overlay.
* **Root Cause**: The Android operating system's low-memory killer (LMK) or aggressive OEM power-saving policies are terminating the background process.
* **Diagnostic Steps & Resolution**:
  1. Go to **Settings > Apps > Privacy Lock > Battery**.
  2. Change battery optimization from *Optimized* or *Restricted* to **Unrestricted** (or *Don't Optimize*).
  3. Ensure **Remove permissions if app is unused** is toggled **OFF** in system settings.
  4. On OEM devices (such as Samsung, Xiaomi, or OnePlus), open your recent apps tray, tap the Privacy Lock icon, and select **Lock this app** (to prevent the task killer from clearing it from RAM).

### 1.2 "Restricted Setting" Dialog on Android 13+
* **Symptom**: Toggling the Privacy Lock service in accessibility settings triggers a restricted warning, preventing activation.
* **Root Cause**: Android 13+ restricts sideloaded apps from enabling accessibility services by default.
* **Resolution**:
  1. Go to **Settings > Apps > Privacy Lock**.
  2. Tap the **Three Vertical Dots** in the top-right corner.
  3. Select **Allow restricted settings**.
  4. Authorize using your device fingerprint or screen lock.
  5. Return to Accessibility Settings and toggle the service.

---

## 2. Biometric Integration Failures

### 2.1 Biometric Prompt Failing to Display
* **Symptom**: Setting "Biometric Unlock" to ON compiles successfully, but the fingerprint dialog never displays when launching locked apps.
* **Root Cause**: Either no biometrics are registered on the device, or the system hardware security level is set to weak.
* **Resolution**:
  1. Open your Android System **Settings > Security > Fingerprint/Face Unlock** and ensure at least one strong biometric template is registered.
  2. Ensure your device hardware supports strong cryptographic biometrics (`BIOMETRIC_STRONG`). Weak camera-only face unlocks may fail to trigger `BiometricPrompt` if they do not meet the security criteria.

---

## 3. Local Compilation & Deployment Issues

### 3.1 "Cannot find symbol" or Unresolved Dependencies
* **Symptom**: running `gradle assembleDebug` fails with package resolution errors.
* **Root Cause**: Outdated local Gradle caches or incorrect version reference formatting.
* **Resolution**:
  1. Verify the dependency naming syntax inside `/gradle/libs.versions.toml`. Kebab-case entries (e.g., `androidx-core-ktx`) must be converted to dot-notation (e.g., `libs.androidx.core.ktx`) inside your `build.gradle.kts` build scripts.
  2. Clean build resources and re-evaluate project files:
     ```bash
     gradle clean assembleDebug
     ```

### 3.2 Java Class File Version Conflict
* **Symptom**: Gradle throws `Unsupported class file major version`.
* **Root Cause**: The local shell is running a different JDK version than the version required by the Android Gradle Plugin (AGP).
* **Resolution**:
  Ensure your terminal environment variables target **JDK 17** compatibility:
  ```bash
  export JAVA_HOME=/path/to/jdk-17
  gradle assembleDebug
  ```

---

## 4. Performance & Overlay Stutter

### 4.1 Keypad Animation Frame Drops (Stuttering)
* **Symptom**: The lock overlay stutters or drops frames when launching.
* **Root Cause**: Heavy background rendering or intensive database lookups on the main thread.
* **Resolution**:
  * Ensure `AppLockManager` is utilizing its thread-safe in-memory cache (`lockedPackages` set) rather than making synchronous SQLite lookups on every accessibility event.
  * Disable randomized keypad layouts inside **Settings** to lower Compose's re-composition complexity if the device has a low-performance GPU.

---

[[Home]] | [<< FAQ](FAQ) | [[Contributing >>](Contributing)]
