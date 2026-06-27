# Project Roadmap

This roadmap outlines planned features, security enhancements, and technical milestones for Privacy Lock, grouped into short-term, medium-term, and long-term goals.

---

## 📅 Roadmap Overview

```
 [ Short Term ] ----------------> [ Medium Term ] ---------------> [ Long Term ]
 - Front Camera selfies           - Pattern / Alpha Locks          - Encrypted WebDAV backup
 - Direct Boot (FBE)              - Advanced Theme Engine          - Launcher Stealth Mode
 - Material 3 Widget              - System-wide Quick Settings     - Multi-User profiles
```

---

## 1. Short-Term Goals (Next 1–3 Months)

### 1.1 Front-Camera Picture Logs
* **Goal**: Enhance the Intruder Logger by capturing a real photo of the intruder using the front-facing camera on failed PIN entries.
* **Technical Plan**: Integrate the **CameraX** library to capture photos silently without displaying a camera preview when the wrong PIN threshold is exceeded.

### 1.2 Direct Boot Support (File-Based Encryption)
* **Goal**: Ensure Privacy Lock starts protecting applications immediately after a device reboot, even before the user has unlocked the device for the first time.
* **Technical Plan**: Move database storage to device-protected storage contexts (`DeviceProtectedStorageContext`) so SQLite databases are readable during Direct Boot.

### 1.3 Quick Status Widget
* **Goal**: Add a Material 3 home screen widget to let users toggle the lock engine or view active lock statistics quickly.

---

## 2. Medium-Term Goals (Next 3–6 Months)

### 2.1 Pattern and Password Credentials
* **Goal**: Expand locking mechanisms beyond 6-digit PINs to support Pattern Locks and alphanumeric Passwords.
* **Technical Plan**: Create generic interfaces for credentials, allowing the overlay UI to dynamically load either a `PatternCanvas` or a custom standard keyboard layout.

### 2.2 System-Wide Quick Settings Tile
* **Goal**: Allow users to toggle the app-lock engine or activate "Panic Mode" directly from the Android System Notification Shade.
* **Technical Plan**: Register an Android `TileService` that binds to the current state of the database and `AppLockManager`.

---

## 3. Long-Term Goals (Next 6+ Months)

### 3.1 Encrypted WebDAV Backups
* **Goal**: Allow users to sync their configurations and settings to a self-hosted cloud instance (such as Nextcloud or ownCloud) without compromising privacy.
* **Technical Plan**: Implement client-side cryptographic encryption using AES-256-GCM, wrapping settings into encrypted payloads before uploading them via WebDAV.

### 3.2 Launcher Stealth Mode
* **Goal**: Hide the Privacy Lock icon from the standard system application launcher completely to hide its presence.
* **Technical Plan**: Disable the primary launcher activity alias programmatically. Launch the application exclusively when dialing the custom stealth code (e.g., `#1234`) in the system dialer.

---

[[Home]] | [<< Changelog](Changelog) | [[API Reference >>](API-Reference)]
