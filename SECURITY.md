# Security Policy

## 📋 Supported Versions

Only the latest release version of **Privacy Lock** receives security updates.

| Version | Supported |
| ------- | --------- |
| 1.0.x   | :white_check_mark: |
| < 1.0.0 | :x: |

---

## 🔒 Secure Engineering Standards

Privacy Lock is engineered with "Secure by Default" core philosophies:

1. **Local Hashed Authentication**: PIN/Password hash values are securely salted using standard SHA-256 with a `java.security.SecureRandom` dynamic salt block. Plaintext credentials are never stored.
2. **Window Shielding**: App screens utilize `FLAG_SECURE` window properties dynamically to inhibit system-level screenshots, background task snapshots, and external screen recordings of secure operations.
3. **Excluded Backup Domains**: Explicit `backup_rules.xml` and `data_extraction_rules.xml` configurations exclude local databases (`privacy_lock_db`) from cloud backups and device-to-device file transfers to protect stored security logs from target side-loading.
4. **Weak RNG Protection**: `java.security.SecureRandom` is used exclusively over standard linear pseudorandom generators (PRNGs) for secure dial generation, decoy selection, and key generation.

---

## 📬 Reporting a Vulnerability

**DO NOT open a public GitHub issue for security vulnerabilities.**

If you discover a security vulnerability or a local bypass mechanism in this project, please report it privately via email to the security team at **indrajithbandarasl@gmail.com**.

Please include:
* **Description**: Detailed description of the vulnerability.
* **Steps to Reproduce**: Detailed steps (with a PoC if possible) to reproduce the vulnerability.
* **Potential Impact**: The impact of the vulnerability on user privacy.

We aim to acknowledge your report within **48 hours** and provide a patch or mitigation within **30 days** of validation.

---

## 🤝 Responsible Disclosure

We ask that you follow responsible disclosure principles:
* Give us reasonable time to investigate and fix the issue before publishing details.
* Do not exploit the vulnerability (e.g., extracting unauthorized user tokens or manipulating locked apps).
* Do not disclose the vulnerability publicly until a patch/mitigation has been officially released.
