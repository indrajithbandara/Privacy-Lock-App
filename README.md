# Privacy Lock

[![Build Status](https://github.com/example/privacy-lock/workflows/Android%20CI/badge.svg)](https://github.com/example/privacy-lock/actions)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android_8.0+-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-M3-blue.svg)](https://developer.android.com/jetpack/compose)

Privacy Lock is a local-first application locker and privacy protection center for Android devices. Unlike standard application lockers that rely on network integration or display excessive advertisements, Privacy Lock is built as an offline-first system utility. It utilizes standard Android background services to intercept application launches and overlay a secure authentication screen, ensuring personal data remains isolated on-device.

---

## 📸 Screenshots

| Onboarding & Permissions | Secure PIN Overlay | Security Timeline |
| :---: | :---: | :---: |
| ![Onboarding Screenshot Placeholder](assets/dashboard_screenshot.png) | ![PIN Overlay Screenshot Placeholder](assets/keypad_screenshot.png) | ![Timeline Screenshot Placeholder](assets/settings_screenshot.png) |

*(Reference images are located in the `/assets` directory. To refresh these files, capture live snapshots on a connected device or run local Roborazzi screenshot verification tasks.)*

---

## 🔍 Why This Project Exists

Mobile utility applications are frequently bundled with tracker SDKs, remote telemetry services, and unneeded network permissions that compromise user metadata. This project was initiated to create a fully transparent, local-first lock utility with three key design constraints:

1. **Zero Network Dependencies**: The application does not request the `INTERNET` permission in its manifest, completely preventing remote data exfiltration.
2. **Explicit Cloud Exclusion**: Local SQLite databases are flagged to bypass standard Android auto-backup rules, preventing credentials and event history from being uploaded to vendor cloud storage.
3. **Frictionless Native Integration**: The UI is styled strictly around standard Material 3 design systems to look and behave like a native system component of the Android operating system.

---

## 🛠 Features

Privacy Lock includes the following core functionalities, all implemented directly within the source tree:

* **Background App Interception**: Uses a background Accessibility Service to detect when specified applications are brought to the foreground and displays a secure locking interface.
* **Master & Decoy PIN Toggles**: Supports defining a standard security PIN alongside an alternative Decoy PIN. Both trigger system unlock, preventing physical coercion scenarios.
* **On-Device Database Persistence**: Utilizes Room SQLite storage to record locked application configurations, settings states, secure timeline logs, and failed intrusion files.
* **Intruder Logging with Generative Avatars**: Records failed PIN entries with masked characters (`****`) alongside randomly assigned visual identifier avatars to flag shoulder-surfing and entry attempts.
* **Layout Randomization**: Features a dynamic keypad layout option that scrambles the numeric entry grid on every display event, mitigating finger-smudge analysis.
* **Dynamic Window Protection**: Integrates standard window flags (`FLAG_SECURE`) to programmatically disable screenshots, system-recents caching, and screen recording on sensitive protection screens.
* **Physical Device Scanning**: Leverages Android's `PackageManager` to scan and list physical applications installed on-device alongside default virtual demo listings.
* **Material 3 UI**: Built with a custom M3 Sage Green color palette with responsive layouts adapting fluidly to compact (mobile) and expanded (tablet) screen sizes.

---

## 📐 Architecture Overview

The codebase is structured under clean Model-View-ViewModel (MVVM) patterns to ensure high separation of concerns:

```
                  ┌───────────────────────────────┐
                  │       Jetpack Compose UI      │
                  │  (Dashboard, PrivacyCenter,   │
                  │         LockActivity)         │
                  └───────────────┬───────────────┘
                                  │ observes StateFlow (collectAsStateWithLifecycle)
                                  ▼
                  ┌───────────────────────────────┐
                  │       PrivacyViewModel        │
                  │   (Coordinates presentation   │
                  │       and screen states)      │
                  └───────────────┬───────────────┘
                                  │ invokes Repository operations
                                  ▼
                  ┌───────────────────────────────┐
                  │       PrivacyRepository       │
                  │   (Coordinates database and   │
                  │     system package scans)     │
                  └───────────────┬───────────────┘
                                  │
                 ┌────────────────┴────────────────┐
                 ▼                                 ▼
      ┌─────────────────────┐           ┌─────────────────────┐
      │   Room Database     │           │    SecurityUtils    │
      │  (SQLite DAOs &     │           │ (Salted SHA-256 and │
      │   local Entities)   │           │  system permissions)│
      └─────────────────────┘           └─────────────────────┘
```

### Flow of Execution
1. **Interception**: `AppAccessibilityService` receives a `TYPE_WINDOW_STATE_CHANGED` event indicating a package has been opened.
2. **Evaluation**: The service queries `AppLockManager` to check if the opened package is locked and currently lacks an active session unlock key.
3. **Intervention**: If locked, the service starts `LockActivity` inside a new task window stack, placing the secure numeric keypad over the targeted app.
4. **Resolution**: Correct entry of the master or decoy PIN updates the session-level unlock list in `AppLockManager` and closes the overlay activity. Locking state automatically resets when the screen is turned off.

---

## 📂 Project Structure

Key modules, source directories, and core classes included in the repository:

```
├── app/
│   ├── src/main/
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt        # Entry point configuring navigation and starting components
│   │   │   ├── LockActivity.kt        # Overlay activity handling security keypad inputs and pin checking
│   │   │   ├── data/
│   │   │   │   ├── AppDatabase.kt     # Room database builder and singleton generator
│   │   │   │   ├── Daos.kt            # SQL data access queries (LockedApp, SecurityConfig, IntruderSelfie, TimelineEvent)
│   │   │   │   ├── Entities.kt        # Local schema definitions representing database tables
│   │   │   │   └── PrivacyRepository.kt # Repository executing device app syncs and DB transactions
│   │   │   ├── security/
│   │   │   │   ├── AppAccessibilityService.kt # Core system interceptor capturing window event changes
│   │   │   │   ├── AppLockManager.kt   # Local state controller managing locked app packages and live tokens
│   │   │   │   └── SecurityUtils.kt   # Hashing processes (SHA-256) and explicit system settings intent launchers
│   │   │   └── ui/
│   │   │       ├── PrivacyViewModel.kt # ViewModel managing screen states and UI event streams
│   │   │       ├── components/
│   │   │       │   └── CommonComponents.kt # Standard elements (SecureKeypad, AppIcon renderers)
│   │   │       └── screens/
│   │   │           ├── DashboardScreen.kt # Displays current locked apps and interactive phone simulation
│   │   │           └── PrivacyCenterScreen.kt # Details system status indicators, permissions checks, and logs
│   │   └── res/
│   │       ├── xml/
│   │       │   ├── accessibility_service_config.xml # Configures target window event filters for background lock triggers
│   │       │   ├── backup_rules.xml   # Explicitly excludes databases from cloud sync files
│   │       │   └── data_extraction_rules.xml # Prevents database exfiltration via local physical backup cables
│   │       └── values/
│   │           └── strings.xml        # Contains application strings and system services definitions
└── gradle/
    └── libs.versions.toml             # Standardized centralized version catalog
```

---

## 📡 Android Permissions

To ensure transparent operations, the manifest declares only the specific permissions needed to intercept processes and draw overlays:

| Permission Name | Type | Purpose |
| :--- | :--- | :--- |
| `android.permission.SYSTEM_ALERT_WINDOW` | System Overlay | Allows drawing the PIN overlay screen directly over other target applications. |
| `android.permission.BIND_ACCESSIBILITY_SERVICE` | System Service | Binds the background scanner to monitor running packages on-device. |
| `android.permission.PACKAGE_USAGE_STATS` | Usage Access | Enhances active application determination accuracy on varied Android operating systems. |
| `android.permission.QUERY_ALL_PACKAGES` | Package Query | Enumerates packages installed on the device to populate the configuration dashboard. |
| `android.permission.FOREGROUND_SERVICE` | Background Run | Keeps the lock validation and accessibility components active in the background. |
| `android.permission.RECEIVE_BOOT_COMPLETED` | System Event | Re-initiates local locking services automatically when the device starts up. |

---

## 🚀 Installation & Build Guide

### Prerequisites
* **JDK 17** or higher
* **Android SDK Level 34** (Upside Down Cake) or higher
* **Gradle 8.0** or higher

### Build Instructions

Execute standard Gradle commands from the root directory to manage, compile, and build the application:

```bash
# Clean and compile the debug package
./gradlew clean assembleDebug

# Compile the release package (Generates unsigned APK)
./gradlew assembleRelease

# Compile the release bundle (Generates AAB for deployment)
./gradlew bundleRelease

# Verify linter rules across source directories
./gradlew lintDebug

# Run unit and local Robolectric tests
./gradlew :app:testDebugUnitTest
```

Generated outputs will be saved to:
* **Debug APK**: `/app/build/outputs/apk/debug/app-debug.apk`
* **Release APK**: `/app/build/outputs/apk/release/app-release-unsigned.apk`
* **Release Bundle**: `/app/build/outputs/bundle/release/app-release.aab`

---

## 🔧 Troubleshooting

#### 1. Applications do not lock after toggling the shield in the dashboard
* **Reason**: The core background accessibility service is likely disabled or was paused by the OS battery management system.
* **Fix**: Navigate to `Settings -> Accessibility -> Installed Services`. Verify that `Privacy Lock` is explicitly turned ON.

#### 2. The physical apps list in the dashboard is empty
* **Reason**: The application requires Usage Access and Package Query permissions to map physical system applications.
* **Fix**: Ensure `System Permissions Required` banner prompts are accepted. You can check permission status in `Settings -> Apps -> Special App Access -> Usage Access`.

#### 3. Standard Android screenshots can still capture the overlay screen
* **Reason**: Screenshot Protection is disabled in the configuration.
* **Fix**: Navigate to the Settings pane within the application and switch "Screenshot Protection" to active. This immediately applies `FLAG_SECURE` to the system window properties.

---

## 💬 FAQ

**Q: Are my PIN entries sent to any external server?**  
A: No. Privacy Lock is built without any internet permissions declared in the `AndroidManifest.xml`. There is no technical capability for the app to communicate with any external web service or remote server.

**Q: Can I retrieve my PIN if I forget it?**  
A: No. PIN values are stored strictly as salted SHA-256 hashes inside local SQLite files. There is no password recovery flow. If you forget your master security PIN, you must clear the application data via system settings to reset the system.

**Q: How does Decoy PIN mode work?**  
A: You can configure a separate numeric PIN under the settings screen. When entered, it unlocks the device normally, but logs the event as a standard access rather than revealing the master credential, helping avoid suspicion.

---

## 🗺 Roadmap

* [ ] **Biometric API Integration**: Support fallback unlocking via hardware fingerprint scanners.
* [ ] **Intruder Notification Indicators**: Flash alerts on-screen when entering the dashboard if an intrusion event is recorded in the logs.
* [ ] **Advanced App Scheduling**: Enable configuring custom time-of-day blocks during which application locking is automatically armed or disarmed.

---

## 🤝 Contribution Guidelines

We welcome contributions to help improve Privacy Lock. Please adhere to the following steps:

1. **Code Formatting**: Ensure all code matches Kotlin style guides and formatting rules. Run `./gradlew lintDebug` before making a pull request.
2. **Testing Constraints**: Ensure all changes are verified by writing or running Unit/Robolectric test tasks. Running `./gradlew :app:testDebugUnitTest` must return clean passes.
3. **Branch Hygiene**: Submit all modifications to the `main` branch through small, self-contained, and atomic pull requests.

---

## 🛡 Security Policy

We take security issues within this utility seriously. Since the application is entirely offline, vulnerabilities are restricted to local side-channel analysis and local database bypasses.

* **Reporting Vulnerabilities**: Please contact the maintainer directly through the security contact email specified in [SECURITY.md](SECURITY.md). Do not open public GitHub issues for discovered bypasses.
* **Scope**: We actively maintain security updates for the current major release version target.

---

## 📄 License

This project is licensed under the Apache License 2.0. See the [LICENSE](LICENSE) file for complete details.

---

## 🤝 Acknowledgements

* **Jetpack Compose Teams**: For providing declarative UI paradigms.
* **Material Design Team**: For standard M3 specifications.
* **Android Open Source Project (AOSP)**: For foundational system services.
