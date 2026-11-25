# Security baseline (development branch)

This is an incomplete remediation, not a production release or independent audit.

| Finding | Severity | Status |
| --- | --- | --- |
| PL-01 | Critical | Remediated in source: explicit enrollment, versioned PBKDF2 with unique salt, Keystore-wrapped verifier and constant-time comparison. Unit/device baseline passed at `a445dcde`; physical upgrade remains unverified. |
| PL-02 | Critical | Remediated in source: removed decoy unlock, PIN confirmation for sensitive changes and persistent throttling. Unit/device baseline passed at `a445dcde`; full foreground test remains an open release gate. |
| PL-03 | High | Release signing fallback removed; optimized unsigned artifact and checksum validation passed. Production key and fingerprint validation remain open. |
| PL-04 | High | SQLCipher plus independent Keystore database key, verified legacy copy and additive migrations. Encryption/interruption/profile migration tests passed on API 24/36 at `a445dcde`; new schema migration requires its own run. |
| PL-05 | High | Monotonic session expiry, screen-off clearing and timed foreground check implemented. Session unit/device tests passed at `a445dcde`; actual foreground/OEM behavior remains open. |
| PL-06 | High | Removed simulated app fixtures from production and switched to actual installed-app discovery; repository behavior tests passed. Real install/remove and OEM scenarios remain unverified. |
| PL-07 | High | Genuine local failed PIN/lockout events, explicit retention/deletion, no new avatar or entered-PIN storage. Legacy masked records preserved for compatibility; retention tests passed. |
| PL-08 | High | Lifecycle handling, permission status, profile-aware authoritative lock query, owner-safe exclusions and degraded warning implemented. Actual foreground/permission integration is still a release gate. |
| PL-09 | Medium | Corrected instrumentation application ID and validated API 24/36, including internal branding at `a445dcde`. |
| PL-10 | High | Export limited to appearance preferences; strong per-operation biometric key and PIN-authorized enrollment implemented. Physical enrollment, invalidation and recovery remain open. |

Android accessibility-based overlays are not an operating-system security boundary. Device owners must retain control of permissions, settings, emergency functions and uninstall. Do not distribute as a production security application until critical and high findings are resolved and validated on devices.

This current register supersedes the historical milestones below, which are retained as chronological evidence. Applicable [OWASP MASVS](https://mas.owasp.org/MASVS/) groups include STORAGE, CRYPTO, AUTH, PLATFORM, CODE and PRIVACY. Passing CodeQL and unit tests is not a full MASVS assessment or a claim of certification.

## CI evidence

- Android CI run 123 passed at commit `8a59c591e22c6421471785cd7046b81392779cf0` (both debug flavors, lint and unit tests). Subsequent changes require a fresh run.
- CodeQL run 49 compiled and produced SARIF, but GitHub rejected upload because default setup is enabled while the advanced workflow is configured. Repository configuration must be reconciled; this is not a passed scan.
- Dependency Review run 23 failed because Dependency Graph is not enabled for this repository. Keep the check enabled and resolve repository settings before release.

## Credential remediation update

- New PIN credentials use versioned PBKDF2 records, unique random salts, a constant-time comparison and background derivation. API 26+ uses HMAC-SHA256; API 24–25 explicitly use the platform HMAC-SHA1 PBKDF2 provider with 1.3 million rounds. No fallback on provider errors. Existing valid legacy SHA-256 records are migrated after successful verification, using a conditional database update. Legacy verifier remains solely for migration. This does not establish encrypted or Keystore-backed storage.
- PIN enrollment requires matching entries; changing an existing primary PIN requires the current PIN. Decoy and panic configuration controls were removed from the settings screen.
- Credential derivation regression tests added; CI execution and device validation pending. Persistent throttling and Keystore integration remain unresolved.
- Android CI run 131 and Dependency Review run 25 passed on the previous commit; CodeQL advanced upload still conflicts with default setup. Do not treat new commits as verified until their own workflows complete.

## Persistent authentication gate and Keystore update

- A single process-wide mutex serializes PIN attempts across both authentication surfaces. Persisted attempt counters and elapsed-realtime cooldowns survive process termination; boot identifiers reset pending cooldown duration on reboot. Devices without a readable boot identifier conservatively restart the pending cooldown after process recreation. The counter is written before verification, and a successful PIN clears it only after persistence succeeds.
- New and successfully verified legacy credential records are wrapped in Android Keystore AES-GCM. Missing or invalid keys deny verification. Hardware-backed key use is device-dependent and not asserted. Database encryption remains a separate unresolved requirement.
- Added policy unit tests and device tests for cooldown persistence and Keystore tamper rejection, as well as API 24/36 emulator CI. New tests and builds are pending; device reboot and physical hardware behavior remain unverified.
- Android CI run 148 and Dependency Review run 30 passed on commit `1fb176272510b71647541648e56579e37f84d703`. CodeQL run 56 failed. New changes require fresh validation.

## Encrypted database and protection lifecycle update

- SQLCipher-backed Room storage with a separate randomly generated database passphrase wrapped by an Android Keystore AES-GCM key. Version-2 plaintext records are copied transactionally and compared before a durable migration marker is written and the old database is retired. Unsupported legacy schema versions or missing keys fail without destructive fallback. Migration, startup performance, backup and physical-device behavior require validation. The initial migration blocks database availability while running on an I/O dispatcher; large-data startup timing is not yet measured.
- Added Android device test for encrypted file header, legacy records and reopening. CI execution pending.
- Accessibility service now queries the authoritative protection row, cancels stale foreground queries, clears sessions on lifecycle events, handles changed lock targets and excludes system/launcher packages from new protection. Accessibility events are not an OS-level security boundary; rapid switching, multi-window, recents and OEM behavior remain unverified. Owner controls remain accessible.
- Removed unnecessary Foojay toolchain resolver after API 36 job failed to resolve it. API 24 instrumentation passed on previous commit; latest run pending. Android CI and Dependency Review passed at commit 4fe191eea0dc1d485b1e3f42f86afacd83d42eb7. CodeQL upload still conflicts with repository default configuration.
- Product language remains English; Sinhala and Tamil localization removed from scope at owner request.

## Genuine UI and release validation follow-up

- Emulator run 17 passed four instrumentation tests on both API 24 and API 36 at `f00b829a74e0571c644a02b99a8c9444aacd22be`, including the encryption round-trip/migration test. Later changes require fresh validation.
- Removed the simulated sandbox and numerical privacy score. The new dashboard uses actual eligible applications, real launch intents and a connected-service status, requires PIN authentication for protection changes and shows a degraded-protection warning. Startup errors no longer instruct users to erase data.
- App discovery updates metadata without replacing protection or pinning settings. New eligible apps can be selected automatically when the owner enables that setting. Event retention is explicit (keep all by default); older records are removed only when a user chooses a retention cap.
- Strong biometric enrollment now requests the current PIN and uses a per-operation Android Keystore key that is invalidated when enrollment changes. Physical biometric and OEM behavior remain unverified.
- Removed unnecessary overlay/usage/foreground/boot/package-wide permissions and unused networking dependencies; query visibility is scoped to launchable apps and home handlers.
- Added an unsigned optimized release validation workflow and artifact checksums; no production signing keys or release publication are assumed.
- CodeQL default setup reports a Kotlin error and rejects advanced workflow SARIF. Switching configurations requires a repository setting change; the advanced workflow remains in place. Dependency Review passed, secret scanning and push protection are enabled. Do not bypass required security checks.
- The final rebranding remains gated on current build/security results. Localization scope was reintroduced by the latest directive; reviewed translations have not been implemented.

## Validation and security follow-up

At commit `56a2582c65385cb453efe790e783378fbae5f91c` all five PR workflows completed successfully: Android CI (212), dependency review (56), unsigned release validation (4), emulator matrix API 24/36 (22, after retry), and advanced CodeQL (82, after repository configuration migration). CodeQL reports successful SARIF upload; successful execution alone does not establish absence of alerts.

Subsequent commits add PIN authorization before sensitive settings changes, transaction-based updates to avoid stale configuration overwrites, and an end-to-end upgrade test covering an interrupted migration, persisted key, data preservation, legacy cleanup and missing key rejection. The test is pending execution on the new commit. Dead decoy and panic configuration methods have been removed, while legacy database columns remain for data migration compatibility. Sinhala and Tamil translations are initially provided for dashboard, navigation, startup and security-events surfaces; other screens and linguistic review are outstanding.

Repository name lookup for `indrajithbandara/Untouched` returned not found with the configured GitHub account; the name appears available but this is not a trademark clearance. Public rebranding and repository rename remain gated on final validation and brand review.

Removed a stale UI message claiming a default PIN existed, disabled authentication input before enrollment, and replaced simulated brand-specific fallback icons with a neutral icon. The historical credential verifier exists only as a read-only migration path. Production release validation now has a separate main-only, environment-scoped signed workflow that requires the existing key and an independently verified certificate fingerprint; it has not been executed with production credentials.
