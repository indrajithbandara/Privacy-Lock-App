# Privacy Lock App Administrator Guide

Welcome to the **Privacy Lock App Administrator Guide**. This document outlines the required workflows, repository settings, continuous integration management, issue triage procedures, and publishing mechanics necessary to administer the Privacy Lock project repository successfully.

---

## 🎨 Navigation Menu

| **Core Manuals** | **Technical Guides** | **Governance & Process** |
| :--- | :--- | :--- |
| 🏠 [[Home]] | 📐 [[Architecture]] | 🛠️ [[Contributing]] |
| 🚀 [[Getting Started]] | 🔌 [[Permissions]] | 🤖 [[CI-CD-Pipeline]] |
| 📲 [[Installation]] | ⚙️ [[Accessibility-Service]] | 🏷️ [[Release-Process]] |
| 📦 [[Features]] | 🔒 [[Security-Model]] | 🗺️ [[Roadmap]] |
| 👤 [[User-Guide]] | 💾 [[Database]] | 🐛 [[Troubleshooting]] |
| 💻 [[Developer-Guide]] | 🧠 [[Lock-Engine]] | ❓ [[FAQ]] |
| 🛡️ [[Administrator-Guide]] | 🧪 [[Testing]] | 📜 [[License]] |

---

## 🛠️ 1. Repository Governance

To maintain a secure, high-quality, enterprise-grade Android application, administrators must enforce strict branch protections and quality gateways:

### Branch Management Policy
*   **Protected Master Branch (`main`)**: 
    *   Direct commits are strictly disabled.
    *   Force-pushes and branch deletions are disallowed.
*   **Pull Request Enforcement**: All integrations must be completed via Pull Requests (PRs).
    *   Each PR requires a minimum of **two approvals** from core maintainers.
    *   All checks must pass, including the automated **Android CI/CD Pipeline** (unit tests, screenshot tests, and static code linter).
    *   All review conversations and threads must be resolved before merging.
    *   PRs must be squash-merged to maintain a linear and clean repository commit log.

---

## 🤖 2. Managing Dependabot Updates

Automated dependency updates are handled via `.github/dependabot.yml` and scheduled weekly.

### Review Procedures for Dependabot Pull Requests:
1.  **Security Verification**: Check for open security advisories (CVEs) related to the dependency using the GitHub Advisory database.
2.  **Verify Compatibility**:
    *   Review the CI/CD test results.
    *   *Warning*: Kotlin compiler and Kotlin Symbol Processing (KSP) versions are tightly coupled. Do not allow Dependabot to upgrade dependencies that pull in new compiler plugins (such as Room KSP generators) without verifying Kotlin compiler version compatibility first.
3.  **Group Merges**: Merge approved updates in groups to reduce version drift across active developer environments.

---

## ⚙️ 3. Managing GitHub Actions Workflows

Automated build operations are declared under `.github/workflows/main.yml`, `.github/workflows/codeql-analysis.yml`, and `.github/workflows/dependency-review.yml`.

### Administrator Checklists:
*   **Caching Policies**: Ensure the `gradle/actions/setup-gradle` cache parameters are configured to read and write on `main`, but read-only on feature branches, protecting build-cache integrity.
*   **Signing Key Rotation**: Release keystores and passcodes are stored in repository actions secrets (`RELEASE_KEYSTORE_BASE64`, `STORE_PASSWORD`, `KEY_PASSWORD`). Update these values in repository settings if security parameters require key rotation.

---

## 📋 4. Issue & PR Triage Procedures

Administrators guide community support, bug logging, and feature proposals using standardized issue workflows:

*   **Triage Steps**:
    1.  When a user logs an issue, apply appropriate labels (`bug`, `feature`, `triage`, `security`).
    2.  If it is a bug, ensure the user provided full environment specifications (Android API Level, Device Model) and reproduction steps.
    3.  If a security vulnerability is reported publicly, close the issue instantly, delete sensitive text, and direct the reporter to follow the security report protocol in `SECURITY.md`.
*   **PR Reviews**: Enforce usage of the standard Pull Request template, checking for test coverage, custom test tags, and accessibility TalkBack support before scheduling merges.

---

## 🏷️ 5. Releases & Versioning

Privacy Lock adheres to **Semantic Versioning (SemVer 2.0.0)** formatting: `MAJOR.MINOR.PATCH` (e.g., `1.1.0`).

### Publish Steps:
1.  Commit final translation strings and documentation enhancements to `main`.
2.  Tag the release branch:
    ```bash
    git tag -a v1.1.0 -m "Release version 1.1.0"
    ```
3.  Push the tag to GitHub:
    ```bash
    git push origin v1.1.0
    ```
4.  The CI pipeline automatically triggers, builds production-ready APKs and AAB packages, signs them, generates SHA-256 checksums, and saves them as assets inside a new GitHub Release draft.
5.  Review the automated release notes, verify checksum signatures, and publish the release.

---

[<< Return to Developer Guide](Developer-Guide) | [Proceed to Testing Guide >>](Testing)
