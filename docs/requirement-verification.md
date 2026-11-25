# Engineering requirement verification

This matrix records implementation separately from execution evidence. The last fully green historical baseline was commit `56a2582`; commit `9462adbe` passed dependency, release, CodeQL and emulator tests but its debug unit-test compilation failed due to a renamed icon component in a screenshot test. Fix `6bc2080` updates the test; a new run is required.

| Requirement | Implementation | Evidence / remaining work |
| --- | --- | --- |
| PIN KDF, random salt, constant-time verification | Versioned PBKDF2; API 24–25 compatibility; legacy migration only | Unit and device tests on prior baselines; latest regression pending |
| Android Keystore credential and database encryption | AES-GCM wrapped PIN verifier and separate SQLCipher passphrase | Encryption, migration and reopening tests passed on API 24/36 in run 27 at `9462adbe` |
| Interrupted legacy upgrade and missing key | Transactional copy, migration marker, key recovery rejection | Device test added; run 27 passed; full physical upgrade unverified |
| Brute-force resistance | Shared persistent gate, elapsed time delay and reconnect policy | Device and unit tests; reboot, clock and physical OEM validation outstanding |
| Biometrics | Strong per-operation key and PIN-authorized enrollment | Physical enrollment/invalidation behavior remains unverified |
| Application locks | Accessibility events, eligible apps only, session state, current DB query | Session tests pass on previous builds; actual app-switch and OEM behavior require additional validation |
| Permission and owner control | Device owner may revoke; launcher/system apps excluded; degraded status | Owner-control regression tests; real-time revocation and multi-window remaining |
| Security events | Real PIN failure/lockout, local retention and explicit deletion | Device retention test; encryption backed by SQLCipher |
| Installed app discovery | Scoped launcher visibility, search and filters, metadata-only updates | Repository tests; install/remove edge cases and individual profiles remain incomplete |
| UI and accessibility | Material 3 navigation, honest dashboard, secure dialog semantics | MainActivity navigation and unenrolled keypad instrumented tests added; execution pending. Not a complete accessibility audit |
| Localization | English, Sinhala and Tamil initial dashboard, navigation, events and authentication resources | Native linguistic review, full settings and layout validation outstanding |
| Release | Optimized unsigned APK/AAB; restricted signed validation workflow | Unsigned run 9 succeeded; production signing and certificate match not executed |
| CodeQL and dependencies | Advanced CodeQL and Dependency Review workflows | Both passed at commit `9462adbe`; latest rerun required; no independent audit claim |
| Branding and repository rename | Proposed name only | Material potential conflicting mobile security app; see [brand review](brand-availability.md) |

No production release or main-branch merge is authorized until the applicable acceptance criteria are actually verified. Physical device/OEM compatibility and production signing remain external dependencies.
