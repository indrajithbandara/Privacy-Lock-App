# Privacy Lock Testing & QA Manual

This document outlines the testing strategy, QA workflows, test frameworks, and automated verification suites used to ensure the security and reliability of **Privacy Lock**.

---

## 🏗️ 1. Testing Strategy Overview

As a security application, Privacy Lock requires strict testing and validation before releases. Our testing suite is divided into three main layers:

```
┌────────────────────────────────────────────────────────┐
│                        Testing                         │
├────────────────────────────────────────────────────────┤
│  1. Unit Tests (JVM)       - Cryptography & Logics     │
│  2. Integration (Robolectric) - Database & Components  │
│  3. UI & Screenshot (Roborazzi) - Layouts & Themes     │
└────────────────────────────────────────────────────────┘
```

---

## 🧪 2. Automated Test Frameworks

Privacy Lock uses modern, high-performance testing libraries to run tests locally on development machines or inside CI pipelines:

### 1. Robolectric
Runs integration and system-level unit tests on a simulated Android environment inside the local JVM, without requiring an emulator or physical device.
* **Use Cases**: Verifies Room database queries, background services, and settings updates.

### 2. Roborazzi
A high-performance screenshot testing tool that captures UI elements in the JVM and compares them against reference images to catch layout regressions.
* **Use Cases**: Verifies Compose layouts, custom keypads, color themes, and landscape adaptions.

---

## 🚀 3. Core Test Configurations

Unit tests are written under `app/src/test/` to verify critical security flows and logical components:

### 1. Cryptography Validation (`SecurityUtilsTest`)
Verifies that passcode hashing performs consistently and securely:
```kotlin
@Test
fun testPinHashing() {
    val rawPin = "123456"
    val hashed = SecurityUtils.hashPin(rawPin)
    assertNotNull(hashed)
    assertEquals(64, hashed.length) // SHA-256 outputs a 64-character hex string
}
```

### 2. Session Token Tracking (`AppLockManagerTest`)
Verifies that application sessions expire correctly based on configured timeouts:
```kotlin
@Test
fun testSessionExpiry() {
    AppLockManager.temporarilyUnlockPackage("com.example.test")
    assertTrue(AppLockManager.isPackageTemporarilyUnlocked("com.example.test"))
    
    // Simulate session expiration
    AppLockManager.clearTemporaryUnlocks()
    assertFalse(AppLockManager.isPackageTemporarilyUnlocked("com.example.test"))
}
```

### 3. Database Integrity Tests (`AppDatabaseTest`)
Verifies that database transactions are written and updated correctly:
```kotlin
@Test
fun testDatabaseInsert() = runTest {
    val app = LockedApp("com.test", "Test App", "Social", true, false, false, "icon")
    db.lockedAppDao().insertApp(app)
    val retrieved = db.lockedAppDao().getAppDirect("com.test")
    assertEquals("Test App", retrieved?.name)
    assertTrue(retrieved?.isLocked == true)
}
```

---

## ⚙️ 4. Running Verification Commands

Execute these Gradle commands in your terminal to run the testing suite:

```bash
# Run all unit and Robolectric tests
./gradlew :app:testDebugUnitTest

# Record reference screenshots (run when making intentional UI changes)
./gradlew :app:recordRoborazziDebug

# Verify layouts against reference screenshots to catch visual regressions
./gradlew :app:verifyRoborazziDebug
```

---

## 🤖 5. Android Version & Platform Compatibility QA

Privacy Lock is tested across different Android versions to verify compatibility:

### 1. Edge-to-Edge Validation (Android 15)
Verifies that settings dashboards and overlay screens adapt correctly to Android 15's edge-to-edge constraints, keeping all buttons and touch targets legible.

### 2. Background Service Behavior (Android 16)
Monitors Android 16 developer builds to verify that background services are not closed by the system's battery manager.

### 3. OEM Device Testing
Verifies that background monitoring runs reliably on customized Android skins (e.g., Samsung One UI, Xiaomi MIUI) without being killed by aggressive battery optimizations.
