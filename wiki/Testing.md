# Privacy Lock App Testing Guide

Welcome to the **Privacy Lock App Testing Guide**. This guide details our comprehensive testing methodology, automation frameworks, local JVM mock drivers, screenshot regression testing, and physical device test checklists.

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

## 🏗️ 1. Testing Strategy Overview

Privacy Lock relies on a three-tiered testing structure to guarantee security, stability, and pixel-perfect UI rendering without requiring slow, physical emulator testing on the main build agents:

```
┌────────────────────────────────────────────────────────┐
│                    Testing Pyramid                     │
├────────────────────────────────────────────────────────┤
│  1. Unit Tests     - Standard JUnit & Mock Providers   │
│  2. Robolectric    - Local headless Android UI Tests   │
│  3. Roborazzi      - Automated visual screenshot tests │
└────────────────────────────────────────────────────────┘
```

---

## 🧪 2. Automated Testing Suite

We run standard unit and integration tests inside the local Java Virtual Machine (JVM) using **Robolectric** and **Roborazzi**.

### Running Tests Locally
Execute the following Gradle tasks from your terminal:

```bash
# Run unit and local Robolectric tests
./gradlew :app:testDebugUnitTest

# Record reference Roborazzi screenshots (after intentional UI changes)
./gradlew :app:recordRoborazziDebug

# Verify current layout against reference screenshots
./gradlew :app:verifyRoborazziDebug
```

---

## 📋 3. Unit & Integration Testing Practices

We isolate database operations and view model lifecycles to ensure testing is clean and repeatable:

### In-Memory Database Tests
Room database DAOs are tested against a clean, transient in-memory database instance before every commit:

```kotlin
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var lockedAppDao: LockedAppDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        lockedAppDao = db.lockedAppDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }
}
```

### Critical User Journey (CUJ) Testing
Robolectric tests automate the end-to-end user experience, validating password creation, application locking, and decoy bypasses.

---

## 📸 4. Screenshot Regression Testing

We integrate **Roborazzi** to capture and assert the visual integrity of Jetpack Compose screens.

### How Screenshot Tests are Structured:
1.  **Isolate Layout**: Initialize the theme container and standard view model in a mock state.
2.  **Capture Node**: Specify the target Composable node (e.g., `DashboardScreen` or `SecureKeypad`).
3.  **Assert Match**: Roborazzi compares the newly generated screenshot against the master file located in the `screenshots/` root directory. If a mismatch of more than 0.1% of pixels is detected, the build fails.

---

## 📱 5. Manual QA Validation Checklist

Before publishing major releases, QA engineers must verify features on physical Android devices following this checklist:

### A. Core Lock Interception
- [ ] Confirm that launching a locked application triggers `LockActivity` within **100ms**.
- [ ] Confirm that pressing the system Home button instantly closes the lock screen and stops target application launch.
- [ ] Confirm that rapid back-press gestures do not leak target application frames.

### B. Credential Deniability (Decoy Mode)
- [ ] Verify that entering the **Master PIN** displays the actual configuration dashboard.
- [ ] Verify that entering the **Decoy PIN** unlocks the screen, displays "Cloud backup warning", but hides the database logs and sets dummy locks.

### C. Screen Shielding
- [ ] Attempt to capture a screenshot via system gestures (e.g., Power + Vol Down) on the lock overlay. Assert that the system blocks the action.
- [ ] Verify that the application preview displays as a solid black block inside the Recents carousel.

---

[<< Return to Administrator Guide](Administrator-Guide) | [Proceed to Performance Guide >>](Performance)
