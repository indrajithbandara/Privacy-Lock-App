# Changelog

This page documents the release history and changelog policies of the Privacy Lock project. We maintain a detailed chronological record of updates, bug fixes, and security enhancements.

---

## Changelog Policy

* We adhere to the [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) format.
* Every release must categorize updates into:
  * **Added**: New features or capabilities.
  * **Fixed**: Bug fixes and behavior corrections.
  * **Security**: Crucial patches addressing security issues.
  * **Changed**: Modifications to existing functionality.

---

## [1.2.0] — 2026-06-26 (Current Release)

This release standardizes the security keypad layouts to prevent visual sniffing and shoulder-surfing, and integrates a dynamic on-demand screenshot shield.

### Added
* **Dynamic Screenshot Protection Switch**: Added an option in **Settings** to toggle screenshot protection on demand.
* **Persistent DataStore Integration**: Preferences are now saved using **Jetpack Preferences DataStore** to ensure settings persist across application restarts.
* **Auto-Lock Timeout Configurations**: Users can now configure how long an unlocked app remains accessible before re-locking (e.g., Immediately, 15 seconds, 1 minute, etc.).

### Fixed
* **Security Keypad Layout Standardized**: Re-organized the 12-button keypad to match the standard Android PIN pad layout:
  * Digits $1$ through $9$ are laid out in a grid.
  * **0** is always centered on the bottom row.
  * **Clear** is locked to the left.
  * **Backspace** is locked to the right.
* **Keypad Randomization Logic**: Enabling **Random Keypad** now shuffles only digits $1$ through $9$, keeping the positions of $0$, Clear, and Backspace fixed to maintain usability.

### Security
* **System-Wide Masking**: When Screenshot Protection is enabled, app previews are masked with a solid background in the Recent Apps list to prevent data leakage.

---

## [1.1.0] — 2026-04-10

### Added
* **Biometric Authentication**: Added fingerprint and face unlock options using Android's `BiometricPrompt` API.
* **System Category Sorting**: Added automatic category classification for installed apps (e.g., Social, Finance, Utilities) inside the dashboard.
* **Intruder Selfie Logging**: Added logging of failed access attempts, including timestamps, tried codes, and stylized vector silhouettes.

### Fixed
* **Service Lifecycle Optimization**: Resolved an issue where the background Accessibility Service would shut down on low-spec devices.

---

## [1.0.0] — 2026-01-15

### Added
* **Initial Release**: Released core application lock service relying on a 6-digit PIN.
* **Local SQLite Storage**: Configured local Room database persistence for locked applications and event logs.
* **User Onboarding Flow**: Implemented a wizard to guide users through enabling Accessibility, Usage Stats, and System Overlay permissions.

---

[[Home]] | [<< Release Process](Release-Process) | [[Roadmap >>](Roadmap)]
