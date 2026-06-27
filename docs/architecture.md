# Privacy Lock App Architecture Guide

This document provides a deep, production-grade architectural blueprint of **Privacy Lock**, an offline-first Android security application. It outlines the structural design, design patterns, internal threads, component interactions, and execution lifecycles that govern the app's secure runtime execution.

---

## 🏗️ 1. Core Architectural Overview

Privacy Lock is built on clean, decoupled architecture principles, combining **Model-View-ViewModel (MVVM)**, the **Repository Pattern**, and **Unidirectional Data Flow (UDF)**. 

To ensure maximum security and efficiency, the application is strictly divided into three layers:
1. **Presentation Layer (UI & Jetpack Compose)**: Houses composable screens, theme managers, and custom widgets. It is entirely stateless, relying on the `PrivacyViewModel` to expose UI state and capture user interactions.
2. **Domain/Business Logic Layer (ViewModels & Managers)**: Manages lock status, monitors background activities, processes cryptographic hashes, and coordinates configuration sessions.
3. **Data Layer (Room Database & DataStore)**: Handles local SQLite queries, stores app package lists, records intruder logs, and persists encrypted user configurations.

```mermaid
graph TD
    %% Presentation Layer
    subgraph Presentation_Layer [Presentation Layer]
        Compose[Jetpack Compose Screens]
        MainAct[MainActivity]
        LockAct[LockActivity]
        VM[PrivacyViewModel]
    end

    %% Business Logic Layer
    subgraph Business_Logic_Layer [Domain & Session Logic]
        LockMgr[AppLockManager]
        AccessService[AppAccessibilityService]
    end

    %% Data Layer
    subgraph Data_Layer [Data & Storage Layer]
        Repo[PrivacyRepository]
        RoomDB[AppDatabase Room]
        DataStore[Preferences DataStore]
    end

    %% Security Module
    subgraph Security_Module [Security Utilities]
        Crypt[SecurityUtils]
    end

    %% Flows
    Compose -->|Observes Flow| VM
    LockAct -->|Checks Token| LockMgr
    VM -->|Dispatches Events| Repo
    Repo -->|Queries SQLite| RoomDB
    AccessService -->|Monitors Foreground| LockMgr
    AccessService -->|Launches overlay| LockAct
    LockAct -->|Wipes CharBuffers| Crypt
    LockMgr -->|Hashes Inputs| Crypt
```

---

## 📂 2. Presentation Layer & Navigation Flow

The UI uses **Jetpack Compose** with a centralized `PrivacyViewModel` providing the state. The main application navigation runs on type-safe Compose routes, while the blocking overlay keypad runs as a separate task inside `LockActivity`.

### Navigation Flow Diagram
This diagram outlines the screens, navigation paths, and conditional checks required to transition from the setup wizard to the protected dashboards.

```mermaid
graph TD
    Start([App Launch]) --> CheckPIN{PIN Configured?}
    
    CheckPIN -->|No| SetupWizard[Onboarding Screen]
    SetupWizard --> SetPIN[Set 6-Digit PIN Screen]
    SetPIN --> Dashboard[Dashboard Screen]
    
    CheckPIN -->|Yes| PromptPIN[Unlock Dashboard Screen]
    PromptPIN -->|Correct PIN| Dashboard
    
    Dashboard -->|Navigate| Settings[Settings Screen]
    Dashboard -->|Navigate| PrivacyCenter[Privacy Center Screen]
    PrivacyCenter -->|View Logs| IntruderLogs[Intruder Logs Screen]
```

---

## 🛡️ 3. App Interceptor & Background Execution Thread

The core capability of Privacy Lock is launching a secure lock screen over protected applications when they enter the foreground. This process is governed by the native Android `AppAccessibilityService` and coordinated with a thread-safe `AppLockManager` session cache.

### Interception Sequence diagram
The sequence below illustrates the step-by-step event flow when a user launches a locked application (e.g., a Finance app):

```mermaid
sequenceDiagram
    autonumber
    actor User
    participant System as Android OS
    participant Service as AppAccessibilityService
    participant Manager as AppLockManager
    participant LockAct as LockActivity
    participant TargetApp as Target Application

    User->>System: Launches Finance App
    System->>Service: Dispatches TYPE_WINDOW_STATE_CHANGED (com.finance)
    Service->>Manager: isPackageLocked("com.finance")
    Manager-->>Service: Return true
    Service->>Manager: isPackageTemporarilyUnlocked("com.finance")
    Manager-->>Service: Return false
    Service->>System: Starts LockActivity (Intent)
    System->>LockAct: Launches on Top (FLAG_SECURE)
    LockAct->>User: Displays PIN Keypad
    User->>LockAct: Enters Correct PIN
    LockAct->>Manager: temporarilyUnlockPackage("com.finance")
    LockAct->>System: finish()
    System->>TargetApp: Reveals Target App View
```

---

## 💾 4. Data Layer Architecture (Room Database Schema)

The persistent storage structure relies on a single Room Database instance `privacy_lock_db` with four core tables, fully synchronized in real-time.

```mermaid
erDiagram
    locked_apps {
        string packageName PK
        string name
        string category
        boolean isLocked
        boolean isFavorite
        boolean isPinned
        string iconName
    }
    security_config {
        string id PK
        string hashedPin
        string hashedDecoyPin
        string hashedPanicPin
        boolean biometricsEnabled
        int autoLockTimeoutSeconds
        boolean randomizeKeypad
        boolean vibrateOnKeyPress
        boolean lockNewlyInstalledApps
        boolean intruderDetectionEnabled
        boolean intruderSelfieEnabled
        int failedAttemptLimit
        boolean screenshotProtection
        boolean autoLockNewApps
        int temporaryUnlockDurationMinutes
        string decoyModeType
        string themeMode
        string stealthCode
    }
    timeline_events {
        int id PK
        long timestamp
        string type
        string description
        string appName
    }
    intruder_selfies {
        int id PK
        long timestamp
        string failedPinAttempt
        int avatarId
    }

    security_config ||--o{ timeline_events : "triggers logs"
    locked_apps ||--o{ timeline_events : "records actions"
    intruder_selfies ||--o{ timeline_events : "references"
```

---

## ⚙️ 5. MVVM & Repository Decoupling Pattern

To prevent code duplication, `PrivacyRepository` exposes cohesive Kotlin `Flow` structures and executes all operations asynchronously on the IO dispatcher (`Dispatchers.IO`). view models collect these data streams and transform them into Compose state wrappers using `stateIn`.

### MVVM Interactions
```mermaid
classDiagram
    class AppDatabase {
        +lockedAppDao()
        +securityConfigDao()
        +timelineEventDao()
        +intruderSelfieDao()
    }
    class PrivacyRepository {
        -lockedAppDao
        -securityConfigDao
        -timelineEventDao
        -intruderSelfieDao
        +allLockedApps: Flow
        +securityConfig: Flow
        +allEvents: Flow
        +allSelfies: Flow
        +toggleLock(packageName)
        +updateConfig(config)
    }
    class PrivacyViewModel {
        -repository
        +uiState: StateFlow
        +toggleAppLock(packageName)
        +saveSecurityConfig(config)
    }
    class LockActivity {
        -showBiometricPrompt()
        -unlockApp()
    }

    AppDatabase <-- PrivacyRepository : queries
    PrivacyRepository <-- PrivacyViewModel : consumes
    PrivacyViewModel <-- LockActivity : observes settings
```

---

## 🚀 6. CI/CD DevOps Pipeline Architecture

The release automation system is built on GitHub Actions, running tests, audits, and compilation suites inside isolated environments.

```mermaid
graph LR
    Push([Git Push / PR]) --> Checkout[Checkout Code]
    Checkout --> SetupJDK[Setup JDK 17]
    SetupJDK --> Lint[Run Gradle Lint]
    Lint --> UT[Run Robolectric Unit Tests]
    UT --> BuildDebug[Compile Debug APK]
    BuildDebug --> BuildRelease[Compile Release APK/AAB]
    BuildRelease --> Artifacts[Archive signed Artifacts]
```
