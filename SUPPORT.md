# Support Guidelines

Thank you for using Privacy Lock! Please read through these support guidelines to understand the official communication channels and issue resolution procedures.

---

## 📞 Support Channels

To ensure structured resolution, we separate issues based on their nature:

| Issue Type | Recommended Channel | Response SLA |
| :--- | :--- | :--- |
| **Bugs / Crashes** | [GitHub Issues](https://github.com/example/privacy-lock/issues) | 3–5 Business Days |
| **Feature Requests** | [GitHub Discussions](https://github.com/example/privacy-lock/discussions) | Weekly Triage |
| **Security Exploits** | Email to: **indrajithbandarasl@gmail.com** | < 48 Hours |
| **General Q&A** | [GitHub Discussions (Q&A Category)](https://github.com/example/privacy-lock/discussions/categories/q-a) | Community-driven |

---

## 🛠️ Bug Reporting & Triage

When reporting a bug, please use our standard **Bug Report Template** and provide the following details:
1. **Device Model**: e.g., Google Pixel 7a
2. **Android Version**: e.g., Android 14 (API 34)
3. **App Version**: e.g., v1.0.0
4. **Steps to Reproduce**: Detailed list of steps.
5. **Expected vs. Actual Behavior**: Explain clearly what happened.
6. **Logcat / Crash Logs**: If available, attach non-sensitive stack traces.

### The Triage Process
1. **New**: Issue is submitted and awaits maintainer triage.
2. **Needs Info**: If reproduction steps or device details are missing, we will ask for more context.
3. **Accepted / Backlog**: The bug is verified and scheduled for development.
4. **In Progress**: A contributor or maintainer is actively working on a fix.
5. **Resolved**: A patch is merged into the `main` branch and scheduled for the next release.

---

## ⚠️ Known Platform Limitations

Before opening a new ticket, please confirm if your issue matches these known system behaviors:

1. **Accessibility Service Termination**: Modern Android battery savers (such as Samsung's Device Care or Xiaomi's MIUI battery manager) may occasionally terminate background services. This is a system-level constraint and can only be resolved by configuring **Unrestricted** battery access for Privacy Lock.
2. **Android Go / Low RAM Devices**: In-memory caching and rapid task switching are constrained on devices with under 2GB RAM, which may slow down the overlay launch speeds.
