# Privacy Lock Backup & Restore Guide

This guide explains how to back up and restore your settings, application lists, and security preferences locally on your device without relying on external cloud servers.

---

## 🏗️ 1. Offline Backup Architecture

To maintain complete privacy, Privacy Lock does not use external cloud storage for backups. Instead, it features a local, secure export/import mechanism:

* **Serialization**: The app converts your current settings, configuration preferences, and locked applications list into a single, compact, semi-colon-separated string.
* **Encryption**: The generated backup string can be encrypted or saved securely in a local text file or password manager.
* **No Network Usage**: Backups are processed entirely on your device, ensuring your configuration data never leaves your control.

---

## 📝 2. Backup Serialization Format

The generated backup string contains your configurations and locked applications list structured as a semi-colon-delimited payload:

### Example Backup Format:
```
1;global;hashedPinStr;hashedDecoyPinStr;hashedPanicPinStr;true;30;true;true;com.example.finance;com.example.social
```

### Field Structure:
1. **`1`**: Schema format version number (used to ensure backwards compatibility).
2. **`global`**: Database configuration scope identifier.
3. **`hashedPinStr`**: SHA-256 hash of your Master PIN.
4. **`hashedDecoyPinStr`**: SHA-256 hash of your Decoy PIN.
5. **`hashedPanicPinStr`**: SHA-256 hash of your Panic PIN.
6. **`true`**: Biometrics enabled flag.
7. **`30`**: Auto-lock timeout duration in seconds.
8. **`true`**: Keypad randomization flag.
9. **`true`**: Lock newly installed apps flag.
10. **`com.example.finance;com.example.social`**: Semicolon-separated list of locked application package names.

---

## 🚀 3. Import & Deserialization Validation

To prevent malicious or corrupted backup strings from causing application crashes, the import parser performs strict validation checks before writing data to the database:

```
[ Input String ] ──> [ Format Check ] ──> [ Version Check ] ──> [ Package Check ] ──> [ Database Write ]
```

### Import Validation Steps:
1. **Format Check**: Verifies that the input string is not empty and matches the expected semicolon-separated format.
2. **Version Check**: Checks the schema version number. If the backup was created on an older version of the app, the parser can migrate the data to the current format safely.
3. **Package Validation**: Verifies that the package names in the backup list exist on the device. If an app was uninstalled, its package name is ignored to keep the database clean.
4. **Data Integrity Check**: Verifies that passcode hashes are valid SHA-256 strings (64 characters) to prevent corrupted values from locking you out of your apps.

---

## 🔒 4. Best Practices for Storing Backups

Because your backup string contains your passcode hashes, protect it using secure storage practices:

* **Secure Storage**: Save your backup string inside an encrypted container, such as a secure password manager or a local, encrypted text file.
* **Do Not Share**: Never share your backup string with others, as anyone with access to the string can copy your passcode hashes and settings.
* **Keep Updated**: Generate a new backup string whenever you make major changes to your settings or locked applications list.
