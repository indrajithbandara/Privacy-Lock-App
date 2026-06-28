# Building Privacy Lock from Source

This document contains step-by-step instructions for setting up your local development environment and compiling **Privacy Lock** from its Kotlin source code.

---

## 🛠️ 1. Development Prerequisites

To compile the application, ensure your workstation meets these software requirements:

*   **Operating System**: macOS, Linux, or Windows 10/11.
*   **Java Development Kit (JDK)**: JDK 21 (Temurin or OpenJDK recommended).
*   **Android Studio**: Android Studio Ladybug (2024.2.1) or newer.
*   **Android SDK**: Android 14+ SDK (API Level 34/35/36 installed).
*   **Gradle**: Configured automatically via Gradle wrapper.

---

## ⚙️ 2. Step-by-Step Build Setup

### Step 1: Clone the Repository
Clone the codebase to your local workstation:
```bash
git clone https://github.com/indrajithbandara/Privacy-Lock-App.git
cd Privacy-Lock-App
```

### Step 2: Establish Your Local Environment
The project relies on the **Secrets Gradle Plugin** to manage environment credentials safely.
Create your local `.env` configuration file by copying the template file:
```bash
cp .env.example .env
```
*(No sensitive keys are needed for basic builds; this satisfies compilation requirements.)*

### Step 3: Run Baseline Compilation Check
Compile the debug APK package to verify your environment configurations:
```bash
./gradlew assembleDebug
```

---

## 📦 3. Compiling Specific Flavors

The project supports two distribution product flavors: `play` (for Google Play Store) and `opensource` (for F-Droid, GitHub, and IzzyOnDroid).

### Build Open Source APKs
To build the fully open-source release package (free of proprietary hooks):
```bash
./gradlew :app:assembleOpensourceRelease
```
The output APK file will be compiled at:
`app/build/outputs/apk/opensource/release/app-opensource-release.apk`

### Build Google Play App Bundles
To build the official signed production bundle ready for Play Store Console uploads:
```bash
./gradlew :app:bundlePlayRelease
```
The output AAB file will be compiled at:
`app/build/outputs/bundle/playRelease/app-play-release.aab`

---

## 🧪 4. Running the Test Suite

We use Robolectric and Roborazzi to execute local unit and screenshot tests inside the local JVM:

```bash
# Execute local unit and Robolectric integration tests
./gradlew :app:testDebugUnitTest

# Record a new set of reference screenshots (if UI was altered)
./gradlew :app:recordRoborazziDebug

# Verify layouts against reference screenshots
./gradlew :app:verifyRoborazziDebug
```
