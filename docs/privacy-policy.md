# Privacy policy

This project is designed for local Android privacy controls. The app does not require an account, advertising SDK, tracking SDK or Internet permission for its core functionality.

## Data processed on device

- Installed launcher application names and package identifiers are used to display eligible applications.
- The accessibility service observes window-state events and package identifiers to decide whether to request authentication. It is configured not to retrieve window content.
- PIN verification uses a salted credential verifier encrypted with an app-specific Android Keystore key. Entered PIN values are not written into security events.
- Strong biometrics are processed by Android's biometric API. The app does not receive biometric templates.
- Local security events contain timestamps, event categories and optional application names. No camera photography is collected.
- User-created protection profiles contain names, selected application package identifiers, activation states and optional stricter relock rules; profile changes are recorded locally in the security timeline.
- Settings and records are stored locally in encrypted Room storage. Existing legacy records are migrated where supported. Retention can be changed by the user or logs cleared with PIN confirmation.

## Control and limitations

The user may revoke accessibility permission, change Android settings or uninstall the app at any time. Core functionality is offline. Backup is restricted to avoid exporting protected records. Clearing app data or losing the device-specific key may make encrypted records inaccessible; no remote recovery is provided. This is not a promise of absolute device security.

The permission setup presents a clear in-app disclosure and explicit confirmation before navigating to Android's accessibility settings. The app does not grant itself accessibility access or prevent the device owner from revoking it.

Contact: indrajithbandarasl@gmail.com.
