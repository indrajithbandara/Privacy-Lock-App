# Contributing Guidelines

Thank you for your interest in contributing to Privacy Lock. This document outlines our development standards, style guides, and contribution workflows.

---

## 1. Development Setup

To begin development:

1. Fork the official repository and clone your fork locally:
   ```bash
   git clone https://github.com/your-username/privacy-lock.git
   cd privacy-lock
   ```
2. Create a feature branch off of the `develop` branch:
   ```bash
   git checkout -b feature/your-awesome-feature develop
   ```
3. Open the project in Android Studio (Ladybug 2024.2.1 or newer).
4. Allow Gradle to synchronize dependencies.
5. Compile and install the application onto a physical device or emulator configured with developer options.

---

## 2. Coding Conventions & Style Guide

To maintain a clean and consistent codebase, all contributions must adhere to the following guidelines:

### 2.1 Kotlin Standards
* Follow the official [Kotlin Style Guide](https://kotlinlang.org/docs/coding-conventions.html).
* Use **Kotlin Coroutines** and **Flows** for all asynchronous operations. Avoid using raw threads, handlers, or RxJava.
* Use **Constructor Injection** for class dependencies to facilitate unit testing.

### 2.2 Jetpack Compose UI
* Follow Material Design 3 guidelines. Always use `MaterialTheme.colorScheme` instead of hardcoded hex colors.
* Interactive components (such as buttons, toggles, list items) **must** have a minimum touch target size of **48dp x 48dp** to comply with Android accessibility standards.
* Ensure all interactive elements include Material ripples for immediate user feedback.
* Provide meaningful content descriptions (`contentDescription`) for all icon buttons and decorative assets.

---

## 3. Commit Message Formatting

We enforce semantic commit messaging. This helps automate changelog generation and version tagging.

### Format
```
<type>(<scope>): <short description>

[Optional body explaining the change in detail]
```

### Types
* `feat`: A new user-facing feature.
* `fix`: A bug fix.
* `docs`: Documentation changes.
* `style`: Code formatting changes (whitespace, semicolons) with no functional impact.
* `refactor`: Code changes that neither fix a bug nor add a feature.
* `test`: Adding or modifying tests.
* `chore`: Updating build files, dependencies, or CI workflows.

### Examples
* `feat(settings): add screenshot protection switch`
* `fix(keypad): center the number 0 on standard keypad layout`
* `test(viewModel): add unit test for password validation`

---

## 4. Branch Management & Pull Requests

Our branching strategy is structured to maintain project stability:

```
                  +---------------------------+
                  |           main            |  <-- Stable Releases
                  +---------------------------+
                               ^
                               | (Releases)
                  +---------------------------+
                  |          develop          |  <-- Integration Branch
                  +---------------------------+
                               ^
                               | (Pull Requests)
         +---------------------+---------------------+
         |                                           |
+---------------------+                     +---------------------+
| feature/add-switch  |                     |   bugfix/fix-keypad |
+---------------------+                     +---------------------+
```

### Pull Request (PR) Requirements
* All pull requests must be targeted against the `develop` branch.
* Ensure your code compiles successfully without warning messages:
  ```bash
  gradle assembleDebug
  ```
* Run local unit and Robolectric integration tests to verify your changes:
  ```bash
  gradle :app:testDebugUnitTest
  ```
* Verify your changes against screenshot tests (if you modified UI layouts):
  ```bash
  gradle :app:verifyRoborazziDebug
  ```

---

[[Home]] | [<< Troubleshooting](Troubleshooting) | [[GitHub Workflow >>](GitHub-Workflow)]
