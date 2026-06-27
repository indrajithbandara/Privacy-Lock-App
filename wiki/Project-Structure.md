# Project Structure

This directory blueprint map provides a comprehensive walkthrough of the Privacy Lock codebase and package organization.

---

## Workspace Root Directory Layout

```
.
├── .github/                   # GitHub action workflows and pull request templates
├── app/                       # Main Android application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt        # Primary application entry point
│   │   │   │   ├── LockActivity.kt        # Security overlay entry point
│   │   │   │   ├── data/                  # Local SQLite and DataStore definitions
│   │   │   │   ├── security/              # Accessibility service and package managers
│   │   │   │   └── ui/                    # Jetpack Compose visual presentation components
│   │   │   └── res/                       # App resources (drawables, layouts, strings)
│   │   └── test/                          # Unit & JVM integration test suites (Robolectric)
│   └── build.gradle.kts       # Main module build configuration script
├── gradle/                    # Central Gradle configurations
│   └── libs.versions.toml     # Shared dependencies catalog
├── build.gradle.kts           # Root build gradle script
├── metadata.json              # AI Studio Platform definition file
└── settings.gradle.kts        # Core project dependencies configuration
```

---

## Detailed Package Tour

### 1. Root Application Package (`com.example`)
* **`MainActivity.kt`**: Coordinates onboarding, navigation paths, dynamic application routing, and system-level event hooks (such as screenshot shield changes).
* **`LockActivity.kt`**: Coordinates the custom keypad overlays. It initializes `FLAG_SECURE` window shields and handles biometrics or numeric credential validation.

### 2. Data Persistence Package (`com.example.data`)
Contains all the files responsible for structured local state storage:
* **`Entities.kt`**: Database object mappings.
  * `LockedApp`: Stores Package details, categorization, and locked status.
  * `SecurityConfig`: Stores global settings like PIN hashes, decoy configuration, auto-lock timeouts, and randomized keypad settings.
  * `TimelineEvent`: Tracks historic security changes and authorization occurrences.
  * `IntruderSelfie`: Tracks intrusion details (such as failed attempts and generated character avatars).
* **`DAOs.kt`**: Contains database query definitions (`LockedAppDao`, `SecurityConfigDao`, `TimelineEventDao`, `IntruderSelfieDao`) utilizing Kotlin flow emission.
* **`AppDatabase.kt`**: Standard SQLite Room database abstraction.
* **`PrivacyRepository.kt`**: Unified gateway class managing Room query executions and DataStore operations.

### 3. Security Engine Package (`com.example.security`)
Handles OS-level transitions, service states, and lock operations:
* **`AppAccessibilityService.kt`**: An Android `AccessibilityService` that monitors window transformations and triggers `LockActivity` for unauthenticated, protected apps.
* **`AppLockManager.kt`**: In-memory state machine tracking active locks, temporary session clearances, and global auto-lock timelines.

### 4. User Interface Package (`com.example.ui`)
Encapsulates ViewModels, screens, helper widgets, and design systems:
* **`PrivacyViewModel.kt`**: The single source of truth for UI state. Connects directly to the repository and updates data flows.
* **`components/CommonComponents.kt`**: Declares reusable Material 3 widgets, including the standardized adaptive `SecureKeypad`, customized buttons, and timeline timeline items.
* **`screens/`**:
  * `DashboardScreen.kt`: The landing page representing dynamic applications search, app-lock toggles, and Category tab cards.
  * `IntruderSelfieScreen.kt`: Renders historical unauthorized entry attempts and failed locks with visual avatars.
  * `PrivacyCenterScreen.kt`: Comprehensive audit portal presenting security recommendation cards, category health grades, and fast resolution settings.
  * `SettingsScreen.kt`: The administrative control panel where configurations (e.g., Random Keypad, Screenshot Protection toggle, and Auto Lock intervals) can be mutated.
* **`theme/`**: Declares visual presets (`Color.kt`, `Theme.kt`, `Type.kt`) adhering to Material Design 3 guidelines.

---

[[Home]] | [<< Architecture](Architecture) | [[App Lock Engine >>](App-Lock-Engine)]
