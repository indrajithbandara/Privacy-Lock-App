# GitHub Workflow & CI/CD

This document details our GitHub repository management, issue tracking, versioning policies, and automated Continuous Integration (CI) pipelines.

---

## 1. Branching Strategy

We follow a modified **GitFlow** branching strategy designed to keep the main release branch stable while allowing rapid development in staging.

* **`main`**: Represents production-ready releases. Every commit to `main` is tagged with a semantic version (e.g., `v1.2.0`) and compiled into a release APK or AAB.
* **`develop`**: The primary integration branch. All active feature branches and bug fixes are merged here.
* **`feature/*`**: Dedicated branches for individual feature development (e.g., `feature/random-keypad`).
* **`bugfix/*`**: Dedicated branches for resolving specific bugs.
* **`hotfix/*`**: Quick patches made directly off of `main` to resolve critical production bugs, which are then merged back into both `main` and `develop`.

---

## 2. Issue and Pull Request (PR) Templates

To maintain high-quality contributions, our repository provides pre-configured templates under `.github/`:

### Issue Templates
* **Bug Report (`bug_report.md`)**: Prompts contributors for device hardware specifications, Android operating system versions, step-by-step reproduction instructions, and actual vs. expected behavior.
* **Feature Request (`feature_request.md`)**: Prompts users to describe the problem, propose a solution, specify Material 3 design requirements, and suggest user scenarios.

### Pull Request Templates
* **PR Template (`pull_request_template.md`)**: Prompts the author to link the related issue, summarize changes, verify compilation, document testing steps (unit and screenshot tests), and note any UI changes.

---

## 3. Continuous Integration (CI) Workflows

Every pull request or push to the `develop` and `main` branches triggers automated test runs and build verifications via **GitHub Actions**.

### Build and Test Pipeline (`.github/workflows/ci.yml`)

The CI environment runs a Linux runner with Android SDK components pre-installed. The build pipeline performs the following tasks:

```
+---------------------------------------------------------+
|                  GITHUB ACTIONS RUNNER                  |
+---------------------------------------------------------+
                             |
                             v  Checkout Code
+---------------------------------------------------------+
|                    Set up Java 17                       |
+---------------------------------------------------------+
                             |
                             v  Restore Gradle Cache
+---------------------------------------------------------+
|                    Restore Gradle Cache                 |
+---------------------------------------------------------+
                             |
                             v  Verify Code Formatting
+---------------------------------------------------------+
|                    gradle lintDebug                     |
+---------------------------------------------------------+
                             |
                             v  Run Unit & Robolectric Tests
+---------------------------------------------------------+
|                gradle :app:testDebugUnitTest            |
+---------------------------------------------------------+
                             |
                             v  Compile Debug APK
+---------------------------------------------------------+
|                 gradle assembleDebug                    |
+---------------------------------------------------------+
                             |
                             v  Compile Unsigned Release APKs
+---------------------------------------------------------+
|                gradle assembleRelease                   |
|                gradle bundleRelease                     |
+---------------------------------------------------------+
```

---

## 4. Pull Request Review Checklist

Maintainers review pull requests against the following criteria:

1. **Compilation Check**: The PR must compile successfully with zero syntax errors.
2. **Coverage Check**: Code modifications must be covered by unit tests (or Robolectric integration tests).
3. **Screenshot Check**: If visual layouts are modified, reference screenshot verifications (`verifyRoborazziDebug`) must pass.
4. **Style Check**: The code must match standard Kotlin coding conventions and design guidelines.
5. **Security Check**: Cryptographic implementations must utilize secure APIs (such as `SecureRandom`). Sensitive credentials must never be hardcoded.

---

[[Home]] | [<< Contributing](Contributing) | [[Release Process >>](Release-Process)]
