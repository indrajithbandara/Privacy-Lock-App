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
