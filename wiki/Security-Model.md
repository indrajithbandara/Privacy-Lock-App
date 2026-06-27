# Security Model & Threat Profiles

Privacy Lock enforces a highly disciplined security model designed to isolate user configurations, defend against forced disclosure, and resist physical or local network-based attacks. 

---

## 🔒 1. Cryptographic Architecture

To ensure maximum privacy, Privacy Lock operates under a **zero-knowledge local trust model**. Sensitive configurations and PIN hashes are encrypted and stored inside the on-device SQLite database.

```
                  +-----------------------------------+
                  |      User Enters 6-digit PIN      |
                  +-----------------------------------+
                                    |
                                    v
                  +-----------------------------------+
                  |   Combine PIN with Unique Salt    |
                  +-----------------------------------+
                                    |
                                    v
                  +-----------------------------------+
                  |        SHA-256 Hash Function      |
                  +-----------------------------------+
                                    |
                                    v
                  +-----------------------------------+
                  | Compare with stored Hash in Room  |
                  +-----------------------------------+
```

### Cryptographic Hashing properties:
* **One-Way Hashing**: PINs are never stored as plain text. Every credential (Primary, Decoy, and Panic) is compiled with a randomized, cryptographically strong salt and hashed using **SHA-256**.
* **Rainbow Table Protection**: Salts are regenerated and saved alongside the hashed output in the database. Attackers cannot precompute hash lists to recover credentials.
* **Database Key Wrapping (Enterprise Target)**: Release builds support wrapping the database encryption key via the Android Keystore System (leveraging AES-GCM-256 keys backed by secure hardware like StrongBox or Trusted Execution Environments).

---

## ⚔️ 2. Passive & Active Threat Defenses

Privacy Lock protects the active device environment through several advanced defensive strategies:

### 2.1 Dynamic Window Shield (`FLAG_SECURE`)
When Screenshot Protection is toggled ON, Privacy Lock requests the Android System to apply `WindowManager.LayoutParams.FLAG_SECURE` to active UI windows.
* **Capture Blocked**: Standard screenshot attempts are blocked, emitting a toast informing the user of the security policy.
* **Recording Inhibited**: Screen recorders and casting streams receive a blank, solid black canvas.
* **Switcher Masking**: The Recent Apps system switcher hides the screen's cached thumbnail with a dark solid block, preventing residual data leakage.

### 2.2 Shoulder-Surfing and Smudge Protection (Keypad Shuffle)
* **The Vulnerability**: Continuous PIN input leaves finger-grease smudge trails on the glass screen, which can easily betray a sequence of numbers.
* **The Defense**: Enabling **Random Keypad** dynamically shuffles digits $1$-$9$ into random positions on every load event. Operation of the keypad remains standard while ensuring coordinate tracking is physically randomized.

---

## 🛡️ 3. Threat Model and Mitigation Matrix

| Threat | Attack Vector | Mitigation Strategy | Efficiency |
| :--- | :--- | :--- | :--- |
| **Physical Seizure** | Attacker grabs unlocked device | Accessibility Service locks targeted packages immediately on state change | **High** |
| **Forced Disclosure** | Attacker forces user to enter PIN | User inputs **Decoy PIN** to trigger a fake crash or sandboxed dashboard | **Maximum** |
| **Shoulder Surfing** | Attacker observes finger positioning | **Random Keypad** shuffles numeric button positions dynamically | **High** |
| **Smudge Analysis** | Screen grease analysis | Shuffled numbers change the physical tap coordinates on the glass | **High** |
| **RAM Inspection** | Attacker dumps memory of running processes | Credentials are kept in JVM RAM as ephemeral `CharArrays` and wiped immediately | **Medium** |
| **Root Access** | Attacker roots device to read SQLite data | SQLite is encrypted via SQLCipher with key derived from hardware Keystore | **High** |

---

## ☣️ 4. Plausible Deniability (Decoy and Panic States)

The application provides unique defensive profiles for extreme coercion scenarios:

* **Decoy PIN Action**: If entered, the lock overlay acts as if authentication succeeded, but routes the user to a mock interface or fake crash sequence. This provides a convincing disguise, leading an attacker to believe the app crashed or holds no real configurations.
* **Panic PIN Action**: Instantly revokes all temporarily unlocked packages, clears current cache lists, and closes the active viewport. It forces an immediate routing back to the standard home screen launcher, minimizing open surfaces.

---

[[Home]] | [<< Features](Features) | [[Accessibility Service >>](Accessibility-Service)]
