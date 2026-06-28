# Release Process and Signing Guidelines

This document details the official release process, semantic versioning guidelines, continuous delivery pipeline executions, and cryptographic signing mechanisms for the **Privacy Lock** project.

---

## 📐 1. Semantic Versioning & Tagging

Privacy Lock strictly adheres to **Semantic Versioning 2.0.0** (SemVer):
`MAJOR.MINOR.PATCH` (e.g., `1.1.2`)

*   **MAJOR**: Incremented when backward-incompatible API changes or structural storage upgrades are introduced.
*   **MINOR**: Incremented when new, backward-compatible features are added (e.g., Decoy PIN modes, pattern locks).
*   **PATCH**: Incremented when backward-compatible bug fixes or security hotfixes are merged.

---

## 🤖 2. Release Automation Workflow

Our CI/CD pipeline (managed via GitHub Actions) automates the compilation, signing, checksum generation, and publishing processes when a new git tag is pushed.

```
┌─────────────────┐      ┌──────────────┐      ┌─────────────────────────┐
│  Git Tag Push   │ ───> │ GitHub CI/CD │ ───> │  Auto-generated Draft   │
│   (e.g., v1.1.0) │      │  Runner Base │      │   GitHub Release Assets │
└─────────────────┘      └──────────────┘      └─────────────────────────┘
```

### Steps Executed in CI/CD Release Phase:
1.  **Checkout & Dependencies**: Cleans workspace, checks out release tag, and sets up JDK 21.
2.  **Lint & Test Verification**: Runs unit, Robolectric, and Roborazzi screenshot verification tasks. If any check fails, the release build aborts instantly.
3.  **Compilation**: Builds release targets for both product flavors:
    *   `opensource` Release APK
    *   `play` Release App Bundle (AAB)
4.  **Signing**:
    *   Injects the base64-encoded upload keystore from GitHub secrets (`RELEASE_KEYSTORE_BASE64`) into a transient `.jks` file.
    *   Signs compile outputs using standard Android `apksigner` and `jarsigner` with store and key passwords stored in secrets.
5.  **Checksum Generation**: Computes SHA-256 checksums for both output packages to enable binary tampering audits:
    ```bash
    sha256sum app-opensource-release.apk > app-opensource-release.apk.sha256
    ```
6.  **Draft Release Creation**: Creates a new Draft Release in the GitHub repository, uploads signed binaries, SHA-256 checksum sheets, and parses standard git logs to generate changelog drafts.

---

## 🔒 3. Manual Verification & Distribution

Once the release workflow completes successfully, repository administrators execute the final publishing steps:

1.  **Checksum Audit**: Verify that the generated SHA-256 signatures match locally compiled binaries exactly.
2.  **Review Release Notes**: Polishing the draft release notes, formatting categorized headings: `🚀 Added`, `🐛 Fixed`, `🔒 Security`.
3.  **Publish Release**: Click **Publish Release** in the GitHub interface, making the release assets public.
4.  **Open Source Syncing**:
    *   **IzzyOnDroid / Obtainium**: Updates will propagate automatically as indexing scripts scan the new release tag.
    *   **F-Droid**: F-Droid builders detect the new tag within 24-72 hours, rebuild from the tag source, sign using F-Droid's official keys, and deploy to the main index.
