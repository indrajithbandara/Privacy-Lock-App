# Security baseline (development branch)

This is an incomplete remediation, not a production release or independent audit.

| Finding | Severity | Status |
| --- | --- | --- |
| PL-01 | Critical | Removed implicit default PIN and hashCode fallback. Existing credential verification still needs secure enrollment and modern key derivation. |
| PL-02 | Critical | Removed decoy credential unlock and require a configured PIN for new lock preferences. Authentication authorization and persistent throttling remain incomplete. |
| PL-03 | High | Removed release-signing fallback and automatic publication. Signing and artifact validation remain pending. |
| PL-04 | High | Room database is not encrypted. Removed destructive migration fallback; explicit legacy migrations and encryption remain necessary. |
| PL-05 | High | Session expiry changed to monotonic elapsed time; device tests pending. |
| PL-06 | High | Removed fake app seed list and obsolete demo records when absent from successful discovery. Other presentation simulations remain. |
| PL-07 | High | Replaced new simulated avatar records with authentication failure events and a genuine event list. Legacy records remain in the database; deletion, retention and build verification remain pending. |
| PL-08 | High | Protection lifecycle and permission handling remain incomplete. |
| PL-09 | Medium | Corrected instrumentation test application ID; execution pending. |
| PL-10 | High | Authentication configuration backup and biometric enrollment need secure design. |

Android accessibility-based overlays are not an operating-system security boundary. Device owners must retain control of permissions, settings, emergency functions and uninstall. Do not distribute as a production security application until critical and high findings are resolved and validated on devices.

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
