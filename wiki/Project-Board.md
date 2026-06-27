# GitHub Project v2 Blueprint: Privacy Lock App Roadmap

This document provides a comprehensive, enterprise-grade blueprint for configuring and managing the official **GitHub Project (Projects v2)** board for **Privacy Lock**—a professional, offline-first Android security application. It acts as the ultimate reference for product managers, release engineers, security leads, and open-source contributors, detailing workflows, custom fields, automation rules, standard labels, custom views, and a comprehensive backlog of **exactly 75 realistic, highly technical, and production-ready tasks**.

---

## 🎨 1. Industry Best Practices & Design Rationale

Implementing an enterprise-grade project management system on GitHub requires adopting practices used by top-tier engineering organizations such as **Google (AndroidX)**, **Microsoft**, **JetBrains**, and the **Linux Foundation**. The structure of this board is guided by the following principles:

* **Strict Status Hygiene**: Workflows are divided into highly discrete execution gates (e.g., separating `🧪 Testing` and `👀 Code Review` from active `🚧 In Progress` states) to prevent premature merging and ensure that only fully validated, compiled, and audited binaries are scheduled for deployment.
* **Granular Custom Fields**: By indexing issues along multiple metadata dimensions—including **Complexity (Story Points)**, **Platform Risk Profiles**, **Architectural Components**, and **Target Release Milestones**—maintainers can dynamically generate highly filtered, contextual views. This keeps large team workloads uncluttered.
* **Zero-Knowledge Security Posture**: Reflecting the core mission of Privacy Lock, all development planning treats security, offline privacy, cryptographic verification, and battery conservation not as passive "nice-to-have" enhancements, but as high-priority, trackable technical requirements with dedicated validation steps.

---

## 📋 2. Project Metadata & Configuration

* **Project Name**: `Privacy Lock App Roadmap`
* **Description**: *Track development, security, documentation, testing, releases, maintenance, dependency updates, and long-term planning for Privacy Lock.*
* **Default View Layout**: `Board (Kanban)`

---

## 🗂️ 3. Kanban Status Columns (The Lifecycle Pipeline)

The project lifecycle is enforced across nine sequential columns. Each column represents a distinct phase with clear entrance and exit criteria:

```
[ Backlog ] -> [ Planned ] -> [ Ready for Development ] -> [ In Progress ] -> [ Code Review ] -> [ Testing ] -> [ Ready for Release ] -> [ Done ] -> [ Released ]
```

| Status Column | Icon | Definition & Entrance Criteria | Exit Criteria |
| :--- | :---: | :--- | :--- |
| **📋 Backlog** | `📋` | Raw ideas, open community feature proposals, non-blocking enhancement requests, and low-priority bugs. | Triaged by a maintainer, assigned a priority, and moved to Planned. |
| **📝 Planned** | `📝` | Approved issues that align with the product roadmap. Technically scoped and assigned to a specific Milestone and Target Version. | Technical architecture is defined and requirements are clarified. |
| **🚀 Ready for Development** | `🚀` | High-priority backlog items that are fully specified, estimated, and ready for an engineer to assign to themselves. | Issue is assigned to a developer and active work begins. |
| **🚧 In Progress** | `🚧` | Features currently being coded, active bug fixes, or documentation updates under active draft. Branch is created. | Pull Request (PR) is opened and linked to the corresponding issue. |
| **👀 Code Review** | `👀` | Work is completed. Associated PR is open and undergoing peer review, architecture auditing, and static analysis. | PR obtains at least two approvals and passes all standard CI checks. |
| **🧪 Testing** | `🧪` | Changes are undergoing automated integration tests (Robolectric), screenshot validations (Roborazzi), and manual QA. | Verification tests pass on active target physical devices/emulators. |
| **⏳ Ready for Release** | `⏳` | Merged features waiting to be compiled into the next release candidate (RC) tag. Code is frozen on the main branch. | Release pipeline executes successfully and release candidate is signed. |
| **✅ Done** | `✅` | Work is fully complete, merged, and verified. Release candidate testing has passed. | Tag is compiled into a release version. |
| **🎉 Released** | `🎉` | Item is included in an official production-ready release distributed via F-Droid, GitHub Releases, or the Play Store. | Closed and archived. |

---

## ⚙️ 4. Custom Fields Specification

Custom fields enable precise querying, programmatic automation, and rich project insights. Configure these fields within your GitHub Project setup:

```yaml
fields:
  - name: "Priority"
    type: "Single select"
    options:
      - label: "🔴 Critical"
        color: "red"
        description: "Showstopping issues (e.g., security bypasses, data corruption, boot loops, or severe runtime crashes)."
      - label: "🟠 High"
        color: "orange"
        description: "Main feature requirements, critical cryptographic hardening, or high-severity bug fixes."
      - label: "🟡 Medium"
        color: "yellow"
        description: "Standard enhancements, UI polishes, secondary features, or optimization items."
      - label: "🟢 Low"
        color: "green"
        description: "Minor visual adjustments, non-urgent refactorings, or trivial documentation updates."

  - name: "Type"
    type: "Single select"
    options:
      - "Feature"
      - "Bug"
      - "Documentation"
      - "Security"
      - "Performance"
      - "Testing"
      - "Maintenance"
      - "Dependency"
      - "Refactor"
      - "UI"
      - "UX"

  - name: "Component"
    type: "Single select"
    options:
      - "Application Lock"
      - "Accessibility Service"
      - "Authentication"
      - "Biometrics"
      - "Database"
      - "UI"
      - "Settings"
      - "Notifications"
      - "Security"
      - "GitHub"
      - "CI/CD"
      - "Documentation"

  - name: "Complexity"
    type: "Single select"
    options:
      - label: "Small"
        color: "blue"
        description: "Trivial effort (1-2 story points: e.g., config changes, icon assets, simple string edits)."
      - label: "Medium"
        color: "cyan"
        description: "Standard effort (3-5 story points: e.g., a single Composable screen, simple Room entity additions)."
      - label: "Large"
        color: "purple"
        description: "Significant effort (8-13 story points: e.g., customized lock keypads, background work pipelines)."
      - label: "Epic"
        color: "magenta"
        description: "Massive effort (20+ story points: e.g., full biometric flows, cryptographic DB wrapping)."

  - name: "Version"
    type: "Single select"
    options:
      - "v1.0"
      - "v1.1"
      - "v1.2"
      - "v2.0"

  - name: "Risk"
    type: "Single select"
    options:
      - label: "Low"
        color: "green"
        description: "Minimal danger of regressions, build failures, or policy rejections."
      - label: "Medium"
        color: "yellow"
        description: "Standard risk. Affects local user states, database transitions, or basic UI structures."
      - label: "High"
        color: "orange"
        description: "High risk. Touches core background interception threads or cryptographic key derivation."
      - label: "Critical"
        color: "red"
        description: "Extreme risk. Direct impact on OS accessibility frameworks, platform sandboxing, or Google Play policies."
```

---

## 🏷️ 5. Standard Repository Labels

To align issue tracking with Project V2 views, configure the following labels inside your repository (`Settings -> Labels`):

| Label Name | Color | Category | Description |
| :--- | :---: | :--- | :--- |
| `bug` | `#d73a4a` | Core | Code, configuration, or layout regressions. |
| `feature` | `#a2eeef` | Core | New feature implementations or capabilities. |
| `security` | `#000000` | Security | Cryptography, biometric wrappers, or vulnerability patches. |
| `documentation` | `#0075ca` | Metadata | Wiki pages, markdown files, and inline code annotations. |
| `performance` | `#e9967a` | Quality | Memory leak patches, battery optimization, and DB caching. |
| `testing` | `#0e8a16` | Quality | JUnit, Robolectric unit tests, and Roborazzi screenshot tests. |
| `maintenance` | `#5319e7` | Core | Code cleanups, Gradle refactorings, and toolchain configurations. |
| `enhancement` | `#3c82f6` | Quality | Enhancing existing UI layouts, haptic responses, or UX transitions. |
| `ui` | `#fef2c0` | Design | Jetpack Compose screens, layouts, color themes, and grids. |
| `ux` | `#bfdadc` | Design | Interactive user flows, gesture intercepts, and dynamic behaviors. |
| `compose` | `#1d76db` | Platform | Jetpack Compose-specific runtime updates and compiler modifiers. |
| `kotlin` | `#9B51E0` | Platform | Kotlin language features, coroutines, and flow structures. |
| `android` | `#3DDC84` | Platform | Native Android SDK integrations, background services, and manifest rules. |
| `accessibility` | `#111111` | Platform | Accessibility Service loops, XML configurations, and system event maps. |
| `biometric` | `#50e3c2` | Platform | Fingerprint verification and hardware secure-key configurations. |
| `database` | `#f7ca18` | Storage | SQLite and Room persistence frameworks. |
| `room` | `#f39c12` | Storage | Room-specific KSP compilation, migrations, and DAO operations. |
| `datastore` | `#d35400` | Storage | Jetpack DataStore-encrypted user configurations and flag states. |
| `ci/cd` | `#cfd8dc` | DevOps | Continuous integration, GitHub workflows, and runner systems. |
| `github-actions` | `#2088FF` | DevOps | Automated workflows and runners configured inside `.github/`. |
| `dependabot` | `#4A90E2` | DevOps | Automating dependencies updates based on weekly checks. |
| `release` | `#27ae60` | DevOps | Binary packaging, release tags, and metadata formatting. |
| `good first issue` | `#7057ff` | Community | Straightforward tasks perfect for onboarding new contributors. |
| `help wanted` | `#008672` | Community | Complex design debates or highly technical bug investigations. |
| `high priority` | `#ff5722` | Priority | Items scheduled for immediate attention in the active sprint cycle. |
| `low priority` | `#8bc34a` | Priority | Items scheduled to be addressed after all core milestones are finalized. |

---

## 🎯 6. Target Release Milestones

Project milestones organize development into logical, sequential releases, matching the target timeline:

* **Milestone 1: `Version 1.0 Stable`**
  * *Focus*: Secure local 6-digit PIN lock overlays, accessibility service intercepts, randomized keypad configurations, sqlite encryption, local backups, and stable GitHub Actions CI pipelines.
* **Milestone 2: `Version 1.1`**
  * *Focus*: Biometric integration (`BiometricPrompt`), landscape orientation support, tablet layout optimizations, edge-to-edge system navigation, and battery/performance optimization checks.
* **Milestone 3: `Version 1.2`**
  * *Focus*: Intruder visual timelines, customizable stealth icon switchers, play store app bundle preparations, and localized translation files.
* **Milestone 4: `Version 2.0`**
  * *Focus*: High-risk AES-256 database key-wrapping backed by hardware StrongBox/Keystore, dynamic pattern-locking canvas overlays, and privacy-friendly analytics models.
* **Milestone 5: `Future Ideas`**
  * *Focus*: Encrypted cloud backup sync, custom lock screen wallpapers, and geo-fenced smart unlocking profiles.

---

## 🤖 7. Advanced Automation Rules & Workflow States

To minimize project administration overhead, enable and configure the following rules in the **Workflows** panel of your GitHub Project (v2):

### 7.1 Automated Status Mapping Rules

1. **Auto-Assign on Activity**:
   * **Trigger**: When an issue is assigned to a contributor.
   * **Action**: Update the card Status to `🚧 In Progress`.
2. **Pull Request Routing**:
   * **Trigger**: When a Pull Request referencing an issue is opened.
   * **Action**: Update the issue and PR card Status to `👀 Code Review`.
3. **Continuous Integration Gate**:
   * **Trigger**: When all required checks in the GitHub Actions build workflow pass.
   * **Action**: Automatically transition the card Status to `🧪 Testing`.
4. **Validation Progression**:
   * **Trigger**: When a QA engineer appends the `qa-verified` label.
   * **Action**: Update the card Status to `⏳ Ready for Release`.
5. **Continuous Deployment Gate**:
   * **Trigger**: When a Pull Request is successfully merged into the `main` branch.
   * **Action**: Transition the associated card Status to `✅ Done`.
6. **Tag & Release Progression**:
   * **Trigger**: When a new production-signed tag and release are published on GitHub.
   * **Action**: Transition all closed issues linked to that release tag to `🎉 Released`.

### 7.2 Programmatic Metadata Mapping

Configure GitHub Actions scripts (e.g., using `.github/workflows/project-automation.yml`) to automatically assign custom fields based on labels:

```
[ Label: "security" ] --------> Assign Field [ Risk: High ] & [ Component: Security ]
[ Label: "performance" ] -----> Assign Field [ Component: Performance ] & [ Complexity: Medium ]
[ Label: "bug" ] -------------> Assign Field [ Type: Bug ]
[ Label: "ci/cd" ] -----------> Assign Field [ Component: CI/CD ] & [ Risk: Low ]
```

---

## 🖥️ 8. High-Fidelity Custom Project Views

Organize developer dashboards using custom views. Click the **+ New View** tab in GitHub Projects to create these configured environments:

### 8.1 Core Views
* **Board (Kanban)**:
  * *Layout*: `Board`
  * *Grouped by*: `Status`
  * *Filters*: `no:archived`
  * *Purpose*: Main operational pipeline monitoring day-to-day progression.
* **Table (Master List)**:
  * *Layout*: `Table`
  * *Visible Fields*: `Title`, `Assignees`, `Status`, `Priority`, `Type`, `Component`, `Complexity`, `Version`
  * *Purpose*: Complete bulk-editing interface for triaging and scoping sessions.
* **Roadmap (Timeline)**:
  * *Layout*: `Roadmap`
  * *Grouped by*: `Version` (Using start/end dates from milestone metadata)
  * *Purpose*: Executive summary visualizing long-term development plans and launch targets.

### 8.2 Contextual Dashboards
* **Releases**:
  * *Layout*: `Table`
  * *Grouped by*: `Version`
  * *Filters*: `status:Done,Released`
  * *Purpose*: Keeps track of which features are bundled into specific build distributions.
* **High Priority**:
  * *Layout*: `Board`
  * *Grouped by*: `Status`
  * *Filters*: `Priority:"🔴 Critical","🟠 High"`
  * *Purpose*: War-room dashboard for addressing critical blockers, regressions, and security flaws.
* **Security**:
  * *Layout*: `Table`
  * *Filters*: `Type:Security OR Label:security`
  * *Sort*: `Risk descending`
  * *Purpose*: Audit board for cryptographic validations and permission adjustments.
* **Testing**:
  * *Layout*: `Board`
  * *Filters*: `Status:Testing OR Type:Testing`
  * *Purpose*: Dedicated board for testing engineers to inspect ready-to-test candidate code.
* **Documentation**:
  * *Layout*: `Table`
  * *Filters*: `Type:Documentation OR Label:documentation`
  * *Purpose*: Writing queue for wiki pages, manuals, README optimizations, and structural graphs.
* **Dependencies**:
  * *Layout*: `Table`
  * *Filters*: `Type:Dependency OR label:dependencies`
  * *Purpose*: Dedicated feed for tracking automated Dependabot PRs and library updates.
* **Performance**:
  * *Layout*: `Table`
  * *Filters*: `Type:Performance OR label:performance`
  * *Purpose*: Bottleneck review center (monitoring battery drain, startup times, and memory leaks).
* **My Work**:
  * *Layout*: `Board`
  * *Grouped by*: `Status`
  * *Filters*: `assignee:@me`
  * *Purpose*: Personalized board focusing on tasks assigned to the active user.
* **Recently Updated**:
  * *Layout*: `Table`
  * *Sort*: `Updated descending`
  * *Purpose*: Real-time feed highlighting where active discussion and modifications are happening.
* **Released Features**:
  * *Layout*: `Board`
  * *Filters*: `Status:Released`
  * *Purpose*: Read-only catalog displaying completed capabilities currently live in production.

---

## 🗂️ 9. The Master Backlog: 75 Realistic Project Items

Below is the complete, high-fidelity directory of **exactly 75 realistic, highly technical tasks** designed for the Privacy Lock ecosystem. Every item is categorized, fully specified, and populated with precise project metadata.

---

### 🛡️ Section 9.1: Cryptography & Security (Items 1-10)

#### 1. Implement Argon2id password hashing inside Key derivation workflow
* **Description**: Replace SHA-256 fallback mechanisms with secure on-device Argon2id password hashing to defend against physical offline dictionary attacks on extracted sqlite databases.
* **Metadata**: `Status: 📋 Backlog | Priority: 🟠 High | Type: Security | Component: Security | Complexity: Large | Version: v2.0 | Risk: High`

#### 2. Establish Hardware-backed Keystore Keyset wrapper
* **Description**: Derive AES-GCM-256 database protection keys wrapping them via the Android Keystore system. Fallback gracefully if StrongBox or TEE modules are physically missing.
* **Metadata**: `Status: 📝 Planned | Priority: 🔴 Critical | Type: Security | Component: Database | Complexity: Epic | Version: v1.0 | Risk: High`

#### 3. Secure Ephemeral RAM allocations using mutable CharArrays for PIN inputs
* **Description**: Ensure user passcodes are held in RAM using zeroable `CharArray` containers rather than immutable JVM `Strings`. Explicitly wipe buffers directly after database validation.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Security | Component: Authentication | Complexity: Medium | Version: v1.0 | Risk: Medium`

#### 4. Design randomized keypad click position variations
* **Description**: Introduce randomized, subtle pixel coordinate offsets to keypad button touch targets on the secure lock overlay. This prevents side-channel smudge analysis on high-resolution screens.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟡 Medium | Type: Security | Component: Security | Complexity: Medium | Version: v1.0 | Risk: Low`

#### 5. Verify local root detection execution blockages
* **Description**: Integrate root validation helpers (e.g., Libsu or RootBeer). Gracefully terminate core state memory caches or raise alerts if the runtime environment displays signs of compromise.
* **Metadata**: `Status: 📋 Backlog | Priority: 🟠 High | Type: Security | Component: Security | Complexity: Medium | Version: v2.0 | Risk: Medium`

#### 6. Audit secure memory storage keys for biometric fallback validation
* **Description**: Restrict biometric token lifespans using Android `BiometricPrompt` rules. Ensure a valid biometric pass only accesses temporary decryption keys for up to 60 seconds.
* **Metadata**: `Status: 📝 Planned | Priority: 🟠 High | Type: Security | Component: Biometrics | Complexity: Large | Version: v1.1 | Risk: High`

#### 7. Block memory profiling of decrypted database instances
* **Description**: Force memory allocation zeroing on close transactions inside the Room SQLCipher adapter database wrapper.
* **Metadata**: `Status: 📋 Backlog | Priority: 🟡 Medium | Type: Security | Component: Database | Complexity: Large | Version: v2.0 | Risk: Medium`

#### 8. Prevent system logs from printing localized package metadata
* **Description**: Audit ProGuard and Timber logging patterns. Ensure package IDs, user entry attempts, and error details are stripped from Logcat output in production release builds.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🔴 Critical | Type: Security | Component: Security | Complexity: Small | Version: v1.0 | Risk: High`

#### 9. Implement decoy application category groupings
* **Description**: Extend Decoy PIN behaviors. When a Decoy PIN is entered, dynamically filter the dashboard view to display a fake list of locked social utilities instead of actual locked bank apps.
* **Metadata**: `Status: 📝 Planned | Priority: 🟡 Medium | Type: Security | Component: Settings | Complexity: Medium | Version: v1.0 | Risk: Medium`

#### 10. Audit third-party dependencies for transit vulnerabilities
* **Description**: Scan all libraries declared in `libs.versions.toml` against the OWASP vulnerability directory to verify that no transient code includes unauthorized internet transits.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🔴 Critical | Type: Security | Component: CI/CD | Complexity: Small | Version: v1.0 | Risk: High`

---

### ⚡ Section 9.2: Performance, Memory, Startup & Battery Optimization (Items 11-20)

#### 11. Refactor Accessibility onAccessibilityEvent pipeline to eliminate main-thread lag
* **Description**: Offload all non-UI package verification processes and state check routines from the main thread. Run comparisons on dedicated background Coroutine dispatchers.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🔴 Critical | Type: Performance | Component: Accessibility Service | Complexity: Large | Version: v1.0 | Risk: High`

#### 12. Pre-compile Room database lock mappings inside memory cache
* **Description**: Bootstrap locked package paths into a synchronized `HashSet` on system start. Evaluate accessibility events against this in-memory cache to ensure sub-millisecond lookups.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Performance | Component: Database | Complexity: Medium | Version: v1.0 | Risk: Medium`

#### 13. Audit background battery consumption profile during active app switching
* **Description**: Ensure accessibility evaluation loops consume less than 1% battery over a standard 24-hour cycle. Profile CPU wakeups and trace wakes using Android energy profilers.
* **Metadata**: `Status: 📝 Planned | Priority: 🟠 High | Type: Performance | Component: Accessibility Service | Complexity: Medium | Version: v1.1 | Risk: High`

#### 14. Optimize startup initialization sequence
* **Description**: Implement Jetpack App Startup initializer cards to bootstrap dependencies asynchronously. This lowers launch times by over 200ms on older devices.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟡 Medium | Type: Performance | Component: UI | Complexity: Medium | Version: v1.0 | Risk: Low`

#### 15. Profile and patch memory leaks on LockActivity recreation events
* **Description**: Use LeakCanary in debug configurations to locate and resolve reference leaks that occur when `LockActivity` is repeatedly launched and torn down by the OS.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Performance | Component: UI | Complexity: Medium | Version: v1.0 | Risk: Medium`

#### 16. Implement Room database transaction batches for bulk configuration writes
* **Description**: Avoid disk-write bottlenecks. Group app locking and unlocking changes in settings into single, batched Room transactions to prevent frame drops in the UI list.
* **Metadata**: `Status: 📋 Backlog | Priority: 🟡 Medium | Type: Performance | Component: Database | Complexity: Small | Version: v1.1 | Risk: Low`

#### 17. Compress vector illustrations to decrease compiled APK footprints
* **Description**: Optimize decorative XML drawable paths and background illustrations. Reduce the final build size of the compiled release package.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟢 Low | Type: Performance | Component: UI | Complexity: Small | Version: v1.0 | Risk: Low`

#### 18. Constrain garbage collection churn inside the overlay keypad
* **Description**: Redesign Compose button modifiers on the secure keypad to reuse objects. This prevents layout recompositions from triggering frequent garbage collection pauses.
* **Metadata**: `Status: 📋 Backlog | Priority: 🟢 Low | Type: Performance | Component: UI | Complexity: Medium | Version: v1.1 | Risk: Low`

#### 19. Limit background monitoring overhead when the device is locked
* **Description**: Register receiver events for screen-off states to pause background evaluation loops. Resume background evaluations only when the user unlocks their screen.
* **Metadata**: `Status: 📋 Backlog | Priority: 🟠 High | Type: Performance | Component: Accessibility Service | Complexity: Medium | Version: v1.1 | Risk: Medium`

#### 20. Implement R8/ProGuard shrinking rules for release distributions
* **Description**: Configure granular ProGuard keep-rules in `proguard-rules.pro` to remove unused classes. Optimize and obfuscate compiled Kotlin binaries safely.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Performance | Component: CI/CD | Complexity: Medium | Version: v1.0 | Risk: Medium`

---

### ⚙️ Section 9.3: Accessibility Service & Core Interceptor (Items 21-27)

#### 21. Optimize package detection accuracy on custom OEM skins (One UI / MIUI)
* **Description**: Address app-launch detection bugs on highly customized Android interfaces (e.g., Samsung, Xiaomi, Oppo). Refactor window state transitions to handle customized layout configurations.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🔴 Critical | Type: Feature | Component: Accessibility Service | Complexity: Large | Version: v1.0 | Risk: High`

#### 22. Restructure accessibility service description strings in manifest
* **Description**: Review user-facing accessibility permission disclosures in `accessibility_service_config.xml`. Ensure clear explanations that comply with current Google Play Store disclosure guidelines.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Documentation | Component: Accessibility Service | Complexity: Small | Version: v1.0 | Risk: Low`

#### 23. Double-Back hardware button gesture blocker
* **Description**: Add back-button callback overrides in `LockActivity` to immediately route users to the launcher Home screen if they try to bypass the lock overlay using back gestures.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🔴 Critical | Type: Feature | Component: Application Lock | Complexity: Small | Version: v1.0 | Risk: Medium`

#### 24. Implement persistent service monitor notification
* **Description**: Create a low-priority, persistent foreground service notification to ensure the OS does not kill the Accessibility service when system resources are low.
* **Metadata**: `Status: 📝 Planned | Priority: 🟠 High | Type: Feature | Component: Notifications | Complexity: Small | Version: v1.1 | Risk: Medium`

#### 25. Design accessibility setup onboarding checklist wizard
* **Description**: Create a user onboarding screen that guides users through granting Accessibility, Usage Stats, and System Overlay permissions with clear step-by-step instructions.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: UI | Component: UI | Complexity: Medium | Version: v1.0 | Risk: Low`

#### 26. Track accessibility restart triggers during app updates
* **Description**: Implement a receiver to detect package updates and automatically re-initialize internal configurations, ensuring uninterrupted lock protection.
* **Metadata**: `Status: 📝 Planned | Priority: 🟡 Medium | Type: Feature | Component: Accessibility Service | Complexity: Small | Version: v1.1 | Risk: Medium`

#### 27. Build system overlay fallback locks
* **Description**: Implement fallback window protection using the standard System Alert Window permission. This ensures apps are blocked even if background accessibility services are temporarily stopped.
* **Metadata**: `Status: 📋 Backlog | Priority: 🟡 Medium | Type: Feature | Component: Application Lock | Complexity: Large | Version: v2.0 | Risk: High`

---

### 🔑 Section 9.4: Biometrics & Authentication (Items 28-34)

#### 28. Integrate Biometric API support using androidx.biometric helper
* **Description**: Build secure biometric verification flows into `LockActivity`. Allow users to authenticate using fingerprints or secure 3D face recognition instead of typing their PIN.
* **Metadata**: `Status: 📝 Planned | Priority: 🟠 High | Type: Feature | Component: Biometrics | Complexity: Large | Version: v1.1 | Risk: High`

#### 29. Implement custom failed attempts lockouts
* **Description**: Protect against brute-force attacks by implementing exponential wait delays (e.g., 30s, 5m, 15m) on the lock overlay after 5 consecutive failed PIN entry attempts.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Security | Component: Authentication | Complexity: Medium | Version: v1.0 | Risk: Medium`

#### 30. Build PIN configuration validation interface
* **Description**: Design and build setup screens that require users to double-enter and confirm their 6-digit passcode before saving it to the database.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🔴 Critical | Type: UI | Component: UI | Complexity: Small | Version: v1.0 | Risk: Low`

#### 31. Implement secure biometric-state modifications listener
* **Description**: Monitor the device for biometric changes (such as enrolling a new fingerprint) and automatically disable biometric fallback. This forces the user to enter their master PIN for security.
* **Metadata**: `Status: 📝 Planned | Priority: 🟠 High | Type: Security | Component: Biometrics | Complexity: Medium | Version: v1.1 | Risk: High`

#### 32. Build PIN visibility toggle behaviors
* **Description**: Add toggle settings that hide or mask the entered digits on the lock overlay keypad to protect against over-the-shoulder snooping.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟢 Low | Type: UX | Component: UI | Complexity: Small | Version: v1.0 | Risk: Low`

#### 33. Design fallback secondary passwords configurations
* **Description**: Allow users to set up a secondary, high-entropy alpha-numeric password that can be used to recover access if they forget their 6-digit PIN.
* **Metadata**: `Status: 📋 Backlog | Priority: 🟡 Medium | Type: Feature | Component: Authentication | Complexity: Medium | Version: v1.2 | Risk: Medium`

#### 34. Add granular haptic feedback presets to keypad taps
* **Description**: Integrate the Android haptic feedback system to provide tactile buzz responses on keypad inputs, following Material Design touch guidelines.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟢 Low | Type: UX | Component: UI | Complexity: Small | Version: v1.0 | Risk: Low`

---

### 💾 Section 9.5: Database & Persistence (Room/DataStore) (Items 35-41)

#### 35. Secure local database structures using SQLCipher encryptor
* **Description**: Secure the Room SQLite database using SQLCipher encryption. Derivate keys dynamically from the Android hardware Keystore to prevent offline reading of user settings.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🔴 Critical | Type: Security | Component: Database | Complexity: Epic | Version: v1.0 | Risk: High`

#### 36. Formulate clean Room migration script patterns for future versions
* **Description**: Write automated migration test cases to ensure seamless database transitions from the v1 schema to future additions without risking user configuration data.
* **Metadata**: `Status: 📝 Planned | Priority: 🟠 High | Type: Testing | Component: Database | Complexity: Medium | Version: v1.1 | Risk: High`

#### 37. Replace local SharedPreferences configurations with Jetpack DataStore
* **Description**: Migrate basic configuration settings (such as dark theme toggles or keypad shuffle flags) from SharedPreferences to type-safe, asynchronous Jetpack DataStore streams.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Refactor | Component: Database | Complexity: Medium | Version: v1.0 | Risk: Medium`

#### 38. Build timeline logs Room DAO database interfaces
* **Description**: Write the Room entity, DAO, and helper classes to persist local timeline logs. Return Kotlin Flow lists to power real-time security dashboards.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟡 Medium | Type: Feature | Component: Database | Complexity: Small | Version: v1.0 | Risk: Low`

#### 39. Optimize Database transactions for persistent audit logging
* **Description**: Implement transaction blocks and write-ahead logging (WAL) in Room. This prevents disk I/O bottlenecks during peak background security events.
* **Metadata**: `Status: 📋 Backlog | Priority: 🟢 Low | Type: Performance | Component: Database | Complexity: Medium | Version: v1.1 | Risk: Medium`

#### 40. Secure configuration import/export parser
* **Description**: Design an import/export mechanism that lets users backup their configurations. Backups are exported as semi-colon-separated strings with integrity checks to prevent tampering.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟡 Medium | Type: Feature | Component: Database | Complexity: Medium | Version: v1.0 | Risk: Medium`

#### 41. Design local configuration wiping triggers
* **Description**: Add a setting that completely wipes the local SQLite database, settings configurations, and encryption keys after a specific number of incorrect PIN entry attempts.
* **Metadata**: `Status: 📋 Backlog | Priority: 🟠 High | Type: Security | Component: Database | Complexity: Medium | Version: v1.2 | Risk: High`

---

### 🎨 Section 9.6: Material 3 UI, Theme, & UX (Compose, Tablet, Landscape, Dark Theme) (Items 42-49)

#### 42. Establish consistent Material 3 color themes and dynamic accents
* **Description**: Set up the Material 3 design system in the app. Use dynamic color palettes on supported devices and define clean primary and background color themes.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: UI | Component: UI | Complexity: Small | Version: v1.0 | Risk: Low`

#### 43. Optimize layout structures to support Landscape displays
* **Description**: Ensure the settings dashboard and lock overlay screens adjust elegantly to landscape orientation, preventing overlapping elements or cut-off views.
* **Metadata**: `Status: 📝 Planned | Priority: 🟡 Medium | Type: UI | Component: UI | Complexity: Medium | Version: v1.1 | Risk: Low`

#### 44. Optimize layouts for Large Screens (Tablets / Foldables)
* **Description**: Adapt settings layouts for tablets and foldables using Window Size Classes. Use multi-pane master-detail structures on wide screens to prevent wide, stretched-out layouts.
* **Metadata**: `Status: 📝 Planned | Priority: 🟡 Medium | Type: UI | Component: UI | Complexity: Medium | Version: v1.1 | Risk: Low`

#### 45. Implement Dark and Light themes across the interface
* **Description**: Support dynamic system dark/light theme switching. Ensure all custom colors, cards, and text styles in Compose meet target accessibility contrast requirements.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: UI | Component: UI | Complexity: Small | Version: v1.0 | Risk: Low`

#### 46. Ensure touch targets on all interactive elements meet accessibility guidelines
* **Description**: Review all buttons, switches, and list elements. Ensure they have a minimum touch target size of 48.dp to align with Android accessibility standards.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: UX | Component: UI | Complexity: Small | Version: v1.0 | Risk: Low`

#### 47. Design interactive launch animation transitions for settings cards
* **Description**: Use Compose animation transitions to add polish to the dashboard, providing elegant fade-in and slide-up card movements.
* **Metadata**: `Status: 📋 Backlog | Priority: 🟢 Low | Type: UX | Component: UI | Complexity: Small | Version: v1.2 | Risk: Low`

#### 48. Implement custom category filtering icons inside app lists
* **Description**: Add category filter headers (such as Social, Financial, and System) to the app selection screen, styled with distinct Material vector icons for fast navigation.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟡 Medium | Type: UI | Component: UI | Complexity: Small | Version: v1.0 | Risk: Low`

#### 49. Integrate full screen-edge Edge-to-Edge displays support
* **Description**: Use `enableEdgeToEdge()` to let screens render behind status and navigation bars. Configure appropriate window insets to keep the UI legible and modern.
* **Metadata**: `Status: 📝 Planned | Priority: 🟠 High | Type: UI | Component: UI | Complexity: Medium | Version: v1.1 | Risk: Low`

---

### 📸 Section 9.7: Screenshot, Masking & Intruder Detection (Items 50-55)

#### 50. Implement FLAG_SECURE window masking on sensitive screens
* **Description**: Add WindowManager flags to block screenshots and video recording on the settings and lock overlay screens. This prevents sensitive configurations or PIN inputs from being captured.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Security | Component: Security | Complexity: Small | Version: v1.0 | Risk: Medium`

#### 51. Hide thumbnail previews in the Recent Apps switcher
* **Description**: Ensure the lock overlay displays a secure, solid-colored blank screen inside the system Recent Apps list, preventing data leakage in multitasking views.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Security | Component: Security | Complexity: Small | Version: v1.0 | Risk: Medium`

#### 52. Implement on-device failed attempt tracking in the Intruder Center
* **Description**: Create a secure visual timeline that logs failed PIN entries. The logs capture timestamps, invalid entries, and associate each incident with a unique on-device decorative silhouette icon.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟡 Medium | Type: Feature | Component: Security | Complexity: Large | Version: v1.0 | Risk: Medium`

#### 53. Build custom notifications for intrusion attempt alerts
* **Description**: Display a secure, low-profile warning notification when the app is opened if new unauthorized access attempts were logged in the background.
* **Metadata**: `Status: 📝 Planned | Priority: 🟡 Medium | Type: Feature | Component: Notifications | Complexity: Small | Version: v1.2 | Risk: Low`

#### 54. Optimize screenshot-blocker notification triggers
* **Description**: Register system screenshot event listeners to display a secure warning toast when users attempt to take a screenshot of protected screens.
* **Metadata**: `Status: 📝 Planned | Priority: 🟢 Low | Type: UX | Component: Security | Complexity: Small | Version: v1.1 | Risk: Low`

#### 55. Design stealthy custom application icons launcher
* **Description**: Add a settings option that lets users choose from alternative launcher icons (such as Calculator or Weather), helping disguise the application.
* **Metadata**: `Status: 📝 Planned | Priority: 🟡 Medium | Type: Feature | Component: Settings | Complexity: Medium | Version: v1.2 | Risk: High`

---

### 🤖 Section 9.8: CI/CD, GitHub Actions, Dependabot & Release Automation (Items 56-62)

#### 56. Build the master GitHub Actions build-and-test workflow config
* **Description**: Write `.github/workflows/main.yml` to automatically checkout code, set up JDK 17, run linter checks, run unit tests, and compile debug and release binaries on every PR or merge.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: CI/CD | Component: CI/CD | Complexity: Medium | Version: v1.0 | Risk: Low`

#### 57. Configure Dependabot automated updates tracking
* **Description**: Write `.github/dependabot.yml` to run weekly scans on dependencies and propose library version upgrades to keep build systems secure.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Dependency | Component: GitHub | Complexity: Small | Version: v1.0 | Risk: Low`

#### 58. Automate package signing pipelines inside release runners
* **Description**: Configure GitHub Actions to sign release binaries securely using keys stored in repository secrets. This allows for automated deployment of production builds.
* **Metadata**: `Status: 📋 Backlog | Priority: 🟠 High | Type: CI/CD | Component: CI/CD | Complexity: Large | Version: v2.0 | Risk: High`

#### 59. Establish semantic versioning workflows using tag matchers
* **Description**: Set up automated semantic tagging rules in CI. Build release notes and prepare package upgrades automatically based on merge tags.
* **Metadata**: `Status: 📝 Planned | Priority: 🟡 Medium | Type: CI/CD | Component: CI/CD | Complexity: Medium | Version: v1.1 | Risk: Low`

#### 60. Clean up unused libraries inside compile blocks
* **Description**: Audit `build.gradle.kts` files to remove unused dependencies. This reduces compiled APK sizes and speeds up compilation times.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Maintenance | Component: CI/CD | Complexity: Small | Version: v1.0 | Risk: Low`

#### 61. Establish fast Gradle dependencies caching on GitHub Actions runners
* **Description**: Use standard action caching features to cache dependency catalogs and compiler intermediates, shortening CI runtimes.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟡 Medium | Type: CI/CD | Component: CI/CD | Complexity: Small | Version: v1.0 | Risk: Low`

#### 62. Configure fast F-Droid metadata updates
* **Description**: Organize app metadata, descriptions, and release images inside fast-lane directories to support automated cataloging on open-source F-Droid markets.
* **Metadata**: `Status: 📝 Planned | Priority: 🟡 Medium | Type: Documentation | Component: CI/CD | Complexity: Small | Version: v1.2 | Risk: Low`

---

### 📖 Section 9.9: Documentation, Wiki, Manuals & Open Source Governance (Items 63-70)

#### 63. Formulate the project wiki index Home page
* **Description**: Create and structure `/wiki/Home.md` to establish a central, easy-to-navigate landing page for the project's documentation.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Documentation | Component: Documentation | Complexity: Small | Version: v1.0 | Risk: Low`

#### 64. Document Accessibility Service architectural integration rules
* **Description**: Author `/wiki/Accessibility-Service.md` to document background execution states, permission requirements, and performance optimizations.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Documentation | Component: Documentation | Complexity: Small | Version: v1.0 | Risk: Low`

#### 65. Structure detailed database model descriptions
* **Description**: Write `/wiki/Database.md` to document local Room database tables, relations, encryption features, and backup structures.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Documentation | Component: Documentation | Complexity: Small | Version: v1.0 | Risk: Low`

#### 66. Write the secure security disclosure statement (SECURITY.md)
* **Description**: Author `/SECURITY.md` in the repository to define how security vulnerabilities should be privately reported and addressed.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Documentation | Component: GitHub | Complexity: Small | Version: v1.0 | Risk: Low`

#### 67. Create the contributing guide workflow guidelines (CONTRIBUTING.md)
* **Description**: Author `/CONTRIBUTING.md` to guide open-source contributors on code styles, PR policies, testing requirements, and branch structures.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟡 Medium | Type: Documentation | Component: GitHub | Complexity: Small | Version: v1.0 | Risk: Low`

#### 68. Author the Code of Conduct standards statement
* **Description**: Add `/CODE_OF_CONDUCT.md` to establish a safe, inclusive, and professional environment for open-source contributors.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟢 Low | Type: Documentation | Component: GitHub | Complexity: Small | Version: v1.0 | Risk: Low`

#### 69. Create standard Issue Templates for issue trackers
* **Description**: Write structured markdown configurations under `.github/ISSUE_TEMPLATE/` for bug reporting, feature proposals, and dependency updates.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟢 Low | Type: Documentation | Component: GitHub | Complexity: Small | Version: v1.0 | Risk: Low`

#### 70. Design standard Pull Request templates
* **Description**: Write `.github/PULL_REQUEST_TEMPLATE.md` to ensure code changes include clear summaries, test results, and compliance checklists.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟢 Low | Type: Documentation | Component: GitHub | Complexity: Small | Version: v1.0 | Risk: Low`

---

### 🧪 Section 9.10: Testing, QA, Android 15/16 Compatibility & Support (Items 71-75)

#### 71. Implement local JVM unit verification suites using Robolectric
* **Description**: Write unit tests in `app/src/test/` to verify local user authentication models, database operations, and settings configuration logic without requiring an emulator.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Testing | Component: Security | Complexity: Medium | Version: v1.0 | Risk: Low`

#### 72. Build JVM Layout validation screenshot checks using Roborazzi
* **Description**: Implement Roborazzi tests to capture and compare UI screenshots, ensuring layouts render correctly on compact and wide screens.
* **Metadata**: `Status: 🚧 In Progress | Priority: 🟠 High | Type: Testing | Component: UI | Complexity: Medium | Version: v1.0 | Risk: Low`

#### 73. Verify app operations against Android 15 (API 35) Edge-to-Edge display rules
* **Description**: Test on-device layout behaviors against Android 15 edge-to-edge constraints, verifying that screens remain readable without overlaps or cut-offs.
* **Metadata**: `Status: 📝 Planned | Priority: 🟡 Medium | Type: Testing | Component: Accessibility Service | Complexity: Medium | Version: v1.1 | Risk: Low`

#### 74. Verify and test app behavior on Android 16 (API 36) developer previews
* **Description**: Run compatibility checks on Android 16 developer builds to catch and address potential changes to system background services early.
* **Metadata**: `Status: 📋 Backlog | Priority: 🟡 Medium | Type: Testing | Component: Accessibility Service | Complexity: Medium | Version: v1.2 | Risk: Medium`

#### 75. Build localized string translations for multilanguage support
* **Description**: Set up multi-language configuration files (such as Spanish and German) to make security settings accessible to a broader user base.
* **Metadata**: `Status: 📝 Planned | Priority: 🟡 Medium | Type: Documentation | Component: UI | Complexity: Medium | Version: v1.2 | Risk: Low`

---

## 📅 10. Release Milestones & Roadmap Timeline

This section maps development over time, grouping tasks into logical milestones toward production:

```
[ Phase 1: v1.0 Stable Base ] ---> [ Phase 2: v1.1 Enhancements ] ---> [ Phase 3: v1.2 Polish ] ---> [ Phase 4: v2.0 Enterprise ]
```

### 📦 Phase 1: `Version 1.0 Stable`
* **Timeline**: 4 Sprints (8 Weeks)
* **Milestone Objectives**: Secure the core application locking pipeline, implement stable background accessibility intercepts, ensure robust secure PIN inputs, and establish GitHub Actions CI checks.
* **Core Tasks Included**: 
  - *Secure Ephemeral RAM allocations using mutable CharArrays* (Item 3)
  - *Design randomized keypad click position variations* (Item 4)
  - *Refactor Accessibility pipeline to eliminate main-thread lag* (Item 11)
  - *Pre-compile Room database lock mappings inside memory cache* (Item 12)
  - *Secure local database structures using SQLCipher encryptor* (Item 35)
  - *Build the master GitHub Actions build-and-test workflow config* (Item 56)

### 📦 Phase 2: `Version 1.1`
* **Timeline**: 3 Sprints (6 Weeks)
* **Milestone Objectives**: Add seamless biometric authentication fallbacks, optimize UI layouts for tablets and landscape orientations, and perform deep battery consumption profiles.
* **Core Tasks Included**:
  - *Audit secure memory storage keys for biometric fallback validation* (Item 6)
  - *Audit background battery consumption profile* (Item 13)
  - *Integrate Biometric API support using androidx.biometric helper* (Item 28)
  - *Optimize layout structures to support Landscape displays* (Item 43)
  - *Optimize layouts for Large Screens (Tablets / Foldables)* (Item 44)
  - *Integrate full screen-edge Edge-to-Edge displays support* (Item 49)

### 📦 Phase 3: `Version 1.2`
* **Timeline**: 3 Sprints (6 Weeks)
* **Milestone Objectives**: Implement the on-device failed attempt tracking timeline, provide customizable stealth launcher icon sets, and compile localized string translations.
* **Core Tasks Included**:
  - *Design fallback secondary passwords configurations* (Item 33)
  - *Build custom notifications for intrusion attempt alerts* (Item 53)
  - *Design stealthy custom application icons launcher* (Item 55)
  - *Configure fast F-Droid metadata updates* (Item 62)
  - *Verify and test app behavior on Android 16 previews* (Item 74)
  - *Build localized string translations for multilanguage support* (Item 75)

### 📦 Phase 4: `Version 2.0`
* **Timeline**: 5 Sprints (10 Weeks)
* **Milestone Objectives**: Leverage Hardware StrongBox for database encryption keys, enforce root verification system checks, and support fully automated release signatures.
* **Core Tasks Included**:
  - *Implement Argon2id password hashing* (Item 1)
  - *Verify local root detection execution blockages* (Item 5)
  - *Block memory profiling of decrypted database instances* (Item 7)
  - *Build system overlay fallback locks* (Item 27)
  - *Automate package signing pipelines inside release runners* (Item 58)

---

## 🛡️ 11. Project Architecture & Extensibility

Developers can easily extend or contribute to the project by following the design patterns of our core interfaces:

### 11.1 The Security Authenticator Interface
Any new verification method (such as custom pattern locks or remote unlock requests) must implement the standard `Authenticator` interface:

```kotlin
interface Authenticator {
    /**
     * Verifies the user credentials asynchronously.
     * @param input Ephemeral character array input directly from UI buffers.
     * @return Flow emitting dynamic verification states.
     */
    fun verifyCredentials(input: CharArray): Flow<AuthResult>
}
```

### 11.2 The Event Interceptor Contract
To build alternative launch blocking triggers (for example, geofence-based locks or scheduling rules):

```kotlin
interface Interceptor {
    /**
     * Determines whether target package should be locked.
     * @param packageName The bundle identifier of the launching application.
     * @return True if the package must be intercepted and locked.
     */
    fun shouldIntercept(packageName: String): Boolean
}
```

---

[[Home]] | [[Installation >>](Installation)]
