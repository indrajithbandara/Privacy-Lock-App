# Security Policy

We take the security of Privacy Lock seriously. If you are a security researcher or developer and identify a vulnerability, please read this policy to report your findings responsibly.

---

## 1. Supported Versions

Only the latest release version of Privacy Lock is actively supported with security patches and updates. We recommend all users keep their installations up to date to ensure they are protected.

| Version | Supported | Notes |
| :--- | :--- | :--- |
| **v1.2.x** | ✅ Yes | Current release. Active security updates are pushed directly to this branch. |
| **v1.1.x** | ❌ No | Legacy release. Please upgrade to the latest version to receive security fixes. |
| **v1.0.x** | ❌ No | Legacy release. Unsupported. |

---

## 2. Reporting a Vulnerability

### 🚨 Do NOT open public GitHub Issues for security bugs.
Publishing security vulnerabilities in public issues exposes users to active exploits before a patch can be developed and distributed.

### Reporting Procedure
1. Send a detailed report of the vulnerability to the maintainers at **security-reporting@example.com** (use GPG encryption if possible).
2. Include the following details in your report:
   * **Vulnerability Type**: (e.g., PIN bypass, SQL injection, memory leak, privilege escalation).
   * **Impact**: A description of the potential exploit scenario and what an attacker could achieve.
   * **Steps to Reproduce**: A clear, step-by-step guide, proof-of-concept code, or ADB commands to reproduce the issue.
   * **System Configurations**: The Android OS version and device models where the exploit was successfully executed.

---

## 3. Our Response & Responsible Disclosure

* **Acknowledgment**: We aim to acknowledge and reply to security reports within **48 hours** of receipt.
* **Evaluation**: The security team will evaluate and reproduce the reported issue to confirm its severity.
* **Patch Development**: If confirmed, we will develop a patch and test it across our CI pipeline. We aim to release a hotfix within **14 days** for critical issues.
* **Public Disclosure**: Once a patch is released and distributed to users via F-Droid and the Google Play Store, we will publicly disclose the vulnerability and credit the reporting researcher in our release notes.

---

[[Home]] | [<< Privacy Policy](Privacy-Policy)
