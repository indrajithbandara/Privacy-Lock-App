# Engineering requirement verification

This matrix records implementation separately from execution evidence. The last fully green historical baseline was commit `56a2582`; commit `9462adbe` passed dependency, release, CodeQL and emulator tests but its debug unit-test compilation failed due to a renamed icon component in a screenshot test. Fix `6bc2080` updated the test; the subsequent CI reached lint and reported missing translations. The product name is intentionally non-translatable and the accessibility disclosure has been translated. A fresh full run is required.

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
| Localization | English, Sinhala and Tamil dashboard, navigation, events, authentication and settings resources; localized accessibility disclosure | Linguistic review and layout validation outstanding; historical free-text event descriptions remain as originally recorded |
| Release | Optimized unsigned APK/AAB; restricted signed validation workflow | Unsigned run 9 succeeded; production signing and certificate match not executed |
| CodeQL and dependencies | Advanced CodeQL and Dependency Review workflows | Both passed at commit `9462adbe`; latest rerun required; no independent audit claim |
| Branding and repository rename | Debug-only Untouched: Privacy Lock identity, launcher vectors and SVG/PNG assets | Debug identity test added; public rename and release remain gated by material potential conflicting mobile security app; see [brand review](brand-availability.md) |

## Latest observed check results

At `5dc66145`, API 24 and API 36 instrumentation both passed (run `45623778499`; API 24 executed 12 tests). The retry on `162f57a` also passed API 24, so the earlier installation failure was transient; the available failure output does not establish its precise underlying cause. The minimum SDK remains 24. Android CI run `45623778430` failed because the debug Robolectric test expected the previous launcher label; the assertion has been updated to match the approved internal brand.

Protection profiles now use encrypted schema version 3 with an additive migration. Active profile app selections are combined with individual locks by the same database query used by the service. Profile changes require PIN confirmation, and profile management clears temporary sessions. Profile-specific timeouts, genuine third-party fixture interaction, physical-device validation and upgrade from a previously signed APK remain unverified or incomplete. The profile instrumentation tests cover union semantics and encrypted reopening, not actual foreground transitions.

Positive relock timeouts now schedule a cancellable foreground-session expiry check rather than waiting exclusively for an additional window event. Android background-activity restrictions and missing/ambiguous window events still limit assurance; physical-device and foreground fixture testing remain required.

All five workflows passed on commit `a445dcde` (Android CI `45626819430`, API 24/36 `45626819479`, CodeQL `45626819423`, dependency review `45626819542`, unsigned release `45626819471`); 15 instrumentation tests ran on each API. A separate emulator-only fixture APK and a real foreground protection scenario have been added next, using the visible Android Settings consent UI. This new scenario must itself pass before claiming end-to-end protection. Removed unused legacy stealth-code generation and obsolete permission helpers; the legacy columns remain to preserve existing database rows.

On `b989bf41`, release validation, Android CI, Dependency Review and CodeQL passed, but the new foreground test failed when interacting with the Android Settings service row on both APIs. The consent automation now resets the Settings task, retries visible actions and uses a touch fallback for non-clickable accessibility labels. It retains all assertions and surfaces primary failures independently of cleanup failures. The actual foreground test remains a release gate.

At `162f57a19fb6e5f64f347fedb62da6ae0aeb017e`, Android CI (`45619362717`), CodeQL (`45619362477`), Dependency Review (`45619362548`) and unsigned release validation (`45619362620`) succeeded. Emulator run `45619362656` succeeded on API 36 but API 24 stopped before executing tests at APK installation. No functional regression was established by that failure. A retry and an explicit emulator data partition size with storage diagnostics were initiated. All subsequent source changes require their own full validation.

No production release or main-branch merge is authorized until the applicable acceptance criteria are actually verified. Physical device/OEM compatibility and production signing remain external dependencies.
