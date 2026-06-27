# GitHub Project v2 Blueprint: Privacy Lock App Roadmap

This document provides a comprehensive, enterprise-grade specification for configuring the official **GitHub Project (Projects v2)** board for the **Privacy Lock** Android application. It acts as the team's operational manual, defining pipelines, fields, labels, milestones, automations, and detailed task listings to guide developers and managers.

---

## 📋 Project Configuration Metadata

* **Project Name**: `Privacy Lock App Roadmap`
* **Project Description**: *A complete roadmap and project management board for the Privacy Lock Android application. This project tracks feature development, bug fixes, documentation, testing, security improvements, UI/UX enhancements, CI/CD, and release milestones.*
* **Default View Layout**: `Board (Kanban)`

---

## 🗂️ 1. Kanban Status Columns

The project lifecycle is managed across six primary columns, guiding issues and pull requests from raw concepts to production delivery:

| Column Header | Icon | Definition & Entrance Criteria |
| :--- | :---: | :--- |
| **📋 Backlog** | `📋` | Raw ideas, feature proposals, non-blocking enhancement requests, and low-priority improvements. |
| **📝 Planned** | `📝` | Triaged and approved issues. Technically scoped, assigned to a milestone, and awaiting active development. |
| **🚧 In Progress**| `🚧` | Features currently under development, active bug fixes, or documentation updates. Branch created. |
| **👀 In Review**  | `👀` | Work completed. Associated Pull Request is open, undergoing code review, QA checks, or CI testing. |
| **✅ Done**       | `✅` | Fully merged Pull Requests, completed documentation, or closed issues that have been verified on-device. |
| **🚀 Released**   | `🚀` | Features included in an official production release (GitHub Tag/Release, Play Store, or F-Droid). |

---

## ⚙️ 2. Custom Fields Specification

To allow advanced sorting, filtering, and status charting, set up the following custom attribute fields inside your GitHub Project:

```yaml
fields:
  - name: "Priority"
    type: "Single select"
    options:
      - label: "🔴 Critical"
        color: "red"
        description: "Showstopping issues (e.g. security bypass, data corruption, or runtime crashes)."
      - label: "🟠 High"
        color: "orange"
        description: "Main feature requirements, major security hardening, or high-severity bug fixes."
      - label: "🟡 Medium"
        color: "yellow"
        description: "Standard enhancements, UI polishes, secondary feature items, or general optimizations."
      - label: "🟢 Low"
        color: "green"
        description: "Minor visual adjustments, non-urgent refactoring, or trivial documentation updates."

  - name: "Complexity"
    type: "Single select"
    options:
      - label: "S (1-2 pts)"
        color: "blue"
        description: "Trivial effort (e.g. text edits, configuration flags, simple assets replacement)."
      - label: "M (3-5 pts)"
        color: "cyan"
        description: "Standard effort (e.g. single Compose screen, simple DB entity addition, local logic checks)."
      - label: "L (8-13 pts)"
        color: "purple"
        description: "Significant effort (e.g. background work manager routines, custom Accessibility loops)."
      - label: "XL (20+ pts)"
        color: "magenta"
        description: "Complex system changes (e.g. cryptographic key-wrapping, complete module rewrites)."

  - name: "Category"
    type: "Single select"
    options:
      - "Core Interception"
      - "Security Shield"
      - "User Experience"
      - "Performance"
      - "Documentation"
      - "CI/CD & DevOps"
      - "Open Source Governance"
      - "Distribution"

  - name: "Platform"
    type: "Single select"
    options:
      - "Android SDK"
      - "Jetpack Compose"
      - "SQLite / Room"
      - "GitHub Actions"
      - "Metadata & Docs"

  - name: "Progress %"
    type: "Number"
    min: 0
    max: 100

  - name: "Version"
    type: "Single select"
    options:
      - "v0.9 Beta"
      - "v1.0 Stable"
      - "v1.1 Improvements"
      - "v2.0 Major Release"
```

---

## 🏷️ 3. Standard Repository Labels

Configure these labels within your repository (`Settings -> Labels`) to categorize issues and PRs visually:

| Label Name | Recommended Color | Description / Context |
| :--- | :---: | :--- |
| `feature` | `#a2eeef` | New feature implementations or enhancements. |
| `bug` | `#d73a4a` | Code, configuration, or UI regression fixes. |
| `documentation` | `#0075ca` | Markdown wiki updates, README changes, or code annotations. |
| `enhancement` | `#3c82f6` | Polishing existing features, code refactoring, or layout improvements. |
| `security` | `#000000` | Cryptographic mechanisms, biometric wrappers, or vulnerability fixes. |
| `performance` | `#e9967a` | Optimizations targeting RAM, battery, thread blockages, or DB caches. |
| `ui` | `#fef2c0` | User interface structure, Compose grids, button alignments, or colors. |
| `ux` | `#bfdadc` | Interactive user journeys, gesture intercepts, visual feedback, or haptics. |
| `testing` | `#0e8a16` | Automated unit tests, JVM screenshot validations, or QA test cases. |
| `ci/cd` | `#fbca04` | GitHub Actions setups, Gradle configurations, or signing pipelines. |
| `dependencies` | `#1d76db` | Dependabot alerts, build library upgrades, and compiler versions. |
| `good first issue` | `#7057ff` | Trivial tasks perfect for community onboarding and beginners. |
| `help wanted` | `#008672` | Complex architecture debates requiring extra brains or design reviews. |
| `high priority` | `#ff5722` | Requires immediate prioritization or scheduling in the active sprint. |
| `medium priority` | `#ffeb3b` | Scheduled for standard active development tracking. |
| `low priority` | `#8bc34a` | Address when core tasks are finalized; backlog optimization candidates. |

---

## 🎯 4. Milestones & Releases

Development targets are divided into four main milestones:

```
+-----------------------------------------------------------------------------------+
| 📦 Milestone: v0.9 Beta                                                           |
| Target: Core app lock, accessibility intercept, keypad layout, & primary setup.   |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| 📦 Milestone: v1.0 Stable                                                         |
| Target: Screenshot masking, decoy login, local backups, & full CI/CD pipeline.     |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| 📦 Milestone: v1.1 Improvements                                                   |
| Target: Biometric integration, battery optimizations, and Android 15 edge-to-edge. |
+-----------------------------------------------------------------------------------+
                                         |
                                         v
+-----------------------------------------------------------------------------------+
| 📦 Milestone: v2.0 Major Release                                                 |
| Target: AES database encryption, pattern locker overlays, and cloud backups.     |
+-----------------------------------------------------------------------------------+
```

---

## 🤖 5. Automation Rules & Workflows

To reduce project management overhead, enable the following **GitHub Project v2 Native Workflows** in the board's setup panel:

1. **Auto-Assign to "🚧 In Progress"**:
   * **Trigger**: When an issue is linked to a branch or assigned to a team member.
   * **Action**: Move associated item status to `🚧 In Progress`.
2. **Move to "👀 In Review"**:
   * **Trigger**: When a Pull Request referencing an issue is opened.
   * **Action**: Automatically move the issue card to `👀 In Review`.
3. **Move to "Testing"**:
   * **Trigger**: Automated checkouts and unit test suite successfully pass in GitHub Actions.
   * **Action**: Append `Verified-CI` check label and queue for final evaluation.
4. **Move to "🚀 Released"**:
   * **Trigger**: A new production git tag/release is published on GitHub.
   * **Action**: Move all linked closed issue cards to `🚀 Released`.
5. **Move to "✅ Done"**:
   * **Trigger**: When an issue is closed directly or a pull request is merged.
   * **Action**: Move the card directly to `✅ Done`.

---

## 🗂️ 6. Task Backlog Directory (The 26 Project Items)

Below is the complete, high-fidelity directory of **26 realistic tasks** representing real-world efforts for an Android security application. Copy and paste these specifications to populate issues or card drafts:

---

### 🧱 Section A: Core Application Features

#### 1. Core App Lock Interceptor
* **Description**: Implement package transition detection within `AppAccessibilityService` by observing system `TYPE_WINDOW_STATE_CHANGED` broadcasts. Route intercepted launches to the secure blocker.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🔴 Critical
  * **Milestone**: `v0.9 Beta`
  * **Labels**: `feature`, `security`, `android`
  * **Complexity**: L (8-13 pts)
  * **Category**: Core Interception

#### 2. Biometric Authentication Integration
* **Description**: Integrate `androidx.biometric:biometric-ktx`. Display hardware authentication prompt (fingerprint or face unlock) as a primary or fallback verification option in `LockActivity`.
* **Metadata**:
  * **Status**: `📝 Planned`
  * **Priority**: 🟠 High
  * **Milestone**: `v1.1 Improvements`
  * **Labels**: `feature`, `security`, `ui`
  * **Complexity**: M (3-5 pts)
  * **Category**: Security Shield

#### 3. 6-Digit PIN Security Mechanics
* **Description**: Build a robust, 6-digit numeric input passcode setup and verification sequence. Hash credentials securely using SHA-256 with a randomized salt before storing in local Room database.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🔴 Critical
  * **Milestone**: `v0.9 Beta`
  * **Labels**: `feature`, `security`, `backend`
  * **Complexity**: M (3-5 pts)
  * **Category**: Security Shield

#### 4. Random PIN Keypad Grid Layout
* **Description**: Build a custom Jetpack Compose keypad widget that randomizes the display locations of digits $1$-$9$ on every launch event to block shoulder-surfing and fingerprint smudge pattern profiling.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟠 High
  * **Milestone**: `v0.9 Beta`
  * **Labels**: `feature`, `security`, `ui`
  * **Complexity**: M (3-5 pts)
  * **Category**: Security Shield

#### 5. Screenshot Protection (`FLAG_SECURE`)
* **Description**: Apply `WindowManager.LayoutParams.FLAG_SECURE` layout flags dynamic controls to `LockActivity` and settings screens to block screen captures, recorders, and task preview exposures in Recents view.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟠 High
  * **Milestone**: `v1.0 Stable`
  * **Labels**: `feature`, `security`, `ux`
  * **Complexity**: S (1-2 pts)
  * **Category**: Security Shield

#### 6. Auto Lock Timeout Engine
* **Description**: Design an ephemeral in-memory cache timer to bypass locking recently validated packages within configurable grace periods (e.g. Immediately, 15 seconds, 1 minute, 5 minutes).
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟠 High
  * **Milestone**: `v0.9 Beta`
  * **Labels**: `feature`, `ux`, `backend`
  * **Complexity**: M (3-5 pts)
  * **Category**: Core Interception

#### 7. Intruder Visual Incident Center
* **Description**: Log failed PIN entry timestamps and target application properties. On successful login, check for logged incidents, trigger a warning badge, and display a timeline with distinct vector silhouettes.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟡 Medium
  * **Milestone**: `v1.0 Stable`
  * **Labels**: `feature`, `security`, `ui`
  * **Complexity**: L (8-13 pts)
  * **Category**: Security Shield

#### 8. Decoy PIN (Plausible Deniability)
* **Description**: Add secondary "Decoy PIN" configuration database entries. If input on lock overlays, successfully authenticate but render a mock crash dialog or restricted fake dashboard statistics view.
* **Metadata**:
  * **Status**: `📝 Planned`
  * **Priority**: 🟠 High
  * **Milestone**: `v1.0 Stable`
  * **Labels**: `feature`, `security`, `ux`
  * **Complexity**: L (8-13 pts)
  * **Category**: Security Shield

#### 9. Portable Backup & Restore Engine
* **Description**: Build a secure, local text-based backup parser. Compile configured application lists and hashed credentials into encrypted text strings that can be safely exported/imported locally.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟡 Medium
  * **Milestone**: `v1.0 Stable`
  * **Labels**: `feature`, `backend`, `security`
  * **Complexity**: M (3-5 pts)
  * **Category**: Security Shield

---

### ⚡ Section B: System & Platform Hardening

#### 10. Accessibility Service Reliability Refactoring
* **Description**: Optimize window event dispatching in `AppAccessibilityService`. Prevent main UI thread blocks and ensure rapid, stutter-free package interception on all major Android custom OEM skins.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🔴 Critical
  * **Milestone**: `v0.9 Beta`
  * **Labels**: `enhancement`, `performance`, `android`
  * **Complexity**: L (8-13 pts)
  * **Category**: Core Interception

#### 11. Battery Optimization Audits
* **Description**: Ensure Accessibility loops consume less than 1% battery over a 12-hour background cycle. Leverage CPU profiling tools and dynamic in-memory caching to eliminate redundant database queries.
* **Metadata**:
  * **Status**: `📝 Planned`
  * **Priority**: 🟠 High
  * **Milestone**: `v1.1 Improvements`
  * **Labels**: `performance`, `testing`, `android`
  * **Complexity**: M (3-5 pts)
  * **Category**: Performance

#### 12. App Launch Interceptor Performance Tuning
* **Description**: Implement thread-safe in-memory collections inside `AppLockManager` to load protected packages on start, bypassing disk access overhead during critical window-switching events.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟠 High
  * **Milestone**: `v0.9 Beta`
  * **Labels**: `performance`, `enhancement`, `backend`
  * **Complexity**: M (3-5 pts)
  * **Category**: Performance

#### 13. System Notification Customization
* **Description**: Design clean, low-profile notification channels to alert users on persistent active background service statuses or successful system security sweeps, conforming to Android 14 notification rules.
* **Metadata**:
  * **Status**: `📝 Planned`
  * **Priority**: 🟡 Medium
  * **Milestone**: `v1.1 Improvements`
  * **Labels**: `feature`, `ui`, `android`
  * **Complexity**: S (1-2 pts)
  * **Category**: User Experience

---

### 🎨 Section C: UI/UX & Design

#### 14. Material 3 UI Design Polish
* **Description**: Standardize custom color themes inside `Theme.kt` using a secure color scheme palette (Sage Green, Forest Accent). Implement responsive Material 3 layout components with beautiful fluid ripples.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟠 High
  * **Milestone**: `v0.9 Beta`
  * **Labels**: `enhancement`, `ui`, `ux`
  * **Complexity**: S (1-2 pts)
  * **Category**: User Experience

#### 15. Settings Redesign & Layout Structuring
* **Description**: Refactor settings into separate intuitive cards (Security Options, General Customization, Advanced Actions). Ensure touch target sizing meets accessibility guidelines (minimum 48dp).
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟡 Medium
  * **Milestone**: `v0.9 Beta`
  * **Labels**: `enhancement`, `ui`, `ux`
  * **Complexity**: S (1-2 pts)
  * **Category**: User Experience

---

### 🧪 Section D: Validation, Quality & QA

#### 16. Unit & Screenshot Automated Testing Suite
* **Description**: Implement JVM Robolectric and Roborazzi visual verification checks inside `app/src/test/`. Ensure overlay layouts, keypad items, and home routing states are robust to modifications.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟠 High
  * **Milestone**: `v1.0 Stable`
  * **Labels**: `testing`, `ci/cd`
  * **Complexity**: M (3-5 pts)
  * **Category**: Security Shield

#### 17. Target Compatibility Testing & Verification
* **Description**: Conduct manual and automated verification tests against Android 15 edge-to-edge constraints and Android 16 developer preview branches. Confirm overlay views display properly without visual overlap.
* **Metadata**:
  * **Status**: `📝 Planned`
  * **Priority**: 🟡 Medium
  * **Milestone**: `v1.1 Improvements`
  * **Labels**: `testing`, `android`
  * **Complexity**: M (3-5 pts)
  * **Category**: Security Shield

---

### 📖 Section E: Project Documentation

#### 18. Structured GitHub Wiki Setup
* **Description**: Formulate a complete, interconnected offline-first Markdown wiki documentation suite capturing installation rules, database schema flowcharts, lock overlay lifecycles, and security properties.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟠 High
  * **Milestone**: `v0.9 Beta`
  * **Labels**: `documentation`
  * **Complexity**: M (3-5 pts)
  * **Category**: Documentation

#### 19. README Enhancement
* **Description**: Overhaul the repository root `README.md` to introduce a high-contrast hero banner, clear feature outlines, detailed architecture hierarchy trees, and quick setup developer workflows.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟡 Medium
  * **Milestone**: `v0.9 Beta`
  * **Labels**: `documentation`, `good first issue`
  * **Complexity**: S (1-2 pts)
  * **Category**: Documentation

#### 20. Contributing Guide Setup (`CONTRIBUTING.md`)
* **Description**: Write code style requirements, issue filing instructions, branching protocols, pull request rules, and test verification standards to facilitate developer contribution.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟡 Medium
  * **Milestone**: `v0.9 Beta`
  * **Labels**: `documentation`, `good first issue`
  * **Complexity**: S (1-2 pts)
  * **Category**: Open Source Governance

#### 21. Pull Request Standardized Templates
* **Description**: Create `.github/PULL_REQUEST_TEMPLATE.md` with structured checklists covering issue references, changes list, testing evidence, code style compliance, and screenshots.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟢 Low
  * **Milestone**: `v0.9 Beta`
  * **Labels**: `documentation`, `open source`
  * **Complexity**: S (1-2 pts)
  * **Category**: Open Source Governance

#### 22. Issue Tracking Templates
* **Description**: Create Markdown config templates under `.github/ISSUE_TEMPLATE/` for Bug Reports, Feature Proposals, and Dependency Updates to pre-fill logs with system diagnostic tables.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟢 Low
  * **Milestone**: `v0.9 Beta`
  * **Labels**: `documentation`, `open source`
  * **Complexity**: S (1-2 pts)
  * **Category**: Open Source Governance

#### 23. Security Policy Statement (`SECURITY.md`)
* **Description**: Formulate a secure disclosure mechanism defining supported versions, coordinated response pathways, private communication handles, and patch dispatch speeds.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟠 High
  * **Milestone**: `v0.9 Beta`
  * **Labels**: `documentation`, `security`
  * **Complexity**: S (1-2 pts)
  * **Category**: Open Source Governance

---

### 📦 Section F: Delivery & Infrastructure

#### 24. GitHub Actions CI/CD Setup
* **Description**: Write a continuous integration workflow (`.github/workflows/main.yml`) that checks out code, configures JDK 17, caches libraries, runs linter checks, tests JVM models, and builds APK binaries.
* **Metadata**:
  * **Status**: `🚧 In Progress`
  * **Priority**: 🟠 High
  * **Milestone**: `v1.0 Stable`
  * **Labels**: `ci/cd`, `dependencies`
  * **Complexity**: M (3-5 pts)
  * **Category**: CI/CD & DevOps

#### 25. Play Store Release Strategy Planning
* **Description**: Draft Google Play Console listings, organize assets (app screenshots, vector icons), write store copy descriptions, and set up test environments inside Google Play's closed testing track.
* **Metadata**:
  * **Status**: `📝 Planned`
  * **Priority**: 🟡 Medium
  * **Milestone**: `v2.0 Major Release`
  * **Labels**: `ci/cd`, `ui`, `ux`
  * **Complexity**: M (3-5 pts)
  * **Category**: Distribution

#### 26. Version 1.0 Release Package Construction
* **Description**: Compile and verify release builds. Prepare changelogs, build notes, and sign binaries using secure, non-committed signing configurations for F-Droid and GitHub Releases.
* **Metadata**:
  * **Status**: `📝 Planned`
  * **Priority**: 🟠 High
  * **Milestone**: `v1.0 Stable`
  * **Labels**: `ci/cd`, `documentation`
  * **Complexity**: S (1-2 pts)
  * **Category**: Distribution

---

[[Home]] | [[Installation >>](Installation)]
