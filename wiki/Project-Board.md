# GitHub Projects v2 Blueprint: Privacy Lock App

This document serves as the complete, actionable blueprint for setting up and managing the official GitHub Project Board (Projects v2) for the **Privacy Lock** application. It is designed to emulate a professional, high-performing Android engineering team's agile workflow.

---

## 📋 1. Project Board Schema

### Kanban Columns (Status)
The project board utilizes a detailed pipeline to manage issues from inception to production verification:

| Column | Description |
| :--- | :--- |
| **Backlog** | Unprioritized, unscheduled feature ideas, user suggestions, and low-priority bug reports. |
| **Planned** | Triaged and approved items scheduled for a specific upcoming milestone but not yet ready for development. |
| **Ready** | Fully groomed tasks with precise technical specifications, ready to be assigned and pulled by developers. |
| **In Progress** | Active development is underway. A branch has been created and code is being written. |
| **In Review** | Development is complete. A Pull Request is open, undergoing peer reviews and CI checks. |
| **Testing** | PR is merged into `develop`. Changes are undergoing automated unit/screenshot checks and manual QA builds. |
| **Release Ready** | Approved features and hotfixes compiled into a release candidate, awaiting final signing and distribution. |
| **Released** | The release candidate is pushed to production (GitHub Releases, Play Store, or F-Droid). |
| **Done** | Issues are closed, verified, and retrofitted. No further action is required. |

### Custom Fields Configuration
To categorize, track, and measure progress, set up the following custom fields in your GitHub Project v2:

```yaml
fields:
  - name: "Priority"
    type: "Single select"
    options:
      - label: "🔴 Critical"
        color: "red"
        description: "Blocks core security mechanics or causes application crash."
      - label: "🟠 High"
        color: "orange"
        description: "Major feature or critical UX improvement."
      - label: "🟡 Medium"
        color: "yellow"
        description: "Standard features, enhancements, or internal improvements."
      - label: "🟢 Low"
        color: "green"
        description: "Minor visual adjustments, non-urgent refactoring, or trivial docs."

  - name: "Complexity"
    type: "Single select"
    options:
      - label: "S (1-2 pts)"
        color: "blue"
        description: "Simple changes, small text edits, or minor styling tweaks."
      - label: "M (3-5 pts)"
        color: "cyan"
        description: "Standard UI screens, localized component refactoring, database migrations."
      - label: "L (8-13 pts)"
        color: "purple"
        description: "Major background services, cryptographic components, complex Compose states."
      - label: "XL (20+ pts)"
        color: "magenta"
        description: "Multi-module architecture, complete system rewrites, system overlays."

  - name: "Category"
    type: "Single select"
    options:
      - "Architecture"
      - "Security"
      - "Features"
      - "Performance"
      - "UI / UX"
      - "Testing"
      - "Documentation"
      - "Open Source"
      - "Distribution"
      - "CI/CD"

  - name: "Platform"
    type: "Single select"
    options:
      - "Android SDK"
      - "Jetpack Compose"
      - "SQLite / Room"
      - "GitHub Actions"
      - "Repository Metadata"

  - name: "Progress %"
    type: "Number"
    min: 0
    max: 100

  - name: "Target Release"
    type: "Single select"
    options:
      - "v1.0.0 (Launch)"
      - "v1.1.0 (Biometrics & Alerts)"
      - "v1.2.0 (Schedules & Patterns)"
      - "v2.0.0 (Profiles & Wrapping)"
```

---

## 🏷️ 2. Labeling Strategy

Use these standard repository labels to categorize issue and pull request cards:

| Label | Color | Description |
| :--- | :--- | :--- |
| `bug` | `#d73a4a` | Something is broken or not functioning correctly. |
| `feature` | `#a2eeef` | New feature requests or visual enhancements. |
| `enhancement` | `#3c82f6` | Polishing existing features, visuals, or code structure. |
| `security` | `#000000` | Security-related implementations, encryption, or vulnerability fixes. |
| `performance` | `#e9967a` | Optimizations targeting RAM, battery, startup, or DB queries. |
| `documentation` | `#0075ca` | Modifications to Markdown documentation, wiki pages, or comments. |
| `good first issue` | `#7057ff` | Trivial entry points suitable for external contributors. |
| `help wanted` | `#008672` | Complex challenges requiring community assistance or discussion. |
| `android` | `#3ddc84` | Android OS configurations, framework interfaces, or Gradle rules. |
| `compose` | `#4285f4` | Jetpack Compose UI layout, state, or styling code. |
| `backend` | `#673ab7` | Repository architecture, database layers, and core logic. |

---

## ⚙️ 3. Project Automation Workflows

We leverage GitHub Projects v2 native workflows combined with custom GitHub Action events to automate status changes:

### Built-In Project Workflows
Configure the following rules directly within the **Workflows** settings panel of your GitHub Project:

1. **Auto-Assign to "In Progress"**:
   * **Trigger**: Item assigned to a user or branch created.
   * **Action**: Move card from `Ready` to `In Progress`.
2. **Auto-Review Flow**:
   * **Trigger**: Pull Request opened.
   * **Action**: Move associated issue card from `In Progress` to `In Review`.
3. **Draft Pull Request Protection**:
   * **Trigger**: Draft PR opened.
   * **Action**: Keep card in `In Progress` but append review pending label.
4. **CI Testing Pipeline**:
   * **Trigger**: PR review approved and checks pass.
   * **Action**: Move card from `In Review` to `Testing`.
5. **Closed Resolution**:
   * **Trigger**: Issue closed.
   * **Action**: Move card to `Done`.

### GitHub Actions Automations
You can also run GitHub workflows to automatically adjust project custom fields when specific events occur. 

Example YAML snippet to automate the move to "Released" on tag publication:

```yaml
name: Project Automation - Release Sync

on:
  release:
    types: [published]

jobs:
  update_project:
    runs-on: ubuntu-latest
    steps:
      - name: Move Issues to Released
        uses: actions/add-to-project@v0.5.0
        with:
          project-url: https://github.com/users/indrajithbandara/projects/1
          github-token: ${{ secrets.PROJECT_AUTOMATION_PAT }}
```

---

## 🗺️ 4. Roadmap & Milestones

Our development cycle is structured into four sequential milestones:

```
+-----------------------------------------------------------------------------------+
| v1.0.0 (Launch)                                                                   |
| - Core Interceptor (AppAccessibilityService)                                      |
| - Dual Credentials (Primary & Decoy)                                              |
| - Offline logs & Intruder Center                                                  |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| v1.1.0 (Biometrics & Alerts)                                                      |
| - Biometric fallbacks using androidx.biometric                                     |
| - Intruder Alert Badge Notifications on launch                                    |
| - Android 15 Edge-to-Edge and 16 Preview Support                                  |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| v1.2.0 (Schedules & Patterns)                                                     |
| - Customizable Lock Schedules via WorkManager                                      |
| - 3x3 Gestural Pattern Screen (Jetpack Compose Canvas)                            |
| - Advanced Database backups with local export/import                              |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| v2.0.0 (Profiles & Wrapping)                                                      |
| - Multi-Profile Decoy Sandboxing (Hiding app lists based on PIN)                  |
| - SQLite Encryption via SQLCipher                                                 |
| - Keystore-backed AES/GCM Database Key Wrapping                                   |
+-----------------------------------------------------------------------------------+
```

---

## 📂 5. Realistic Task Directory

Below is the complete, high-fidelity backlog of task specifications representing real-world efforts. This can be directly imported into GitHub issues or project draft cards.

### 📐 Category: Architecture

#### TASK-ARCH-01: Establish Room Offline-First Database Structure
* **Technical Description**: Implement database singletons with Kotlin Symbol Processing (KSP). Define schemas for `LockedApp`, `SecurityConfig`, `IntruderSelfie`, and `TimelineEvent` with safe migration setups.
* **Metadata**:
  * **Priority**: 🔴 Critical
  * **Complexity**: L (8-13 pts)
  * **Platform**: SQLite / Room
  * **Target Release**: `v1.0.0 (Launch)`

#### TASK-ARCH-02: Implement Flow-based Presentation State Synchronization
* **Technical Description**: Refactor UI observers to utilize `StateFlow` and Compose `collectAsStateWithLifecycle` to avoid lifecycle leaks during configuration changes or background context switches.
* **Metadata**:
  * **Priority**: 🟠 High
  * **Complexity**: M (3-5 pts)
  * **Platform**: Jetpack Compose
  * **Target Release**: `v1.0.0 (Launch)`

#### TASK-ARCH-03: Decouple Business Logic into Repository & AppLockManager
* **Technical Description**: Extract direct database query statements from `AppAccessibilityService` and `LockActivity`. Route all reads/writes through a thread-safe repository singleton and a centralized session manager.
* **Metadata**:
  * **Priority**: 🟠 High
  * **Complexity**: M (3-5 pts)
  * **Platform**: Android SDK
  * **Target Release**: `v1.0.0 (Launch)`

---

### 🛡️ Category: Security

#### TASK-SEC-01: Biometric Fallback Authentication Flow
* **Technical Description**: Integrate `androidx.biometric:biometric-ktx`. Allow users to toggle biometric access in settings. Display system biometric prompt with fallback to Master PIN when app launch is intercepted.
* **Metadata**:
  * **Priority**: 🔴 Critical
  * **Complexity**: L (8-13 pts)
  * **Platform**: Android SDK
  * **Target Release**: `v1.1.0 (Biometrics & Alerts)`

#### TASK-SEC-02: Scrambled Keypad Shuffling Mechanics
* **Technical Description**: Implement random keypad layout shuffling. Numbers $1$-$9$ are randomized on every display event using `java.security.SecureRandom`. Keep $0$, `Clear`, and `Backspace` locked in their standard design positions.
* **Metadata**:
  * **Priority**: 🟠 High
  * **Complexity**: M (3-5 pts)
  * **Platform**: Jetpack Compose
  * **Target Release**: `v1.0.0 (Launch)`

#### TASK-SEC-03: Implement Hardware-Backed Keystore Database Wrapping
* **Technical Description**: Integrate SQLCipher support for the Room database. Wrap/unwrap the database encryption key securely using AES/GCM keys stored inside the hardware-backed Android Keystore system.
* **Metadata**:
  * **Priority**: 🔴 Critical
  * **Complexity**: XL (20+ pts)
  * **Platform**: SQLite / Room
  * **Target Release**: `v2.0.0 (Profiles & Wrapping)`

#### TASK-SEC-04: Secure Window Flag Administration
* **Technical Description**: Configure window layouts to dynamically apply `WindowManager.LayoutParams.FLAG_SECURE` based on database configuration. Block screenshot hooks, recorders, and task preview recents cache.
* **Metadata**:
  * **Priority**: 🟠 High
  * **Complexity**: S (1-2 pts)
  * **Platform**: Android SDK
  * **Target Release**: `v1.0.0 (Launch)`

---

### 🚀 Category: Features

#### TASK-FEAT-01: Lock Interceptor Schedules Engine
* **Technical Description**: Design a background scheduler utilizing Android `WorkManager` and `AlarmManager`. Support configuring custom weekly time windows during which target applications are automatically locked or bypassed.
* **Metadata**:
  * **Priority**: 🟡 Medium
  * **Complexity**: L (8-13 pts)
  * **Platform**: Android SDK
  * **Target Release**: `v1.2.0 (Schedules & Patterns)`

#### TASK-FEAT-02: Multi-Profile Decoy Sandboxing Interface
* **Technical Description**: Enable support for multiple Decoy PINs. Depending on which Decoy PIN is typed, completely filter and display a matching sandboxed application listing on the Dashboard to avoid social suspicion.
* **Metadata**:
  * **Priority**: 🟡 Medium
  * **Complexity**: L (8-13 pts)
  * **Platform**: Android SDK
  * **Target Release**: `v2.0.0 (Profiles & Wrapping)`

#### TASK-FEAT-03: Intruder Snapshot Recovery Setup
* **Technical Description**: When a PIN attempt fails 3 times, utilize the camera permission to capture a localized snapshot in the background. Store the image locally inside the app's protected file system and log a timeline event.
* **Metadata**:
  * **Priority**: 🟠 High
  * **Complexity**: L (8-13 pts)
  * **Platform**: Android SDK
  * **Target Release**: `v1.1.0 (Biometrics & Alerts)`

#### TASK-FEAT-04: Panic PIN Quick Escape Action
* **Technical Description**: Create a dedicated "Panic PIN". When typed on the secure overlay keypad, immediately terminate all background activities, clear the unlock cache, and force route the user back to the system Home Screen launcher.
* **Metadata**:
  * **Priority**: 🟠 High
  * **Complexity**: M (3-5 pts)
  * **Platform**: Android SDK
  * **Target Release**: `v1.1.0 (Biometrics & Alerts)`

---

### ⚡ Category: Performance

#### TASK-PERF-01: Accessibility Event Parsing Optimization
* **Technical Description**: Benchmark and refactor accessibility interceptor callbacks. Reduce object allocation and avoid thread blockages inside the main loop of `onAccessibilityEvent` to maintain target system responsiveness.
* **Metadata**:
  * **Priority**: 🟠 High
  * **Complexity**: M (3-5 pts)
  * **Platform**: Android SDK
  * **Target Release**: `v1.0.0 (Launch)`

#### TASK-PERF-02: Database Query Caching Architecture
* **Technical Description**: Store locked package lists inside an in-memory thread-safe state cache within `AppLockManager` to avoid querying Room database SQLite files on every system window state change.
* **Metadata**:
  * **Priority**: 🟠 High
  * **Complexity**: S (1-2 pts)
  * **Platform**: SQLite / Room
  * **Target Release**: `v1.0.0 (Launch)`

#### TASK-PERF-03: Benchmark Battery Usage Under Long Intercept Operations
* **Technical Description**: Conduct profiles using Android Profiler over 12-hour background cycles. Ensure accessibility state change evaluations use less than 1% of the system battery footprint.
* **Metadata**:
  * **Priority**: 🟡 Medium
  * **Complexity**: S (1-2 pts)
  * **Platform**: Android SDK
  * **Target Release**: `v1.2.0 (Schedules & Patterns)`

---

### 🎨 Category: UI / UX

#### TASK-UI-01: Standardize Material 3 Theme & Sage Palette
* **Technical Description**: Build a centralized M3 color scheme utilizing Sage (primary), Forest (secondary), and Olive (tertiary) custom accent mappings. Handle smooth background and surface transitions in dark/light variations.
* **Metadata**:
  * **Priority**: 🟠 High
  * **Complexity**: S (1-2 pts)
  * **Platform**: Jetpack Compose
  * **Target Release**: `v1.0.0 (Launch)`

#### TASK-UI-02: Keypad Numeric Press Visual Ripple Effect
* **Technical Description**: Standardize button size to at least 72dp to satisfy accessibility touch target scales. Build soft tactile container shapes using Compose Canvas and customize state-based Material 3 click ripples.
* **Metadata**:
  * **Priority**: 🟡 Medium
  * **Complexity**: S (1-2 pts)
  * **Platform**: Jetpack Compose
  * **Target Release**: `v1.0.0 (Launch)`

#### TASK-UI-03: Onboarding On-Device Interactive Simulator
* **Technical Description**: Create an interactive phone preview simulation on the Dashboard screen. Help users visually practice toggling locks, inputting decoy PINs, and understanding the background overlay flow on-screen.
* **Metadata**:
  * **Priority**: 🟡 Medium
  * **Complexity**: M (3-5 pts)
  * **Platform**: Jetpack Compose
  * **Target Release**: `v1.0.0 (Launch)`

---

### 🧪 Category: Testing

#### TASK-TEST-01: Compose UI Screenshot Testing Integration
* **Technical Description**: Configure Roborazzi inside `app/build.gradle.kts` and setup JVM screenshot verification routines for `DashboardScreen`, `PrivacyCenterScreen`, and `LockActivity` secure keypads.
* **Metadata**:
  * **Priority**: 🟠 High
  * **Complexity**: M (3-5 pts)
  * **Platform**: Jetpack Compose
  * **Target Release**: `v1.0.0 (Launch)`

#### TASK-TEST-02: Robolectric Critical User Journey Verification
* **Technical Description**: Write unit tests verifying that launching locked apps, inserting incorrect PIN codes, inputting standard PINs, or using decoy keys correctly transition AppLockManager state hashes.
* **Metadata**:
  * **Priority**: 🟠 High
  * **Complexity**: M (3-5 pts)
  * **Platform**: Android SDK
  * **Target Release**: `v1.0.0 (Launch)`

#### TASK-TEST-03: Verify Compatibility with Android 15 & 16 Previews
* **Technical Description**: Set up a virtual device utilizing Android 15 and 16 developer preview SDK targets. Validate that the accessibility window transition callbacks do not fail under updated edge-to-edge constraints.
* **Metadata**:
  * **Priority**: 🟡 Medium
  * **Complexity**: M (3-5 pts)
  * **Platform**: Android SDK
  * **Target Release**: `v1.1.0 (Biometrics & Alerts)`

---

### 📦 Category: Distribution & DevOps

#### TASK-DEVOPS-01: Setup GitHub Actions CI Building Infrastructure
* **Technical Description**: Write a complete GitHub Actions workflow under `.github/workflows/main.yml`. Automate code checkouts, JDK setup, linter check runs, Robolectric JUnit testing, and compiled debug APK outputs.
* **Metadata**:
  * **Priority**: 🟠 High
  * **Complexity**: M (3-5 pts)
  * **Platform**: GitHub Actions
  * **Target Release**: `v1.0.0 (Launch)`

#### TASK-DEVOPS-02: Secure Signing Configuration Setup
* **Technical Description**: Implement secure signing configs in `app/build.gradle.kts`. Use environment variables mapped to system actions to execute release signing checks without storing plain keys inside the public repository.
* **Metadata**:
  * **Priority**: 🟠 High
  * **Complexity**: S (1-2 pts)
  * **Platform**: GitHub Actions
  * **Target Release**: `v1.0.0 (Launch)`

#### TASK-DEVOPS-03: Formulate F-Droid Distribution Schema
* **Technical Description**: Set up f-droid metadata files, app descriptions, and screenshot indexes matching official guidelines. Prepare the source tag repository structure for integration inside F-Droid's build metadata.
* **Metadata**:
  * **Priority**: 🟡 Medium
  * **Complexity**: S (1-2 pts)
  * **Platform**: Repository Metadata
  * **Target Release**: `v1.1.0 (Biometrics & Alerts)`

---

## 🚀 6. Importing this Project to GitHub

To quickly instantiate this configuration on GitHub:
1. Go to your GitHub organization or profile, and select **Projects**.
2. Click **New Project** and choose the **Board** layout (v2).
3. Create custom fields as defined in **Section 1 (Project Board Schema)**.
4. Set up the automation rules under **Workflows** to match **Section 3 (Project Automation Workflows)**.
5. Create issues in your repository copying the technical descriptions and metadata from **Section 5 (Realistic Task Directory)**, and add them to your board.
