# Product Features

Privacy Lock provides a robust, offline-first security envelope around your sensitive Android applications. Below is an in-depth tour of the complete user-facing and deep technical features implemented within the product.

---

## 🛠️ 1. Core Interception & Lock Engine
The core value proposition of Privacy Lock lies in its ability to intercept unauthorized application openings immediately at the OS level before any content is shown.

* **Accessibility-Driven Interceptor**: Leverages Android's Accessibility framework to listen directly to `TYPE_WINDOW_STATE_CHANGED` events. This provides deterministic, instant blocking with near-zero overhead.
* **Smart Category Grouping**: Applications are intelligently grouped into categories (e.g., Social, Finance, Utilities, System) on the primary Dashboard. Users can lock/unlock entire categories with a single tap.
* **Custom Unlock Timeout**: Configurable auto-lock grace periods (e.g., Immediate, 15 seconds, 1 minute, 5 minutes) via settings. If the user returns to a recently unlocked app within the timeout window, they are not prompted again.

---

## 🔑 2. Multi-PIN Security Shield
Privacy Lock defends against forced-disclosure and shoulder-surfing scenarios through multiple independent PIN configurations.

```
                  +--------------------------------+
                  |    Keypad Unlock Interface     |
                  +--------------------------------+
                                   |
         +-------------------------+-------------------------+
         |                         |                         |
         v                         v                         v
  [ Primary PIN ]            [ Decoy PIN ]             [ Panic PIN ]
         |                         |                         |
         v                         v                         v
Unlock application and      Unlock application with   Lock all apps immediately,
grant full dashboard access.  simulated crash or fake    clear memory cache, and
                            restricted dashboard.     force exit to Home screen.
```

* **Primary PIN**: The master 6-digit credential. Successfully entering this PIN unlocks the target application and grants unrestricted access to the settings dashboard.
* **Decoy PIN (Plausible Deniability)**: If a third party forces disclosure of the lock, entering the Decoy PIN unlocks the app but triggers a simulated crash dialog or opens a secondary, simulated dashboard with placeholder statistics.
* **Panic PIN (Emergency Exit)**: Entering the Panic PIN instantly closes the app, clears all active unlocked sessions from the memory cache, and forces an immediate exit back to the Android Home screen.

---

## 🛡️ 3. Advanced Shielding & Privacy
* **Randomized Keypad Layout**: When enabled, the digits $1$ through $9$ are shuffled into a randomized grid on every unlock screen presentation, defeating smudge tracking and over-the-shoulder visual scanning.
* **Screenshot & Recents Masking**: Applies system-level `FLAG_SECURE` window shields to prevent screenshot capture, continuous video screen recording, and exposure of app state previews inside Android's "Recent Apps" switcher.
* **Biometric Fallback**: Integrates seamlessly with Android's secure `BiometricPrompt` (`BIOMETRIC_STRONG` tier), allowing fingerprint or 3D face unlock as an alternative to typing the PIN.

---

## 📝 4. Privacy Center & Local Auditing
All security-related logging occurs strictly on-device, with zero external network connectivity.

* **Failed Attempt Tracker**: Captures the exact timestamp, target application, and invalid credential entered during a failed login attempt.
* **Intruder Center Timeline**: Displays full historical timelines of failed intrusion attempts. Each incident is mapped to a unique decorative vector silhouette avatar representing the failed breach.
* **Stealth App Icon**: Support for customizing or hiding the launcher icon, bypassing standard launcher audits.

---

## 📲 5. Application Feature Matrix

| Feature | Primary Function | Data Persistence | Privacy Level |
| :--- | :--- | :--- | :--- |
| **Accessibility Intercept** | Blocks target packages deterministically | Local State Cache | Offline-Only |
| **Plausible Deniability** | Simulated crash / fake dashboard with Decoy PIN | Encrypted Room Table | High |
| **Keypad Shuffling** | Randomizes layout of digits $1$-$9$ | Dynamic In-Memory | High |
| **Window Security Shield** | Employs `FLAG_SECURE` layout flags | System Window Bus | Maximum |
| **Intruder Selfie Capture** | Captures timeline and generates avatars on failed login | Room SQLite DB | Secure |
| **Portability backups** | Text-based configuration import/export | Local Storage File | Local-Only |

---

[[Home]] | [[Installation >>](Installation)]
