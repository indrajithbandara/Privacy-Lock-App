# Privacy Lock Developer Manual

This developer manual provides technical guidelines, environment setup steps, build systems, styling rules, and development practices for engineering teams working on the **Privacy Lock** Android app.

---

## 🛠️ 1. Environment Setup

To compile and execute Privacy Lock, verify your environment meets these core requirements:

| Parameter | Recommended Specification |
| :--- | :--- |
| **Java Development Kit (JDK)** | JDK 17 (recommended: Azul Zulu or JetBrains Runtime) |
| **Android SDK Platform** | API 34 (Android 14) and API 35/36 (Android 15/16) |
| **Android Gradle Plugin (AGP)** | Version 8.8.x |
| **Kotlin Compiler** | Version 2.1.0+ |
| **Android Studio IDE** | Ladybug (2024.2.1) or Koala (2024.1.1)+ |

### Setting Up SDK Configurations:
1. Open Android Studio, navigate to `Settings -> Appearance & Behavior -> System Settings -> Android SDK`.
2. Install **Android SDK Platform 34**, **Android SDK Platform 35**, and **Android SDK Platform 36**.
3. Under the `SDK Tools` tab, install:
   * **Android SDK Build-Tools**
   * **Android SDK Platform-Tools**
   * **NDK (Side by side)**
   * **CMake**

---

## 📐 2. Project Structure

Privacy Lock is organized under a single-module Android build configuration:

```
├── app/
│   ├── src/main/
│   │   ├── java/com/example/
│   │   │   ├── MainActivity.kt        # Setup wizard, navigation, and core screen routes
│   │   │   ├── LockActivity.kt        # Full-screen secure keypad lock screen overlay
│   │   │   ├── data/                  # Room SQL schemas, DAOs, and repository patterns
│   │   │   │   ├── AppDatabase.kt     # SQLite Room Database builder
│   │   │   │   ├── Daos.kt            # DB Queries (LockedApp, Config, Intruder, Event log)
│   │   │   │   ├── Entities.kt        # Entity schemas corresponding to Room tables
│   │   │   │   └── PrivacyRepository.kt # Device app syncs and DB transaction managers
│   │   │   ├── security/              # Lock interception mechanisms & security APIs
│   │   │   │   ├── AppAccessibilityService.kt # System events listener (TYPE_WINDOW_STATE_CHANGED)
│   │   │   │   ├── AppLockManager.kt   # Thread-safe in-memory session tokens and timeouts
│   │   │   │   └── SecurityUtils.kt   # Cryptographic utilities (SHA-256) and permission intents
│   │   │   └── ui/                    # Presentation screens and layouts (Compose)
│   │   │       ├── PrivacyViewModel.kt # Presentation state manager
│   │   │       ├── components/        # Interactive components (SecureKeypad, SimulatedAppIcon)
│   │   │       ├── screens/           # DashboardScreen, SettingsScreen, PrivacyCenterScreen
│   │   │       └── theme/             # Material 3 ColorScheme, Typography, Shapes
│   │   ├── res/                       # Native Android resources
│   │   │   ├── xml/                   # Accessibility and backup settings configs
│   │   │   └── values/                # Localization string files
```

---

## 📦 3. Dependencies & Version Catalog

Privacy Lock uses a centralized Gradle **Version Catalog** located in `gradle/libs.versions.toml`. All libraries, testing utilities, and compiler plugins must be declared there before referencing them in `/app/build.gradle.kts`.

### Standard Gradle Syntax Rules:
When declaring dependencies in `build.gradle.kts` using catalog references, translate **kebab-case** keys from TOML to **dot-notation** in Kotlin.

**Example Catalog Declaration (`libs.versions.toml`):**
```toml
[versions]
androidx-room = "2.6.1"

[libraries]
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "androidx-room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "androidx-room" }
```

**Corresponding Reference in `app/build.gradle.kts`:**
```kotlin
dependencies {
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
}
```

---

## 🛠️ 4. Build & Compilation Commands

Execute these Gradle commands from the workspace root folder to build and maintain the application:

```bash
# Clean build artifacts and build cache
./gradlew clean

# Compile and build the debug APK package
./gradlew assembleDebug

# Compile the release candidate (produces an unsigned release package)
./gradlew assembleRelease

# Run standard code quality checking / static linter checks
./gradlew lintDebug

# Run JVM local unit tests (Robolectric)
./gradlew :app:testDebugUnitTest

# Record visual screenshot baselines using Roborazzi
./gradlew :app:recordRoborazziDebug

# Verify visual screens against reference screenshots using Roborazzi
./gradlew :app:verifyRoborazziDebug
```

---

## 💅 5. Code Style & Coding Standards

To maintain clean and readable code, all contributors must follow these coding rules:

* **Asynchronous execution**: Always use Kotlin Coroutines. Avoid blocking threads with database or package scanning operations. Use `Dispatchers.IO` for disk and database reads, and `Dispatchers.Main` for UI states.
* **Flow state tracking**: Always use `MutableStateFlow` inside ViewModels. Collect states in Compose using `collectAsStateWithLifecycle()` to prevent memory leaks.
* **Safe memory buffers**: Passcodes must be processed using zeroable arrays (e.g., `CharArray`) when possible, rather than immutable `String` arrays, to avoid leaving sensitive inputs in JVM memory.
* **Explicit comments**: Document accessibility intercept rules, biometric configurations, database schemas, and cryptographic procedures clearly using standard Kotlin KDoc notation.

---

## 🤝 6. Standard Git Development Workflow

To ensure smooth integrations, developers must follow this Git workflow:

1. **Create Branch**: Create a feature-specific branch from `main`:
   ```bash
   git checkout -b feature/biometric-timeout-v1.1
   ```
2. **Implement Changes**: Write code, add unit tests, and verify linter checks locally.
3. **Commit Messages**: Write descriptive commit messages matching standard semantic specifications:
   ```bash
   git commit -m "feat(biometrics): implement biometric-prompt session timeout integration"
   ```
4. **Push and Pull Request**: Push changes to GitHub and open a Pull Request targeting the `main` branch. Ensure the PR links to the corresponding issue card in the **Privacy Lock App Roadmap** board.
