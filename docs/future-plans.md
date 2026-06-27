# Privacy Lock Future Development Plans

This document provides a technical overview of the planned upgrades, security enhancements, and feature additions for future major releases of **Privacy Lock**.

---

## 🏗️ 1. Security & Cryptographic Hardening

To provide the highest level of data protection, future major releases of Privacy Lock will focus on upgrading our cryptographic systems and hardware integration:

* **Argon2id Passcode Hashing**: We plan to upgrade our passcode hashing system from SHA-256 to **Argon2id**. This is a modern, memory-hard hashing algorithm that provides maximum resistance against offline brute-force attacks.
* **Android Keystore Database Wrapping**: We plan to wrap database encryption keys inside the hardware-backed **Android Keystore System**. This ensures database files are encrypted at rest and can only be decrypted when the user authenticates, protecting against database cloning attacks.
* **StrongBox Integration**: We plan to leverage StrongBox hardware key storage on compatible devices to provide hardware-isolated cryptographic key protection.

---

## 🎨 2. Enhanced Authentication Options

We plan to add more authentication options to make securing and unlocking your apps more convenient:

* **Custom Pattern Unlock**: We plan to design and build a secure, customizable pattern-drawing lock interface alongside our numeric keypad.
* **Custom Passcode Lengths**: We plan to support variable passcode lengths (4 to 12 digits) to give users more flexibility.
* **Security Lockouts**: We plan to implement progressive delay lockouts (e.g., 30-second delay after 5 failed attempts) to prevent rapid brute-force attacks.

---

## ⚙️ 3. Advanced Settings & Integrations

We plan to expand our local configuration options and automation features:

* **Secure Local Syncing**: We plan to support local backup syncing with self-hosted storage options (e.g., Synology NAS or Nextcloud) using secure local network transfers, without relying on external cloud servers.
* **Tasker Profile Plugins**: We plan to support Tasker integrations, allowing users to enable or disable specific app locks automatically based on time, location, or connected network profiles.
* **New App Alerts**: We plan to display a system notification prompting users to secure newly installed applications as soon as they are added to the device.
