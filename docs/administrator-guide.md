# Privacy Lock Administrator Manual

This administrator guide outlines repository maintenance practices, security release policies, automation rules, and project governance standards for the **Privacy Lock** project.

---

## 🛠️ 1. Repository Maintenance & Governance

Privacy Lock is an open-source security application. Project administrators must maintain strict quality controls over the codebase:

### Branch Policies
* **Branch Protection**: The `main` branch is protected. Direct pushes are disabled.
* **Pull Request Requirements**: All changes must be proposed via Pull Requests. PRs require:
  * At least two approvals from core maintainers.
  * Successful completion of the GitHub Actions build and test suite.
  * No unresolved discussions or comments.

---

## 🤖 2. Managing Dependabot Updates

Automated dependency updates are managed via `.github/dependabot.yml` on a weekly schedule.

### Administrator Procedures for Dependabot PRs:
1. **Audit Security Advisories**: When Dependabot submits a dependency upgrade, review the corresponding CVE reports on the GitHub Security Advisory database.
2. **Review Compatibility**: Check the automated CI test results for the PR.
3. **Verify KSP & Kotlin Versions**: Kotlin compiler and KSP (Kotlin Symbol Processing) versions are tightly coupled. Do not allow Dependabot to upgrade Room or Moshi dependencies without checking Kotlin compiler compatibility first.
4. **Merge Strategy**: Merge approved updates using squash merging to maintain a clean git history.

---

## ⚙️ 3. Managing GitHub Actions Workflows

Continuous Integration (CI) is configured via `.github/workflows/main.yml`. Administrators must periodically monitor and update this pipeline:

* **Runner Updates**: The pipeline runs on `ubuntu-latest`. Check runner logs quarterly to ensure compatibility with newer Android Gradle Plugin (AGP) versions.
* **JDK Versioning**: The build environment uses JDK 17. If upgrading the Kotlin compiler to versions requiring newer Java runtimes, update the `actions/setup-java` configuration in the workflow file.
* **Secrets Management**: Safe release keys are stored in the repository settings under `Settings -> Secrets and Variables -> Actions`. Protect these values and rotate them if a compromise is suspected.

---

## 📋 4. Issue & Pull Request Management

Administrators coordinate issue triaging and peer reviews using structured templates:

* **Issue Templates**: Found in `.github/ISSUE_TEMPLATE/`. Maintain templates for:
  * Bug Reports (requires system details, steps to reproduce, and logs).
  * Feature Proposals (requires descriptive use-cases and mockups).
  * Security Advisory (instructs reporters to send vulnerabilities privately instead of opening public issues).
* **Pull Request Templates**: Configured in `.github/PULL_REQUEST_TEMPLATE.md`. Enforces detailed descriptions, verification checklists, and linkage to open issues.

---

## 🗂️ 5. Managing Project Board Views (Projects V2)

Project statuses are tracked on the **Privacy Lock App Roadmap** board. Maintainers should keep board views organized using the following custom configurations:

* **Board Layout**: Organized by status columns (`📋 Backlog`, `📝 Planned`, `🚀 Ready for Development`, `🚧 In Progress`, `👀 Code Review`, `🧪 Testing`, `⏳ Ready for Release`, `✅ Done`, `🎉 Released`).
* **Metadata Fields**: Keep custom single-select fields updated:
  * **Priority**: `Critical`, `High`, `Medium`, `Low`.
  * **Risk**: `Low`, `Medium`, `High`, `Critical`.
  * **Component**: `Application Lock`, `Accessibility Service`, `Biometrics`, etc.
  * **Complexity**: `Small`, `Medium`, `Large`, `Epic`.
* **Sprints & Schedulers**: Archive cards in the `🎉 Released` column monthly to keep the main views clean and responsive.

---

## 📖 6. Wiki Maintenance & Updates

The GitHub Wiki houses community-facing documentation. Keep the wiki updated by following these guidelines:

1. **Keep Links Intact**: Ensure sidebar navigation items link correctly to corresponding wiki files in the `/wiki` directory.
2. **Synchronize Content**: When code refactorings modify database models, accessibility methods, or user settings, update the wiki pages to match.
3. **Keep It Specific**: Ensure documentation clearly reflects the actual code and architecture, keeping the technical details accurate.

---

## 🏷️ 7. Semantic Versioning & Release Cycles

Privacy Lock follows **Semantic Versioning (SemVer)** specifications: `MAJOR.MINOR.PATCH` (e.g., `1.1.2`):

* **MAJOR**: Architectural overhauls, high-risk security updates, or database changes requiring destructive migrations.
* **MINOR**: New features (e.g., biometric support or landscape layouts) introduced without breaking backwards compatibility.
* **PATCH**: Secure bug fixes, UI adjustments, package size optimizations, or localized string updates.

### Publishing Releases:
1. Run local Robolectric and Roborazzi suites to verify code stability.
2. Tag the target commit on the `main` branch:
   ```bash
   git tag -a v1.1.0 -m "Release version 1.1.0"
   ```
3. Push the tag to GitHub to trigger the automated release pipeline.
4. Publish the signed APK, update the release notes, and post SHA-256 checksums on the GitHub Releases page.
