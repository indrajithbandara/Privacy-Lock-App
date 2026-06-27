# CI/CD Pipeline & GitHub Actions

Privacy Lock leverages an automated **GitHub Actions CI/CD pipeline** to guarantee build stability, verify code quality, execute tests, and package releases on every push or pull request.

---

## 🚀 1. GitHub Actions Pipeline Design

The complete continuous integration workflow is defined under `.github/workflows/main.yml`. It triggers automatically on:
* **Pushes** targeting the `main` or `develop` branch.
* **Pull Requests** opened against the `main` or `develop` branch.

```
       [ Push / Pull Request Event ]
                     |
                     v
+------------------------------------------+
| 1. Environment Setup                     |
|    - Checkout code                       |
|    - Setup JDK 17                        |
|    - Restore Gradle cache                |
+------------------------------------------+
                     |
                     v
+------------------------------------------+
| 2. Quality & Validation                  |
|    - Lint checks (gradle :app:lintDebug) |
|    - Run Unit & Robolectric Tests        |
+------------------------------------------+
                     |
                     v
+------------------------------------------+
| 3. Compilation & Packaging               |
|    - Assemble Debug APK                  |
|    - Assemble Release APK                |
|    - Bundle Release AAB                  |
+------------------------------------------+
                     |
                     v
+------------------------------------------+
| 4. Artifact Management                   |
|    - Upload compiled APKs & AABs         |
|    - Archive test results & lint reports |
+------------------------------------------+
```

---

## 🛠️ 2. Workflow Jobs & Commands

The automated pipeline executes four sequential jobs inside an isolated virtual runner (`ubuntu-latest`):

### 2.1 Code checkout & JDK Setup
```yaml
- name: Checkout Repository
  uses: actions/checkout@v4

- name: Set up Java Development Kit (JDK) 17
  uses: actions/setup-java@v4
  with:
    distribution: 'zulu'
    java-version: '17'
```

### 2.2 Gradle Dependency Cache Restoration
Speeds up workflow runtimes by caching compiled dependencies, plugins, and Gradle files between builds:
```yaml
- name: Cache Gradle Packages
  uses: actions/cache@v4
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
    restore-keys: |
      ${{ runner.os }}-gradle-
```

### 2.3 Quality Controls & Tests
Executes code analysis and runs unit tests using Robolectric local execution:
```yaml
- name: Run Linter Checks
  run: gradle :app:lintDebug

- name: Run Unit Tests
  run: gradle :app:testDebugUnitTest
```

### 2.4 Build and Package Artifacts
Generates final APK and Android App Bundle packages, then archives them as downloadable artifacts:
```yaml
- name: Build Application Artifacts
  run: |
    gradle :app:assembleDebug
    gradle :app:assembleRelease
    gradle :app:bundleRelease

- name: Upload Build Artifacts
  uses: actions/upload-artifact@v4
  with:
    name: privacy-lock-build-artifacts
    path: |
      app/build/outputs/apk/debug/app-debug.apk
      app/build/outputs/apk/release/app-release-unsigned.apk
      app/build/outputs/bundle/release/app-release.aab
```

---

## 🤖 3. Dependabot Dependency Upgrades

To maintain library security, Privacy Lock incorporates a standard Dependabot configuration under `.github/dependabot.yml`.

### Dependabot Workflow & Upgrade Guards
1. **Weekly Scans**: Dependabot scans `libs.versions.toml` and Gradle scripts every Monday for updated library releases.
2. **Compile SDK Check**: If a dependency update (such as `androidx.lifecycle`) is proposed, the pipeline evaluates the required `compileSdk` target. 
3. **Guard Actions**: If a dependency update fails due to a SDK mismatch (e.g., requesting `compileSdk 37` while the project is on `compileSdk 36`), the CI run fails, alerting the engineering team.
4. **Resolution Strategy**: The maintainer can choose to:
   * Keep the current SDK stable and pin/downgrade the dependency to the last compatible version (Option A).
   * Safe-upgrade the entire project targetSDK and compileSDK environments to compileSdk 37 while verifying Gradle, Kotlin, KSP, and Compose compiler compatibility (Option B).

---

[[Home]] | [<< Build Instructions](Build-Instructions) | [[Troubleshooting >>](Troubleshooting)]
