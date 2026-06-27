# Privacy Policy

Privacy Lock is designed with an **offline-first** architectural model. We believe that your privacy settings and personal access logs should remain under your absolute control on your physical device.

---

## 1. Data Stored Locally On Your Device

All data processed by Privacy Lock is written exclusively to secure local storage paths on your device using Android's sandbox isolation:

* **Locked Application Configuration**: A list of package IDs (e.g., `com.whatsapp`) and their lock status is stored inside the local SQLite database.
* **Security Configurations**: Hashed PIN parameters (SHA-256 hashes of your Primary, Decoy, and Panic PINs) and settings preferences (e.g., Random Keypad enabled) are stored locally.
* **Security Event Timeline Logs**: Timestamps and event description text (e.g., "Unlocked WhatsApp") are stored locally to let you audit access.
* **Intruder Attempt Logs**: Invalidation occurrences, including attempted codes, timestamps, and localized vector silhouette mappings, are stored locally.

These files are located inside the application's secure private folder:
`/data/data/com.example/databases/`

---

## 2. Data We Never Collect

Because Privacy Lock contains **no network communication modules or third-party tracking SDKs**, we never collect or transmit the following data:

* **No Personal Identifiable Information (PII)**: We do not know your name, email address, phone number, or device identifiers.
* **No Telemetry or Crash Reports**: We do not use remote analytics services (such as Firebase Analytics or Flurry). If the app crashes, crash trace files are processed only locally on your machine.
* **No PIN Leakage**: Your plaintext PIN strings are processed in memory and never written to disk. Only salted SHA-256 hashes are stored to verify credentials.
* **No Cloud Backups**: We do not automatically upload database backups to any remote servers.

---

## 3. Third-Party Integrations

Our codebase contains zero integrations with external networks or commercial advertising networks.
* **Android System APIs**: The application only communicates with standard Android OS APIs (such as the Accessibility Service API and the Biometric System API) to perform lock operations.

---

## 4. Updates and Audits

As an open-source project licensed under the Apache 2.0 license, our complete source code is available for public audit. You can review the codebase at any time to verify our privacy and security guarantees.

---

[[Home]] | [<< License](License) | [[Security Policy >>](Security-Policy)]
