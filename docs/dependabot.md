# Dependabot Integration & Dependency Management Guide

This document explains the configuration, weekly schedules, and security audit workflows of the automated Dependabot dependency manager (`.github/dependabot.yml`).

---

## 🏗️ 1. Dependabot Integration Overview

Privacy Lock uses **GitHub Dependabot** to monitor and manage library dependencies. Dependabot runs automated weekly audits on the repository, scanning for outdated libraries or known security vulnerabilities, and submits pull requests to upgrade them.

```
┌────────────────────────────────────────────────────────┐
│                   Dependabot Scans                     │
├────────────────────────────────────────────────────────┤
│  1. Check Schedule - Weekly scans on main branch       │
│  2. Ecosystems     - Gradle, GitHub Actions            │
│  3. PR Creation    - Proposes library upgrades         │
│  4. Safety Checks  - Verifies compile-compatibility     │
└────────────────────────────────────────────────────────┘
```

---

## ⚙️ 2. Dependabot Configuration (`.github/dependabot.yml`)

The configuration file is located in the root repository under `.github/dependabot.yml`:

```yaml
version: 2
updates:
  # Maintain dependencies for Gradle
  - package-ecosystem: "gradle"
    directory: "/"
    schedule:
      interval: "weekly"
      day: "monday"
      time: "08:00"
    timezone: "UTC"
    open-pull-requests-limit: 5

  # Maintain dependencies for GitHub Actions
  - package-ecosystem: "github-actions"
    directory: "/"
    schedule:
      interval: "weekly"
      day: "monday"
      time: "08:00"
    timezone: "UTC"
    open-pull-requests-limit: 3
```

---

## 🚀 3. Updating Dependencies Safely

Because Kotlin applications are tightly integrated with compiler plugins, merging dependency upgrades requires careful validation to prevent compilation failures.

### Key Compatibility Constraints:
* **Kotlin & KSP Coupling**: The Kotlin Symbol Processing (KSP) toolchain is tightly bound to specific Kotlin compiler versions. Do not merge room-compiler or database annotation processing library upgrades without verifying that they are compatible with the project's active Kotlin version.
* **Compose BOM Consistency**: Jetpack Compose libraries are managed using the **Compose Bill of Materials (BOM)**. Ensure individual Compose dependencies are not upgraded independently outside the BOM, to prevent layout or runtime rendering inconsistencies.
* **Coroutines Stability**: Coroutines must remain synchronized across all modules. Keep `kotlinx-coroutines-core` and `kotlinx-coroutines-android` versions locked together to prevent runtime threading issues.

---

## 🛡️ 4. Administrator PR Verification Checklist

Before merging a Dependabot pull request, administrators must complete the following checks:

- [ ] **Verify CI Compatibility**: Ensure that the pull request passes all linter, unit test, and layout rendering checks in the GitHub Actions pipeline.
- [ ] **Verify Plugin Compatibility**: Verify that the proposed dependency version is compatible with our active Kotlin compiler and Gradle plugin versions.
- [ ] **Run Layout Regression Tests**: Verify that there are no layout regressions using Roborazzi screenshot verification.
- [ ] **Verify No Network Permissions**: Verify that the library does not require internet permissions to function, maintaining our zero-knowledge, offline-first security posture.
- [ ] **Verify Performance Metrics**: Ensure that the update does not introduce performance bottlenecks or increase the compiled APK size.
- [ ] **Squash and Merge**: Use squash merging to keep git commit logs clean.
