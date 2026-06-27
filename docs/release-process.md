# Privacy Lock Release Process Guide

This document describes the release checklist, signing procedures, automated pipelines, and publishing steps used to distribute official stable builds of **Privacy Lock**.

---

## 🏗️ 1. Release Progression Overview

To ensure maximum safety and stability, all releases follow a strict progression pipeline before deployment:

```
[ Develop Branch ] ──> [ Release Candidate (RC) ] ──> [ QA Verification ] ──> [ Production Tag ] ──> [ Deploy ]
```

* **Step 1: Code Freeze**: Active development is paused. A release candidate branch (e.g., `release/v1.1.0-rc1`) is created from `main`.
* **Step 2: Automated Verifications**: The full test suite (Robolectric unit tests and Roborazzi screenshot tests) must compile and pass on the branch.
* **Step 3: QA Audit**: Manual QA verifications are performed on physical test devices running different Android versions to verify background service stability.
* **Step 4: Tag & Compile**: A production tag (e.g., `v1.1.0`) is pushed to trigger the signed compilation pipeline.
* **Step 5: Publish**: Signed binaries are published to F-Droid and GitHub Releases.

---

## 🔐 2. Safe Code Signing Procedures

To prevent unauthorized builds from replacing installed versions of the app, production binaries are signed using a secure Java Keystore (`.jks`) file.

### Keystore Configuration (`app/build.gradle.kts`):
```kotlin
signingConfigs {
    create("release") {
        val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
        val keystoreFile = file(keystorePath)
        if (keystoreFile.exists()) {
            storeFile = keystoreFile
            storePassword = System.getenv("STORE_PASSWORD")
            keyAlias = "upload"
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
}
```

### GitHub Actions Secrets:
Signing keys are stored securely in the repository settings and are never hardcoded in the codebase.
* **`RELEASE_KEYSTORE_BASE64`**: Base64-encoded representation of the production `.jks` file.
* **`RELEASE_KEYSTORE_PASSWORD`**: Store password of the keystore.
* **`RELEASE_KEY_ALIAS`**: Key alias name.
* **`RELEASE_KEY_PASSWORD`**: Private key password.

---

## 🚀 3. Step-by-Step Release Compilation

Follow these steps to tag and compile a production release:

1. **Verify Local Status**: Ensure all local changes are committed and pushed to `main`.
2. **Create Release Tag**: Create an annotated Git tag on the stable commit:
   ```bash
   git tag -a v1.1.0 -m "Release version 1.1.0 stable"
   ```
3. **Push Tag to GitHub**:
   ```bash
   git push origin v1.1.0
   ```
4. **Automated Compilation**: Pushing the tag triggers the GitHub Actions release workflow. The workflow checks out the tag, restores the signing keys from repository secrets, and compiles the production APK and AAB.
5. **Publish Assets**: The workflow automatically attaches the signed binaries (`privacy-lock-v1.1.0.apk` and `privacy-lock-v1.1.0.aab`) to a new GitHub Release draft.

---

## 🛡️ 4. SHA-256 Checksum Signature Verification

After compiling a release, generate and publish its SHA-256 checksum to allow users to verify the integrity of the downloaded APK:

### Generating Checksums:
Run the appropriate command in your terminal depending on your operating system:
* **Linux**:
  ```bash
  sha256sum privacy-lock-release.apk > privacy-lock-release.apk.sha256
  ```
* **macOS**:
  ```bash
  shasum -a 256 privacy-lock-release.apk > privacy-lock-release.apk.sha256
  ```
* **Windows (PowerShell)**:
  ```powershell
  Get-FileHash privacy-lock-release.apk -Algorithm SHA256 | Format-List > privacy-lock-release.apk.sha256
  ```

Copy and paste the generated checksum string directly into the release description on GitHub.

---

## 🏪 5. F-Droid & Distribution Platforms

To support open-source distribution on F-Droid:

* **Fastlane Metadata**: Maintain localized descriptions, release notes, and screenshot graphics under `/metadata/en-US/` folders.
* **Build Receipts**: Provide F-Droid maintainers with build receipts containing the correct Git commit hashes and build instructions to compile and sign verification builds successfully.
