# Installation

This page covers the requirements, system configuration, and compilation instructions necessary to build Privacy Lock from source.

---

## Development Environment Requirements

Before compiling, ensure your local development workstation meets the following criteria:

| Component | Minimum Version | Recommended Version | Note |
| :--- | :--- | :--- | :--- |
| **Android Studio** | Flamingo (2022.2.1) | Ladybug (2024.2.1) or newer | Required for modern Kotlin DSL support and Compose preview engines. |
| **Java Development Kit (JDK)** | JDK 17 | JDK 17 (Zulu or Temurin) | Compiling with Java 21 is supported, but compiler targets remain pinned to Java 17 compatibility. |
| **Android SDK** | SDK 26 (Android 8.0) | SDK 34 (Android 14) | Min SDK is set to 26; Target SDK is set to 34. |
| **Gradle** | 8.0 | 8.4+ | Bundled with the wrapper configurations in `gradle/wrapper/gradle-wrapper.properties`. |

---

## Workspace Setup

### 1. Clone the Repository
Retrieve the source code and any submodules by cloning the official repository:

```bash
git clone https://github.com/example/privacy-lock.git
cd privacy-lock
```

### 2. Verify Android SDK Configurations
Ensure your system has the correct environment variable set for the Android SDK Location.

* **Linux/macOS**:
  ```bash
  export ANDROID_HOME=$HOME/Android/Sdk
  ```
* **Windows**:
  ```cmd
  set ANDROID_HOME=C:\Users\<YourUsername>\AppData\Local\Android\Sdk
  ```

---

## Build Configurations

The project relies on **Gradle Kotlin DSL (`.gradle.kts`)** and coordinates dependency management through a central Version Catalog (`gradle/libs.versions.toml`).

### Compilation Commands

Execute compilation commands from the project root directory. Do NOT use `gradlew` in restricted container environments; instead, call `gradle` directly:

#### Compile Debug APK
Used for local testing and emulator deployment. Includes full debug configurations and debug symbols:
```bash
gradle :app:assembleDebug
```
The output file is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

#### Compile Release APK
Builds a production-ready, optimized APK. Ensure signing keys are declared in environment variables or your build environment properties:
```bash
gradle :app:assembleRelease
```
The output file is generated at:
`app/build/outputs/apk/release/app-release-unsigned.apk` (or signed if configured).

#### Compile Android App Bundle (AAB)
Recommended format for publishing on the Google Play Store:
```bash
gradle :app:bundleRelease
```
The output file is generated at:
`app/build/outputs/bundle/release/app-release.aab`

#### Run Tests
Verify application functionality and core business logic locally using JUnit 5 and Robolectric:
```bash
gradle :app:testDebugUnitTest
```

---

## Troubleshooting Build and Installation Failures

### 1. Unresolved Reference or Missing Dependencies
* **Symptom**: Compilation fails with `Unresolved reference` or `Package not found` during `compileDebugKotlin`.
* **Solution**: Ensure your version catalog references match Kotlin dot-notation. For example, `androidx-core-ktx` in the `libs.versions.toml` file must be referenced as `libs.androidx.core.ktx` in the Kotlin Gradle configuration. Clean compile using:
  ```bash
  gradle clean assembleDebug
  ```

### 2. Java Compiler Compatibility Errors
* **Symptom**: `Unsupported class file major version` or compiler complains about targeted runtime specifications.
* **Solution**: Navigate to **Android Studio Settings > Build, Execution, Deployment > Build Tools > Gradle** and verify that the **Gradle JDK** matches JDK 17 exactly.

### 3. Missing Keystore Configuration for Release Builds
* **Symptom**: `SigningConfig "release" is missing required property "storeFile"`.
* **Solution**: For local release builds, configure a debug keystore fallback or specify release credentials in local properties. Do NOT modify the platform `debug.keystore` file, as doing so breaks package signatures and upgrades on active deployments.

---

[[Home]] | [[Getting Started >>](Getting-Started)]
