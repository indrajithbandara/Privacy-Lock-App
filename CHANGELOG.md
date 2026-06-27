# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.1] - 2026-06-26

### Added
- **Screenshot Protection Toggle**: Introduced a dedicated setting to programmatically apply `WindowManager.LayoutParams.FLAG_SECURE` to the PIN lock overlay activity, blocking system screenshots, screen recordings, and Recents screen task previews.

### Fixed
- **PIN Keypad Layout Alignment**: Corrected the PIN keypad layout to follow the standard Android phone design:
  ```
  1  2  3
  4  5  6
  7  8  9
  Clear  0  Backspace
  ```
  Ensured `0` remains centered in the bottom row under all conditions, and that `Clear` stays on the left and `Backspace` stays on the right, even when Randomized Keypad is active.

---

## [1.0.0] - 2026-06-26

### Added
- **Privacy Dashboard**: Centralized screen showing the device's Privacy Score, security health checks, and recent intrusion attempts.
- **App Lock Manager**: Ability to toggle secure locks on system apps (Settings, Play Store, Package Installer) and user applications.
- **Intruder Selfie & Secure Logs**: Captures intrusion attempts on failed entry pins. Uses secure avatar indicators and masks the entered PIN entry (`****`) to prevent shoulder-surfing logs.
- **Decoy Stealth PIN**: Allows setting a second, separate decoy PIN. Entering the decoy PIN displays a safe "Decoy Mode" state with normal un-locked system behaviors to avoid coercion.
- **Randomized Keypad Layout**: Security recommendation toggle that shuffles the numeric pin entry grid on launch to mitigate finger smudge and screen-capture attacks.
- **Privacy Timeline**: Local, encrypted activity logs recording lock events, setting changes, and system warnings with full MVVM-backed history.
- **Sage Green M3 Theme**: Centralized, customized Material Design 3 theme utilizing Sage, Olive, and Forest green accents with smooth dark mode handcrafting.
- **Offline-First Room Integration**: Multi-table local Room database architecture utilizing KSP-based data access objects (DAOs) and a repository structure.
- **Backup Exclusion Configurations**: Explicit Android XML backup and data extraction exclusions for database files to prevent backup plain-text database exposure.
