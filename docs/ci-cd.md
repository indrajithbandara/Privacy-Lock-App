# Privacy Lock CI/CD Pipeline Guide

This document describes the design, environment setups, step-by-step pipeline execution, and caching strategies of the automated GitHub Actions continuous integration pipeline (`.github/workflows/main.yml`).

---

## 🏗️ 1. CI/CD Architecture Overview

The automation pipeline runs on **GitHub Actions**. It compiles, tests, and validates every code modification to prevent regressions, keep build environments secure, and automate releases.

```
┌────────────────────────────────────────────────────────┐
│                     CI/CD Pipeline                     │
├────────────────────────────────────────────────────────┤
│  1. Triggers       - PRs, Merges, and Tag Pushes       │
│  2. Environment    - ubuntu-latest, JDK 17, Android SDK│
│  3. Speed Cache    - Caches Gradle files & dependencies │
│  4. Quality Checks - Runs Linter & Unit/Roborazzi tests │
│  5. CD Deployment  - Compiles & signs release on tags   │
└────────────────────────────────────────────────────────┘
```

---

## ⚙️ 2. Workflow Trigger Rules

The pipeline is triggered by the following events:

```yaml
on:
  push:
    branches: [ main ]
    tags:
      - 'v*'
  pull_request:
    branches: [ main ]
```

* **Pushes to `main`**: Runs the full suite of linter checks and unit tests to ensure main branch stability.
* **Pull Requests targeting `main`**: Runs verification checks before code merges.
* **Tag Pushes (`v*`)**: Triggers the Continuous Deployment (CD) pipeline to compile, sign, and publish release packages.

---

## 📦 3. Build Environment Setup

The build environment is configured to run on a standard, high-performance Ubuntu runner:

* **Runner OS**: `ubuntu-latest`
* **Java Development Kit (JDK)**: JDK 17
* **Android SDK**: Automatically loaded by the Android Gradle Plugin (AGP), providing target platform APIs (34, 35, 36) and build-tools.

---

## 🚀 4. Step-by-Step Pipeline Execution

The pipeline executes the following steps in sequence:

### Step 1: Checkout Code
Checks out the repository files under the workspace directory:
```yaml
- name: Checkout Repository
  uses: actions/checkout@v4
```

### Step 2: Set Up JDK 17
Installs JDK 17 on the runner:
```yaml
- name: Set up JDK 17
  uses: actions/setup-java@v4
  with:
    distribution: 'zulu'
    java-version: '17'
```

### Step 3: Gradle Caching
Caches Gradle dependency downloads and compiler intermediates to shorten build times:
```yaml
- name: Setup Gradle Cache
  uses: gradle/actions/setup-gradle@v4
```

### Step 4: Run Static Linter Check
Performs static analysis checks on Kotlin source files:
```yaml
- name: Run Linter Checks
  run: ./gradlew lintDebug
```

### Step 5: Execute Unit Tests
Runs the Robolectric unit and Roborazzi screenshot testing suites inside the local JVM:
```yaml
- name: Run Unit Tests
  run: ./gradlew :app:testDebugUnitTest
```

### Step 6: Compile Debug APK
Compiles the debug application package:
```yaml
- name: Build Debug APK
  run: ./gradlew assembleDebug
```

---

## 🛡️ 5. Continuous Deployment (CD) & Release Automation

When a new release tag is pushed (e.g., `v1.1.0`), the workflow runs additional compilation steps to build and sign production binaries:

### Step 7: Base64 Decrypt Keystore
Decrypts the production signing key stored in repository secrets and saves it locally as a `.jks` file:
```yaml
- name: Decrypt Keystore
  run: |
    echo "${{ secrets.RELEASE_KEYSTORE_BASE64 }}" | base64 --decode > app/my-upload-key.jks
```

### Step 8: Compile Production Binaries
Compiles signed production-ready APK and AAB packages:
```yaml
- name: Build Release Binaries
  run: ./gradlew assembleRelease
  env:
    KEYSTORE_PATH: "my-upload-key.jks"
    STORE_PASSWORD: ${{ secrets.RELEASE_KEYSTORE_PASSWORD }}
    KEY_PASSWORD: ${{ secrets.RELEASE_KEY_PASSWORD }}
```

### Step 9: Publish Draft Release
Attaches the signed packages to a new GitHub Release draft:
```yaml
- name: Create Release
  uses: softprops/action-gh-release@v2
  with:
    files: |
      app/build/outputs/apk/release/app-release.apk
      app/build/outputs/bundle/release/app-release.aab
    draft: true
```
