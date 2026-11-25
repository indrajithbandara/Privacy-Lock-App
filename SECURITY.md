# Security policy

This Android app is a local, accessibility-based privacy utility, not an OS-level security boundary. Please do not rely on it to protect against a compromised operating system, rooted devices, or a device owner changing system permissions.

The current development branch implements salted PBKDF2, Keystore-encrypted credential records, lockout counters, SQLCipher storage and per-use biometric keys. See [the security findings register](docs/security-baseline.md) for current evidence, outstanding limitations and release blockers.

## Reporting

Please report security concerns privately to indrajithbandarasl@gmail.com. Include the affected version, device/Android version, observed behavior and relevant logs with personal information and credentials removed. Do not obtain or disclose other people's data.

Do not publish unverified claims about independent audits or OWASP certification.
