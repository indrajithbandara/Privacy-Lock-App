# Release process

No release may be published until the applicable build, lint, unit, instrumentation, dependency and CodeQL checks pass on the exact release commit and the outstanding critical/high security findings are resolved.

The release validation workflow builds optimized **unsigned** APK and AAB artifacts for both distribution flavors, generates SHA-256 checksums, and uploads them for validation only. It does not publish a release.

## Authorized signing

The development release candidate is `2.0.0-rc.3` (version code `4`, after legacy version code `1`, RC.1 version code `2` and RC.2 version code `3`). It is not a published production release. Verify the signing identity and an actual upgrade before final public release.

Use the existing production signing key. Configure the Gradle environment variables `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` in a restricted release environment. The configured key alias is `upload`. Do not commit or disclose the key or passwords. Do not generate a replacement signing identity merely to make a build pass.

The `signed-release-validation.yml` workflow is manually dispatched on `main` only and uses the `release` GitHub environment. Configure that environment with `KEYSTORE_BASE64`, `STORE_PASSWORD`, and `KEY_PASSWORD` secrets and the public certificate fingerprint variable `EXPECTED_SIGNING_CERT_SHA256`. The workflow rejects absent credentials or a certificate mismatch and uploads signed artifacts for review; it does not publish a release.

After signing, verify each APK with Android SDK `apksigner verify --verbose --print-certs` and compare the certificate fingerprint against the known existing signing identity. Verify the signed AAB with appropriate Java/Android signing tools. Validate SHA-256 checksums, version code/name and update compatibility.

Publish only verified signed artifacts and accurate release notes after all gates pass. Keep unsigned and debug artifacts out of production releases. Google Play and F-Droid approval are separate external processes.
