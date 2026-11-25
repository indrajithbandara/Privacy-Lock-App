# Privacy Lock Supported App Stores & Distribution Channels

This document describes intended distribution channels for the project. No store approval, listing, independent audit, reproducible build or signed public production release is claimed. The intended internal brand is Untouched: Privacy Lock; public branding remains gated by name clearance.

---

## 📦 1. Distribution Matrix

| Channel | Platform Type | Package Type | Build Flavor | Update Mechanism | Verification Method |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Google Play Store** | Intended | AAB (Bundle) | `play` | Google Play | Platform signing / policy review required |
| **GitHub Releases** | Open Source / Direct | APK (Signed) | `opensource` | Manual / Obtainium | SHA-256 Checksum |
| **F-Droid** | Candidate only | Source-built | `opensource` | F-Droid Client Sync | Inclusion and signing not verified |
| **IzzyOnDroid** | Open Source / Staging | APK (Signed) | `opensource` | Izzy Repository Scan | Developer Signed |
| **Obtainium** | Open Source / Direct | APK (Signed) | `opensource` | Direct GitHub Release | SHA-256 Verification |

---

## ⚙️ 2. Detailed Channel Configurations

### 1. Google Play Store (`play` build type)
*   **Targeting**: Android API 36 target, API 24 minimum. Compliance and store acceptance need separate review.
*   **Package Format**: Android App Bundle (`.aab`) to enable Google Play Dynamic Delivery (reducing download sizes by splitting resource assets based on device configurations).
*   **Privacy**: The core app has no Internet permission or analytics/tracking SDK; no optional Firebase/GMS integration is advertised.
*   **Accessibility**: App locking relies on user-authorized window-state events and package identifiers, not screen content. Do not declare the app as an accessibility tool intended for disability support. Before submission, supply the accurate Play Console declaration and demonstration, prominent in-app disclosure, affirmative consent, privacy policy and data safety form. See [Google Play sensitive API policy](https://support.google.com/googleplay/android-developer/answer/16558241).
*   **Package visibility**: Scoped launcher/home intent queries are used rather than `QUERY_ALL_PACKAGES`. See [Google Play package visibility policy](https://support.google.com/googleplay/android-developer/answer/10158779).

### 2. GitHub Releases & Obtainium
*   **Targeting**: Power users seeking immediate, direct updates.
*   **Package Format**: Universal signed APKs (`.apk`) containing full native architecture binary sets.
*   **Obtainium Compatibility**: Candidate distribution through signed GitHub release assets; installation and update behavior must be verified with actual releases.

### 3. F-Droid (The Open Source Repository)
*   **Targeting**: Strict open-source privacy advocates.
*   **Eligibility**: Candidate only. Verify source availability, dependency licenses, native SQLCipher artifact provenance, reproducibility and F-Droid inclusion criteria before submission. No F-Droid build or signature is verified.
*   **Build Recipe**: Intended `opensourceRelease` task; store-specific metadata and reproducible-build evidence must be reviewed.

### 4. IzzyOnDroid (Apk-Based F-Droid Staging)
*   **Targeting**: Fast-track open source updates prior to main F-Droid index inclusion.
*   **Process**: Proposed after a verified signed GitHub release; no IzzyOnDroid inclusion has been established.

---

## 🏗️ 3. Gradle Command Mapping

To compile packages targeted for specific stores, developers execute these mapping commands:

```bash
# Build Open Source Compliant APK (for GitHub Releases, F-Droid staging, IzzyOnDroid)
gradle :app:assembleOpensourceRelease

# Build Google Play Compliant App Bundle (AAB)
gradle :app:bundlePlayRelease
```
