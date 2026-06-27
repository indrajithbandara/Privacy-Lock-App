# Release Process

This document details the lifecycle of preparing, signing, and distributing a new release of Privacy Lock.

---

## 1. Versioning Protocol

We follow **Semantic Versioning 2.0.0 (SemVer)** to tag release configurations:

$$\text{Format: } \mathbf{vMAJOR.MINOR.PATCH}$$

* **`MAJOR`**: Includes breaking changes (such as database migrations with breaking schemas or major architectural overhauls).
* **`MINOR`**: Adds new features in a backward-compatible manner (e.g., adding Random Keypad options).
* **`PATCH`**: Backward-compatible bug fixes and optimizations (e.g., resolving a layout issue on smaller screens).

---

## 2. Release Steps

When preparing a release, maintainers execute the following steps:

```
+---------------------------------------------------------+
|                  RELEASE CHECKLIST                      |
+---------------------------------------------------------+
                             |
                             v  Prepare changelog
+---------------------------------------------------------+
| Update CHANGELOG.md & App build.gradle.kts (versionCode) |
+---------------------------------------------------------+
                             |
                             v  Compile release binaries
+---------------------------------------------------------+
| gradle assembleRelease && gradle bundleRelease          |
+---------------------------------------------------------+
                             |
                             v  Tag release
+---------------------------------------------------------+
| git tag -a v1.2.0 -m "Release v1.2.0" && git push tags  |
+---------------------------------------------------------+
                             |
                             v  Deploy and distribute
+---------------------------------------------------------+
| Deploy to Play Store Console / Publish on F-Droid       |
+---------------------------------------------------------+
```

### Step 1: Update Version Configurations
Update the application version configurations in `app/build.gradle.kts`:
* Increment the `versionCode` (integer) chronologically.
* Update `versionName` (string) matching SemVer rules.

### Step 2: Compile Production Artifacts
Generate the production-ready binaries on a secure workstation:
```bash
gradle clean :app:assembleRelease :app:bundleRelease
```
* **Release APK**: `app/build/outputs/apk/release/app-release-unsigned.apk`
* **App Bundle**: `app/build/outputs/bundle/release/app-release.aab`

### Step 3: Sign the Artifacts
Production binaries must be signed using the release keystore:
```bash
apksigner sign --ks release.jks --out app-release-signed.apk app-release-unsigned.apk
```

---

## 3. Distribution Channels

### 3.1 Google Play Store Distribution
1. Log in to the **Google Play Console**.
2. Select the **Privacy Lock** application.
3. Create a new release in the **Production Track**.
4. Upload the signed Android App Bundle (`.aab`) file.
5. Provide localized Release Notes mapping the corresponding entries inside `CHANGELOG.md`.
6. Submit the release for review.

### 3.2 F-Droid (FOSS App Store)
As an open-source security application, Privacy Lock is published on F-Droid.
* **Build Recipe**: F-Droid compiles applications from source using its own build runners. 
* **Update Process**:
  1. Once a release is tagged on GitHub, the F-Droid metadata parser detects the update automatically.
  2. The parser updates the F-Droid build recipe (`com.example.yml`) at [fdroiddata](https://gitlab.com/fdroid/fdroiddata).
  3. F-Droid's build runners compile the binary, signing it with F-Droid's key, and publish it within 24 to 72 hours.

---

## 4. Code Minification & ProGuard Configuration

To optimize binary size and prevent reverse engineering, release builds are processed using **R8/ProGuard**:

* ProGuard configurations are managed via `/app/proguard-rules.pro`.
* **Important Rules**: Because the local database uses reflection to instantiate DAO tables and entities, we must prevent ProGuard from obfuscating or removing our database classes:
  ```proguard
  # Keep database entities and models intact
  -keep class com.example.data.** { *; }
  -keepclassmembers class com.example.data.** { *; }
  ```

---

[[Home]] | [<< GitHub Workflow](GitHub-Workflow) | [[Changelog >>](Changelog)]
