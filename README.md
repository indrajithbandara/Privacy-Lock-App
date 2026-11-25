# Privacy Lock

An open-source, offline-first Android app-lock and privacy utility.

> Development branch under validation. The future product name is **Untouched — Your apps. Your boundaries.** No production release or operating-system security guarantee is implied.

## Features

- Select installed, eligible applications for local PIN authentication.
- Versioned, salted PBKDF2 PIN records protected by Android Keystore encryption.
- Persistent authentication delays and configurable relocking.
- SQLCipher-backed Room storage with a verified-copy migration from the existing schema.
- Strong biometric authentication when supported, using a PIN-authorized, per-use Keystore key.
- Local security events and user-selectable retention.
- Light and dark themes; initial Sinhala and Tamil navigation/dashboard translations.

## Important limitations

The application reacts to Android accessibility window events. It cannot create an operating-system security boundary, guarantee that protected content is never visible, or prevent a device owner from disabling permissions, uninstalling, or clearing application data. OEM background restrictions, multi-window mode, notifications and recents can affect behavior. System applications and launchers are excluded. See the [security register](docs/security-baseline.md) and [privacy policy](docs/privacy-policy.md).

## Build and test

Requires JDK 21, Gradle 9.3.1, Android SDK 36.1, and supports Android API 24+. See [BUILDING.md](BUILDING.md) for exact commands.

[CI workflows](https://github.com/indrajithbandara/Privacy-Lock-App/actions) · [Security policy](SECURITY.md) · [Release procedure](RELEASE_PROCESS.md)

## Distribution

Debug and unsigned release validation artifacts are solely for testing; they are not signed production releases. Existing published releases may represent older prototype versions. No Google Play or F-Droid approval is claimed.

Application ID: `com.aistudio.privacylock.pnvxld` (retained for upgrade compatibility).

## License

Apache License 2.0. See [LICENSE](LICENSE).
