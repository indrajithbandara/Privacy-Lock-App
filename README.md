# Privacy Lock — Premium Enterprise Privacy Center

[![License](https://img.shields.io/badge/License-Apache_2.0-sage.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-M3-blue.svg)](https://developer.android.com/jetpack/compose)

Privacy Lock is a production-grade, premium offline-first privacy center and app locking utility for Android. Unlike standard app-lockers that feel cluttered or trigger unnecessary cloud synchronizations, Privacy Lock is meticulously designed with a Material 3 Sage Green aesthetic to look, feel, and behave like a native Google Pixel first-party system tool. 

Designed under strict DevSecOps and security engineering methodologies, Privacy Lock empowers users to securely govern their private data, shield sensitive screens, detect intruder unauthorized entries, and toggle camouflage states on-device.

---

## 📸 Screenshots

| Privacy Dashboard | Randomized Secure Keypad | Settings & Camouflage |
|:---:|:---:|:---:|
| <img src="assets/dashboard_screenshot.png" width="260" alt="Privacy Dashboard"/> | <img src="assets/keypad_screenshot.png" width="260" alt="Randomized Keypad"/> | <img src="assets/settings_screenshot.png" width="260" alt="Settings Screen"/> |

*(To populate these, run screenshot capturing or Roborazzi visual regression tests. Place captured output pngs in the `/assets` folder.)*

---

## 🛠 Features

- **App Lock Engine**: Real-time locking configuration of local system applications (Settings, Play Store, Package Installer) and installed third-party utilities.
- **Intruder Selfie & Secure Logs**: Captures and timestamps failed entry PINs. To prevent shoulder surfing and local compromise, PIN logging is masked (`****`) on-disk and secure avatars are generated.
- **Camouflage / Decoy PIN**: Supports setting a separate Decoy PIN. Entering the Decoy PIN dynamically redirects the user to a generic un-locked "Decoy Mode" state to prevent physical coercion or screen sharing compromises.
- **Randomized Keypad Layout**: Security recommendation that dynamically shuffles the numeric pin grid on every entry screen to defeat grease-smudge detection and screen recording vectors.
- **Screenshot Protection Shield**: System-level dynamic window protection utilizing `FLAG_SECURE` to block screenshots, system-recents snapshots, or hardware screen mirroring inside protected views.
- **Privacy Timeline**: Fully localized, encrypted logging of security events (locks toggled, settings changed, failed intrusion attempts) keeping users informed of app health.
- **M3 Sage Green Theme**: Visually comforting, high-contrast Material 3 palette optimized for daylight use and featuring a handcrafted Dark Mode to protect eyes in low-light environments.
- **Full Offline-First Isolation**: No tracking SDKs, no network analytics, no third-party telemetry, and no external clouds. Your keys and access states never leave your device.

---

## 📐 Architecture

Privacy Lock leverages Google's recommended Modern Android Development (MAD) practices. It is structured around the **Model-View-ViewModel (MVVM)** clean architecture pattern combined with a decoupled Repository-backed persistence layer.

```
                  ┌───────────────────────────────┐
                  │      Jetpack Compose UI       │ (Views / Screens)
                  │ (Dashboard, PrivacyCenter,...)│
                  └───────────────┬───────────────┘
                                  │ observes StateFlow (collectAsStateWithLifecycle)
                                  ▼
                  ┌───────────────────────────────┐
                  │       PrivacyViewModel        │ (UI State holder)
                  │   (Handles screen navigation) │
                  └───────────────┬───────────────┘
                                  │ invokes suspend functions / flows
                                  ▼
                  ┌───────────────────────────────┐
                  │       PrivacyRepository       │ (Data source coordinator)
                  └───────────────┬───────────────┘
                                  │
                  ┌───────────────┴───────────────┐
                  ▼                               ▼
       ┌─────────────────────┐         ┌─────────────────────┐
       │     Room SQLite     │         │    SecurityUtils    │ (Secure PIN hashing,
       │  (AppDatabase/DAOs) │         │   & SecureRandom    │  Stealth dial generation)
       └─────────────────────┘         └─────────────────────┘
```

- **UI Layer (Compose)**: Declarative, composable screens relying on unidirectional data flow (UDF). Screens utilize Material 3 styling tokens and implement dynamic layouts that adapt fluidly across Compact (Mobile), Medium (Foldable), and Expanded (Tablet) screen widths.
- **State Layer (ViewModel)**: Coordinates state streams using Kotlin Coroutines and asynchronous state sharing (`StateFlow`). Observes life-cycle transitions safely to avoid memory leaks.
- **Data Layer (Repository)**: Decouples Room entities and security operations from presentation details. Exposes raw stream events directly via Flow.
- **Security Utility Layer**: Encapsulates hashing mechanisms (SHA-256) combined with dynamic cryptographic salt generation using `SecureRandom` to prevent collision and pre-computation lookup table attacks.

---

## 🔒 Security Specifications

### 1. Cryptographically Secure Random Number Generation
All randomized security operations—such as Stealth Dial code assembly or random avatar indexes—exclusively leverage `java.security.SecureRandom`. It enforces high-entropy randomness to defeat state prediction vectors.

### 2. High-Assurance Local PIN Storage
User PIN values are never stored on the filesystem in plaintext. Upon configuration, the PIN is combined with a dynamic cryptographic salt block, hashed via SHA-256, and verified locally on comparison.

### 3. XML Backup & Data Extraction Exclusion
To safeguard SQLite database files from being extracted via ADB cloud backup exploits or device-to-device physical transfer lines, explicit exclusions are established under `/app/src/main/res/xml`:

```xml
<!-- backup_rules.xml / data_extraction_rules.xml -->
<full-backup-content>
    <exclude domain="database" path="privacy_lock_db"/>
    <exclude domain="database" path="privacy_lock_db-shm"/>
    <exclude domain="database" path="privacy_lock_db-wal"/>
</full-backup-content>
```

### 4. Dynamic Screen & Recents Window Shielding
When "Screenshot Protection" is toggled in the dashboard or settings, the active Android Window immediately updates its parameters to include `FLAG_SECURE`. This system-level instruction enforces hardware-level display blanking during recording and prevents OS multitasking cache logging.

---

## 🛡 Privacy Compliance

- **No Cloud Synchronization**: Access credentials and configurations remain strictly local inside SQLite files.
- **No Analytics / Telemetry**: No third-party network connections or SDK initializations.
- **Zero Advertising Trackers**: Free of visual and behavioral tracker telemetry.
- **Secure Default Exclusions**: Local databases are explicitly omitted from standard Android OS backup bundles.

---

## 📡 Permissions Used

The application requests only the minimal system access tokens required to safely govern overlay locks on device:

```xml
<!-- AndroidManifest.xml -->
<!-- Required to show the secure PIN entry screen over other application windows -->
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW"/>

<!-- Required to detect which application has been launched to trigger the overlay lock -->
<uses-permission android:name="android.permission.PACKAGE_USAGE_STATS" tools:ignore="ProtectedPermissions"/>

<!-- Required to start the secure background detection service upon system reboot -->
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED"/>

<!-- Required to run the persistent lock monitoring loop under modern Android OS models -->
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
```

---

## 🛠 Technology Stack

- **Kotlin (1.9.22)**: Modern language for building safe, maintainable Android features.
- **Jetpack Compose (M3)**: Declarative UI framework leveraging Material Design 3 guidelines.
- **Room Persistence Library**: Offline SQL data store utilizing KSP (Kotlin Symbol Processing).
- **Coroutines & Flows**: Safe multi-threaded background task processing.
- **Lifecycle Runtime**: Lifecycle-aware composable state streams (`collectAsStateWithLifecycle`).

---

## 🚀 Installation & Build Guide

### Prerequisites
- **Android Studio Jellyfish (2023.3.1) or higher**
- **Android SDK Level 34 (Upside Down Cake)**
- **Java SE Development Kit (JDK) 17**

### Setup Instructions
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-repo/privacy-lock.git
   cd privacy-lock
   ```

2. **Load Project in Android Studio**:
   - Open Android Studio.
   - Select **Open an Existing Project** and direct the explorer to the cloned `privacy-lock` root folder.
   - Allow Gradle to sync and fetch remote dependencies.

3. **Verify the Build**:
   To compile and package the application into a debug APK directly from your terminal:
   ```bash
   ./gradlew assembleDebug
   ```
   The generated APK will reside at: `/app/build/outputs/apk/debug/app-debug.apk`.

4. **Run Unit & Robolectric Tests**:
   To execute the comprehensive test suite locally:
   ```bash
   ./gradlew :app:testDebugUnitTest
   ```

---

## 🤝 Contribution

Contributions are what make the open-source community an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**. Please read [CONTRIBUTING.md](CONTRIBUTING.md) for branch naming conventions, styling standards, and testing policies.

---

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for complete details.

---

## 🏷 Version Information

- **Current Version**: `1.0.0 (Initial Enterprise Release)`
- **Target SDK**: `34` (Android 14)
- **Min SDK**: `26` (Android 8.0 Oreo)
- **Database Engine**: Room `2.6.1`
