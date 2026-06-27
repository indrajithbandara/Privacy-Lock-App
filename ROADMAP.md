# Privacy Lock Project Roadmap

This document outlines the planned developmental phases and milestones for the Privacy Lock project, classified by timeline.

---

## 🎯 Phase 1: Short-Term Milestones (v1.1.0 - v1.2.0)

### 1. Robust Biometric Failbacks
* **Goal**: Provide standard biometric authentication (fingerprint, face unlock) as a seamless alternative to PIN entry.
* **Tech Details**: Use Jetpack Biometric library (`androidx.biometric:biometric-ktx`) to present standard system prompt. Ensure cryptographic key authentication can fall back cleanly to Master PIN if requested.

### 2. Intruder Alert Badge Notifications
* **Goal**: Notify users immediately upon opening the Privacy Dashboard if any new intrusion attempts were recorded while they were away.
* **Tech Details**: Query the `IntruderSelfie` Room table for events flagged as "unread" or occurring since the last dashboard view. Show a dynamic badge and indicator count in the status panel.

---

## 🚀 Phase 2: Medium-Term Milestones (v2.0.0)

### 3. Customizable Lock Schedules
* **Goal**: Allow users to define automatic time windows or calendar rules during which specific apps are locked or unlocked.
* **Tech Details**: Utilize Android `WorkManager` to trigger background database updates changing the lock state of target applications at specified intervals.

### 4. Advanced Pattern & Gesture Locks
* **Goal**: Expand authentication options beyond standard 6-digit numeric PINs by introducing a 3x3 pattern lock grid.
* **Tech Details**: Build a custom high-performance touch-gesture Canvas component in Jetpack Compose, storing gesture paths securely using hashed coordinates.

---

## 🛡️ Phase 3: Long-Term Milestones (v3.0.0+)

### 5. Multi-Profile Decoy Sandboxing
* **Goal**: Enable setting up distinct decoy profiles that not only unlock the device but change the visible application list entirely.
* **Tech Details**: Support mapping specific locked applications to individual decoy pins, conditionally hiding them from the dashboard list when that specific decoy pin is validated.

### 6. Hardware-Backed Encryption Keys
* **Goal**: Encrypt local security databases using SQLCipher with keys generated and protected inside the Android Keystore System.
* **Tech Details**: Integrate SQLCipher into the Room database creation process, with encryption keys wrapped by Keystore-backed AES/GCM keys requiring user authentication.
