# Build Instructions

This document provides complete, step-by-step instructions for local compilation, testing, and deployment of Privacy Lock from source code.

---

## 💻 1. Host Workstation Prerequisites

Before attempting compilation, confirm your host machine is configured with these system-level assets:

* **Java Development Kit (JDK)**: **JDK 17** is mandatory (Zulu JDK 17 or Eclipse Temurin JDK 17 are recommended). Compile configurations targeting Android compileSDK 37 require JDK 17+ to run the Android Gradle Plugin (AGP).
* **Android SDK**: Install SDK Platform Tools matching **SDK 34 (Android 14)** or newer. Target SDK values can be managed up to SDK 37 based on build-time properties.
* **Gradle Build Tool**: The compilation targets use Gradle wrapper Gradle 8.4+ (fully configured within the project wrapper properties).

---

## 🛠️ 2. Step-by-Step Local Build Commands

Run all command-line commands from the project root directory. On restricted sandboxed servers, execute using the direct `gradle` task runner (instead of `./gradlew`):

### 2.1 Complete Clean Build
To remove all cached compilation files, temporary build intermediates, and reset the dependency compiler cache:
```bash
gradle clean
```

### 2.2 Compile Debug Build (Recommended for testing)
Builds an APK packaged with debug symbols, suitable for local installation and emulator deployment:
```bash
gradle :app:assembleDebug
```
* **Output Artifact Location**: 
  `app/build/outputs/apk/debug/app-debug.apk`

### 2.3 Compile Release Build
Generates a fully-optimized, compressed APK. Note that to run in production, this APK must be signed with a secure Android keystore:
```bash
gradle :app:assembleRelease
```
* **Output Artifact Location**: 
  `app/build/outputs/apk/release/app-release-unsigned.apk`

### 2.4 Compile Release Android App Bundle (AAB)
Compiles the app bundle format required for publishing upgrades on the Google Play Store:
```bash
gradle :app:bundleRelease
```
* **Output Artifact Location**: 
  `app/build/outputs/bundle/release/app-release.aab`

---

## 🔐 3. Secrets & Configuration Management

Privacy Lock prioritizes modern, secure secrets management to prevent leaking private credentials or keys in open-source environments.

### 3.1 Secrets Gradle Plugin
The build system incorporates the **Secrets Gradle Plugin for Android** to inject environment variables securely:
```kotlin
// Configured inside app/build.gradle.kts
secrets {
    defaultPropertiesFileName = ".env.example"
}
```

### 3.2 Setting Up the Environment File
Instead of hardcoding keys or modifying the restricted `local.properties` file, create a local `.env` configuration file in the project's root folder:
1. Copy the sample file:
   ```bash
   cp .env.example .env
   ```
2. Open the newly created `.env` file and declare your secret environment variables:
   ```env
   # Example variables required at compilation time
   SECURE_SALT_KEY=my_secure_64_character_salt_string
   ```
3. During compilation, the Secrets Plugin parses the `.env` file and injects values into the generated `BuildConfig` class, making them accessible in Kotlin code securely.

---

## 🧪 4. Linting and Testing

Validate and test your codebase changes before committing them:

### 4.1 Execute Static Linting Analysis
Ensure code style, structure, and accessibility tags align with Android best practices:
```bash
gradle :app:lintDebug
```
* **Generated Report**: Open `app/build/reports/lint-results-debug.html` in your web browser.

### 4.2 Run Local JUnit and Robolectric Tests
Verify core business logic, database migrations, and presentation state machines in a local JVM environment without launching an emulator:
```bash
gradle :app:testDebugUnitTest
```

---

[[Home]] | [<< Settings](Settings) | [[CI/CD Pipeline >>](CI-CD-Pipeline)]
