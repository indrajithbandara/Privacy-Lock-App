# Privacy Lock Development Roadmap

This document outlines the development milestones, security auditing plans, and feature roadmap for **Privacy Lock**.

---

## 🏗️ 1. Roadmap Overview

The development roadmap is divided into four main phases to ensure a stable, security-focused release schedule:

```
┌────────────────────────────────────────────────────────┐
│                        Roadmap                         │
├────────────────────────────────────────────────────────┤
│  v1.0 - Core Locker     - Offline engine & SQLite      │
│  v1.1 - Advanced Auth   - Decoy PINs & Biometrics      │
│  v1.2 - Stealth Tools   - Dialer code & backups        │
│  v2.0 - Keystore        - Keystore wrapping & patterns │
└────────────────────────────────────────────────────────┘
```

---

## 🚀 2. Phase Details & Milestones

### 🟢 Phase 1: Core Locking Engine (v1.0 Stable)
Focuses on building a stable, reliable core locking engine.
* **Status**: **Completed**
* **Key Features**:
  * Lightweight background accessibility monitoring.
  * Local, offline-first Room SQLite database integration.
  * Standard 6-digit Master PIN protection with SHA-256 hashing.
  * Simple settings dashboard with dynamic app list scanning.

### 🟡 Phase 2: Advanced Authentication (v1.1)
Introduces advanced authentication options and security enhancements.
* **Status**: **Completed**
* **Key Features**:
  * Biometric unlock integration using `androidx.biometric`.
  * Multi-passcode engine supporting Decoy and Panic PINs.
  * Randomized keypad layout to prevent shoulder-surfing.
  * Screenshot and screen recording blocking using `FLAG_SECURE`.

### 🔵 Phase 3: Stealth & Local Recovery (v1.2)
Focuses on backup options, stealth tools, and automatic protection.
* **Status**: **In Progress**
* **Key Features**:
  * **Stealth Dialer Launch**: Opens the app recovery interface by dialing a secure stealth code (e.g., `#1234`) in the phone app.
  * **Local Backups**: Semi-colon-separated backup import and export for local configurations.
  * **Auto-Lock New Apps**: Automatically secures newly installed applications as soon as they are added to the device.
  * **Intruder Logging**: Logs failed authentication attempts inside local database tables with timestamps and silhouettes.

### 🟣 Phase 4: Enterprise Posture (v2.0 Long-Term)
Introduces enterprise-level security features and advanced encryption.
* **Status**: **Planned**
* **Key Features**:
  * **Android Keystore Wrapping**: Secures database keys inside the hardware-backed Android Keystore.
  * **Argon2id Passcode Hashing**: Upgrades passcode hashing from SHA-256 to Argon2id to provide maximum resistance against brute-force attacks.
  * **Pattern Unlock UI**: Adds a customizable pattern-drawing lock interface alongside the numeric keypad.
  * **StrongBox Integration**: Stores cryptographic keys inside hardware-isolated secure elements on compatible devices.
