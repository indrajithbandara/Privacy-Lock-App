# Privacy Lock Supported App Stores & Distribution Channels

This document describes how **Privacy Lock** maintains a single, highly unified codebase while supporting distribution across multiple official Google and Open Source Android application stores.

---

## 📦 1. Distribution Matrix

| Channel | Platform Type | Package Type | Build Flavor | Update Mechanism | Verification Method |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Google Play Store** | Proprietary / Official | AAB (Bundle) | `play` | Google Play Services | Play Protect Signed |
| **GitHub Releases** | Open Source / Direct | APK (Signed) | `opensource` | Manual / Obtainium | SHA-256 Checksum |
| **F-Droid** | Open Source / Verified | Source-built | `opensource` | F-Droid Client Sync | F-Droid Key Signed |
| **IzzyOnDroid** | Open Source / Staging | APK (Signed) | `opensource` | Izzy Repository Scan | Developer Signed |
| **Obtainium** | Open Source / Direct | APK (Signed) | `opensource` | Direct GitHub Release | SHA-256 Verification |

---

## ⚙️ 2. Detailed Channel Configurations

### 1. Google Play Store (`play` build type)
*   **Targeting**: Clean, compliant, and optimized for Android 14+ policies.
*   **Package Format**: Android App Bundle (`.aab`) to enable Google Play Dynamic Delivery (reducing download sizes by splitting resource assets based on device configurations).
*   **Analytics & Services**: Can be configured to bind optional Firebase/GMS helper dependencies (if requested by the user), completely isolated from open-source builds.
*   **Security Scanning**: Fully audited against Google Play Protect regulations.

### 2. GitHub Releases & Obtainium
*   **Targeting**: Power users seeking immediate, direct updates.
*   **Package Format**: Universal signed APKs (`.apk`) containing full native architecture binary sets.
*   **Obtainium Compatibility**: Engineered with semantic release tag configurations, allowing Obtainium to scan the GitHub releases API, fetch update changelogs, parse universal APK assets, and execute secure on-device updates seamlessly.

### 3. F-Droid (The Open Source Repository)
*   **Targeting**: Strict open-source privacy advocates.
*   **F-Droid Criteria Compliance**:
    *   **100% Free Software**: Builds do not contain any proprietary binary blobs or dependencies.
    *   **No Tracking**: Free of telemetry, advertising libraries, or user-tracking SDKs.
    *   **Strictly Built from Source**: F-Droid's continuous builders fetch the source code directly from our tag releases, compile using open-source compilers, and sign using the official F-Droid GPG signing key.
*   **Build Recipe**: Configured via F-Droid metadata using the `opensourceRelease` Gradle assembly task.

### 4. IzzyOnDroid (Apk-Based F-Droid Staging)
*   **Targeting**: Fast-track open source updates prior to main F-Droid index inclusion.
*   **Process**: Izzy OnDroid's automated scripts scan our GitHub Release releases periodically, download the signed `opensourceRelease` APK directly, verify it against open-source constraints, and index it into the Izzy repository.

---

## 🏗️ 3. Gradle Command Mapping

To compile packages targeted for specific stores, developers execute these mapping commands:

```bash
# Build Open Source Compliant APK (for GitHub Releases, F-Droid staging, IzzyOnDroid)
./gradlew :app:assembleOpensourceRelease

# Build Google Play Compliant App Bundle (AAB)
./gradlew :app:bundlePlayRelease
```
