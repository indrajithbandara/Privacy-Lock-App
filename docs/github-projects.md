# Privacy Lock GitHub Projects & Project Board Specification

This document provides the complete, production-grade specification for the **Privacy Lock GitHub Project (Projects v2)**. It details the project statuses, custom fields, milestones, labels, views, and a comprehensive backlog of **75+ realistic project board items** mapped to core system modules.

---

## 🏗️ 1. Project Board Configuration

### Status Columns
Our project board utilizes a high-visibility, 9-column agile workflow to track items from initial triaging to official deployment:

1.  **📋 Backlog**: Unscheduled ideas, user requests, and non-blocking minor tasks.
2.  **📝 Planned**: Approved features and tasks scheduled for current or upcoming milestones.
3.  **🚀 Ready**: Fully specified tickets with technical requirements, ready for engineering.
4.  **🚧 In Progress**: Active development tasks currently assigned to developers.
5.  **👀 Code Review**: Completed code under review (mandatory two approvals).
6.  **🧪 Testing**: Code merged into dev/staging and currently undergoing QA or local unit testing.
7.  **⏳ Ready for Release**: Fully tested and verified features awaiting integration into the next release tag.
8.  **✅ Done**: Completed tasks that are merged into the `main` branch.
9.  **🎉 Released**: Live features deployed in signed production APKs/AABs.

### Custom Fields
*   **Priority (Single Select)**: `🔴 Critical` | `🟠 High` | `🟡 Medium` | `🟢 Low`
*   **Risk (Single Select)**: `🔴 Critical` | `🟠 High` | `🟡 Medium` | `🟢 Low`
*   **Component (Single Select)**: `Core Interceptor` | `Database (Room)` | `Biometrics` | `Settings & UI` | `CI/CD & DevOps` | `Testing` | `Security & Hashing` | `Documentation`
*   **Complexity (Single Select)**: `XS` (Tiny) | `S` (Small) | `M` (Medium) | `L` (Large) | `XL` (Epic)
*   **Milestones (Relations)**:
    *   `v1.0.0` - Secure App Intercept & Lock Core (MVP)
    *   `v1.1.0` - Security Hardening & Encryption Upgrade
    *   `v1.2.0` - Customization, Themes & UX Polish
    *   `v2.0.0` - Secure Local Sync & Advanced Lock Actions

---

## 🏷️ 2. Labeling Taxonomy

Our repository enforces a structured, color-coded labeling system to classify issues and pull requests:

| Label | Color | Description |
| :--- | :--- | :--- |
| `security` | `#d93f0b` | Cryptographic, privacy, or system vulnerability issues. |
| `bug` | `#e11d21` | Software bugs or unexpected failures in production. |
| `feature` | `#0e8a16` | Implementation of new functional requirements. |
| `enhancement` | `#84b6eb` | Incremental improvement or visual polish to an existing feature. |
| `documentation` | `#0052cc` | Additions or corrections to the README, docs, or Wiki. |
| `testing` | `#fbca04` | Robolectric, Roborazzi screenshot, or manual test case additions. |
| `ci-cd` | `#5319e7` | Modifications to GitHub Actions workflows, cache settings, or release scripts. |
| `performance` | `#cc317c` | CPU, RAM, or database transaction optimization tasks. |
| `dependencies` | `#1d76db` | Dependabot or manual dependency upgrade tickets. |

---

## 📋 3. Task Backlog (75+ Project Board Items)

Below is the complete backlog of **76 realistic tickets**, categorized by core components, with statuses, priorities, complexities, and detailed technical descriptions:

### 🛡️ Group A: Core Interceptor & Background Lock Engine (12 Items)

| ID | Title | Status | Priority | Complexity | Milestone | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TSK-001** | Intercept TYPE_WINDOW_STATE_CHANGED Events | Done | 🔴 Critical | M | v1.0.0 | Configure `AppAccessibilityService` to capture package launches. |
| **TSK-002** | Optimize Package Change Filtering | Done | 🟠 High | S | v1.0.0 | Ensure background packages do not trigger unsolicited intercept evaluations. |
| **TSK-003** | Resolve Launcher Screen Double-Back Bug | Done | 🟠 High | M | v1.0.0 | Prevent users from bypassing the lock overlay via rapid back presses. |
| **TSK-004** | Programmatic FLAG_SECURE Overlay Bindings | Done | 🔴 Critical | S | v1.0.0 | Bind `FLAG_SECURE` to `LockActivity` to block screenshots and screen recorders. |
| **TSK-005** | Implement Thread-Safe AppLockManager Cache | Done | 🔴 Critical | M | v1.0.0 | Create a thread-safe local cache to prevent database read lag on launch. |
| **TSK-006** | Handle Task Reparenting for SingleTask Apps | Planned | 🟠 High | L | v1.1.0 | Correctly handle singleTask applications that attempt to bypass overlays. |
| **TSK-007** | Graceful Lock Recovery After Device Crash | Planned | 🟡 Medium | M | v1.1.0 | Restore AppLockManager state automatically if the system kills the app. |
| **TSK-008** | Implement Delayed Auto-Lock Action | Planned | 🟡 Medium | S | v1.2.0 | Allow a 30-second window before locking a recently closed application. |
| **TSK-009** | Detect and Block Multi-Window Split Screen | Ready | 🟠 High | M | v1.1.0 | Prevent locked apps from being viewed by dragging them into split-screen. |
| **TSK-010** | Stop Intercepting Launcher Home Package | Done | 🟢 Low | S | v1.0.0 | Ensure the system launcher is never locked to avoid device lockups. |
| **TSK-011** | Optimizing Accessibility Performance Overhead | Testing | 🟡 Medium | S | v1.1.0 | Profile accessibility service processing times on lower-end devices. |
| **TSK-012** | Implement Emergency Exit Desktop Redirection | Ready | 🟠 High | M | v1.1.0 | Instantly redirect user to the home screen when lock is canceled. |

---

### 💾 Group B: Room Database & Local Schemas (10 Items)

| ID | Title | Status | Priority | Complexity | Milestone | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TSK-013** | Design LockedApp Room Entity & Dao | Done | 🔴 Critical | S | v1.0.0 | Create database schemas and primary keys for package locking rules. |
| **TSK-014** | Design SecurityConfig Schema Table | Done | 🔴 Critical | S | v1.0.0 | Create settings table storing hashes, timeouts, and customized preferences. |
| **TSK-015** | Design TimelineEvent Logging Schema | Done | 🟡 Medium | S | v1.0.0 | Create history logs table for failed attempts and status toggles. |
| **TSK-016** | Create IntruderSelfie Entity Schema | Done | 🟡 Medium | S | v1.0.0 | Create database records for intruder snoop tracking. |
| **TSK-017** | Implement Safe Room Database Migration v1->v2 | Planned | 🟠 High | M | v1.1.0 | Write migration scripts supporting newly added decoy configurations. |
| **TSK-018** | Force Local Encrypted SQLite Database | Ready | 🔴 Critical | L | v1.1.0 | Add Room SQLCipher bindings to encrypt database files at rest. |
| **TSK-019** | Optimize Multi-Threading Transaction Pools | Testing | 🟡 Medium | M | v1.1.0 | Configure Custom Coroutines Dispatcher to execute DB updates. |
| **TSK-020** | Prevent SQLite Database Lock Contention | Ready | 🟡 Medium | M | v1.2.0 | Upgrade database transactions to WAL (Write-Ahead Logging) mode. |
| **TSK-021** | Validate Integrity of Loaded Room Instances | Ready | 🔴 Critical | M | v1.1.0 | Gracefully handle database corruptions by wiping and re-initializing. |
| **TSK-022** | Clean Up Timeline Logs on Max Limit | Planned | 🟢 Low | S | v1.2.0 | Automatically prune logs older than 30 days to save user disk space. |

---

### 🔑 Group C: Biometric & Multi-Credential Systems (11 Items)

| ID | Title | Status | Priority | Complexity | Milestone | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TSK-023** | Integrate Android BiometricPrompt Library | Done | 🔴 Critical | M | v1.0.0 | Implement fingerprint authentication fallback in lock screens. |
| **TSK-024** | Check for Hardware Biometric Availability | Done | 🟠 High | S | v1.0.0 | Query system sensor managers to verify face/fingerprint support. |
| **TSK-025** | Encrypt Master PIN with PBKDF2 Hashing | Planned | 🔴 Critical | M | v1.1.0 | Upgrade PIN hashing from SHA-256 to PBKDF2 with unique salts. |
| **TSK-026** | Implement Decoy PIN Alternative Vault | Done | 🔴 Critical | L | v1.0.0 | Create alternative PIN code displaying dummy app lock lists. |
| **TSK-027** | Implement Panic PIN Instant Desktop Kill | Done | 🟠 High | M | v1.0.0 | Create a Panic PIN that redirects to the home screen and locks all apps. |
| **TSK-028** | Implement Security Lockout Delay Timer | Planned | 🟠 High | M | v1.1.0 | Block keypad inputs for 30 seconds after 5 consecutive failures. |
| **TSK-029** | Support Custom Passcode Lengths (4-12 digits) | Planned | 🟡 Medium | M | v1.2.0 | Allow users to select PIN lengths between 4 and 12 digits. |
| **TSK-030** | Hardware-Backed Keystore Key Generation | Ready | 🔴 Critical | L | v1.1.0 | Generate and store database encryption keys in Android Keystore. |
| **TSK-031** | Handle Biometric Verification Cancellations | Done | 🟢 Low | S | v1.0.0 | Fallback gracefully to PIN keypad when biometric prompts are dismissed. |
| **TSK-032** | Support StrongBox Key Storage Integration | Ready | 🟠 High | L | v2.0.0 | Store credentials in hardware-isolated StrongBox on Pixel/modern chips. |
| **TSK-033** | Implement Fingerprint Sensor Smudge Mitigation | Planned | 🟢 Low | S | v1.2.0 | Shove alert reminders to wipe sensors if authentication fails repeatedly. |

---

### 🎨 Group D: Jetpack Compose User Interface & Theme (11 Items)

| ID | Title | Status | Priority | Complexity | Milestone | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TSK-034** | Style Native Material 3 Sage Green Palette | Done | 🟠 High | S | v1.0.0 | Define primary, secondary, and background colors in `Theme.kt`. |
| **TSK-035** | Responsive App Keypad Grid Layout | Done | 🔴 Critical | M | v1.0.0 | Code standard 1–9 keypad layout with custom centered zero row. |
| **TSK-036** | Implement Randomized Layout Shuffling | Done | 🟠 High | M | v1.0.0 | Shuffle keypad digit positions dynamically to prevent smudge matching. |
| **TSK-037** | Visual Log Timeline Component | Done | 🟡 Medium | S | v1.0.0 | Build a scrollable, high-density logging card list with status icons. |
| **TSK-038** | Material 3 Adaptive Bottom Navigation Rail | Planned | 🟡 Medium | M | v1.2.0 | Switch navigation to a side-rail layout when running on tablets. |
| **TSK-039** | Dark Theme System Configuration Toggles | Done | 🟡 Medium | S | v1.0.0 | Add System, Dark, and Light manual preferences under Settings. |
| **TSK-040** | Smooth View Slide-In Screen Transitions | Done | 🟢 Low | S | v1.0.0 | Design sliding and fading transitions for main application screens. |
| **TSK-041** | Custom Avatar Generators for Intruder Logs | Done | 🟢 Low | S | v1.0.0 | Programmatically generate geometric avatar patterns for log events. |
| **TSK-042** | Custom Pattern Lock Drawing Canvas | Planned | 🟠 High | L | v2.0.0 | Develop a secure, gesture-based pattern draw board in Jetpack Compose. |
| **TSK-043** | Add Touch Feedback Haptic Vibrations | Done | 🟢 Low | S | v1.0.0 | Emit key-press vibrator pulses using standard system haptics. |
| **TSK-044** | Support Full Accessibility TalkBack Labels | Testing | 🟡 Medium | S | v1.1.0 | Audit and assign explicit contentDescriptions to all icons and views. |

---

### 🧪 Group E: Testing & QA Automation (10 Items)

| ID | Title | Status | Priority | Complexity | Milestone | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TSK-045** | Write Room Database DAO Unit Tests | Done | 🔴 Critical | S | v1.0.0 | Code localized JUnit database tests using in-memory Room helpers. |
| **TSK-046** | Setup Robolectric Local JVM Harness | Done | 🟠 High | M | v1.0.0 | Configure build dependencies to support headless Android environment. |
| **TSK-047** | Configure Roborazzi Visual Regression Setup | Done | 🟠 High | M | v1.0.0 | Establish screenshot testing libraries inside build targets. |
| **TSK-048** | Write Critical User Journey Robolectric Test | Done | 🟠 High | M | v1.0.0 | Automate end-to-end simulated lock and unlock testing sequence. |
| **TSK-049** | Write Keypad Randomization Layout Test | Done | 🟡 Medium | S | v1.0.0 | Assert that keypad digits shuffle position while index zero remains stable. |
| **TSK-050** | Write Password Hashing Algorithm Tests | Done | 🟡 Medium | S | v1.0.0 | Test input string validation, hashing, salting, and matching logic. |
| **TSK-051** | Implement LeakCanary Local Heap Monitoring | Ready | 🟢 Low | M | v1.1.0 | Monitor background service task cycles for memory leakage on device. |
| **TSK-052** | Run Accessibility Lint Rule Audits | Testing | 🟡 Medium | S | v1.1.0 | Automate accessibility scanner checks on Compose layout nodes. |
| **TSK-053** | Stress-Test Real-Time Package Syncing | Planned | 🟢 Low | M | v1.2.0 | Assert syncing performance handles devices with 500+ installed apps. |
| **TSK-054** | Mock System Biometric Authentication | Ready | 🟡 Medium | S | v1.0.0 | Write mock drivers verifying ViewModel behavior during snoop attempts. |

---

### ⚙️ Group F: DevOps, CI/CD & Repository Quality (10 Items)

| ID | Title | Status | Priority | Complexity | Milestone | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TSK-055** | Automated GitHub Actions CI Pipeline Setup | Done | 🔴 Critical | M | v1.0.0 | Configure `.github/workflows/main.yml` with setup-java and caching. |
| **TSK-056** | Secure release package signing from secrets | Done | 🔴 Critical | M | v1.0.0 | Map secret keystores into build processes during deployment tags. |
| **TSK-057** | Configure Dependabot for Gradle and Actions | Done | 🟡 Medium | S | v1.0.0 | Set weekly update rules and grouping in `dependabot.yml`. |
| **TSK-058** | Create YAML Bug & Feature Templates | Done | 🟢 Low | S | v1.0.0 | Code structured, easy-to-use input fields for reporting issues. |
| **TSK-059** | Implement CodeQL Security Analysis | Done | 🔴 Critical | S | v1.1.0 | Integrate automated static code vulnerability scanning in CI/CD. |
| **TSK-060** | Configure Pull Request Template Guidance | Done | 🟢 Low | S | v1.0.0 | Require developers to fill out testing and resolution checklists. |
| **TSK-061** | Configure EditorConfig Layout Rules | Done | 🟢 Low | S | v1.0.0 | Add `.editorconfig` aligning kotlin/XML indentation across environments. |
| **TSK-062** | Setup GitAttributes Line Normalization | Done | 🟢 Low | S | v1.0.0 | Enforce LF line-endings and isolate binaries from text parsing. |
| **TSK-063** | Automate SHA-256 Checksum Generations | Done | 🟡 Medium | S | v1.0.0 | Compute checksums during build and output them as release assets. |
| **TSK-064** | Add Automated Dependency Review Pipeline | Done | 🔴 Critical | S | v1.1.0 | Block pull requests that introduce vulnerable or non-compliant libraries. |

---

### 📘 Group G: Enterprise Documentation & Guides (12 Items)

| ID | Title | Status | Priority | Complexity | Milestone | Description |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TSK-065** | Create World-Class README Hub | Done | 🟡 Medium | S | v1.0.0 | Write visual showcases, stacks, architecture, and licensing terms. |
| **TSK-066** | Create Complete GitHub Wiki Pages | Done | 🟡 Medium | M | v1.0.0 | Code 31 linked pages housing developer guides and admin parameters. |
| **TSK-067** | Write System Architectural Design Manual | Done | 🟠 High | S | v1.0.0 | Document MVVM flowcharts, db schemas, and accessibility lifecycles. |
| **TSK-068** | Write Extensive Developer Guide | Done | 🟡 Medium | S | v1.0.0 | Document directories, build tasks, and style patterns. |
| **TSK-069** | Write Detailed End-User Manual | Done | 🟡 Medium | S | v1.0.0 | Document setup wizards, settings toggles, and dual credentials. |
| **TSK-070** | Write Repository Administration Manual | Done | 🟡 Medium | S | v1.0.0 | Document branching policies, SemVer release tagging, and triage rules. |
| **TSK-071** | Write Core Security Threats Whitepaper | Done | 🟠 High | S | v1.0.0 | Outline sandboxing capabilities, keystores, and anti-tamper mechanics. |
| **TSK-072** | Write Detailed Troubleshooting Diagnostics | Done | 🟢 Low | S | v1.0.0 | List common OEM power constraints and solutions. |
| **TSK-073** | Write Application Testing Guideline | Done | 🟢 Low | S | v1.0.0 | Document JVM environments, screenshot setups, and coverage rules. |
| **TSK-074** | Compile Project FAQ Directory | Done | 🟢 Low | S | v1.0.0 | Provide detailed answers to 30 common user and developer questions. |
| **TSK-075** | Draft Development Version Roadmap | Done | 🟢 Low | S | v1.0.0 | Outline phased enhancements and scheduled upgrades. |
| **TSK-076** | Write Known Limitations and Future Plans | Done | 🟠 High | S | v1.0.0 | Outline Direct Boot issues and plans for hardware StrongBox integration. |

---

## 👁️ 4. GitHub Project Views Spec

For maximum productivity, project administrators configure five distinct tabs on the GitHub Project interface:

1.  **📊 Kanban Board**: Grouped by **Status**, with cards displaying Priority, Complexity, and Component tags.
2.  **🗺️ Gantt Roadmap**: Grouped by **Milestone**, showing start and target dates for high-impact tickets (Complexity L/XL).
3.  **🐛 Bug Triage**: A list filtered to show only open cards with the label `bug` or `security`, sorted by Priority.
4.  **⚡ Sprint Planning**: Grouped by Complexity, displaying only cards marked **Planned** or **Ready** for the current milestone.
5.  **📜 Complete Backlog**: A high-density spreadsheet listing all issues, sorted by Component and ID, to track overall progress.
