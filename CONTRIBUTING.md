# Contributing to Privacy Lock

We are excited that you want to contribute to **Privacy Lock**! To maintain a production-grade, secure, and performant enterprise utility, please review the guidelines below before proposing changes.

---

## 📜 Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

---

## 🔄 Development Workflow

We follow a standard feature branch workflow:

1. **Fork & Clone**: Fork the repository on GitHub and clone it to your local environment.
2. **Create Branch**: Create a feature or bugfix branch:
   - Features: `feature/your-feature-name`
   - Bugfixes: `bugfix/issue-description`
   - Security: `security/mitigation-name`
3. **Implement Changes**: Add your code adhering to our Kotlin, Compose, and database standards.
4. **Local Verification**: Ensure compilation and run JVM-level unit/Robolectric tests before submitting:
   ```bash
   ./gradlew compileDebugSources :app:testDebugUnitTest
   ```
5. **Atomic Commits**: Keep commits small, self-contained, and structured (see [Commit Style](#commit-message-guidelines) below).
6. **Push & Pull Request**: Push changes and open a Pull Request against the `main` branch, filling out the Pull Request Template.

---

## 🛠️ Coding Standards

### 1. Kotlin & Jetpack Compose
* **Architecture**: Follow clean Model-View-ViewModel (MVVM) principles with single sources of truth (e.g. SQLite database synced via Repository).
* **State Management**: Use `StateFlow` and collect states using `collectAsStateWithLifecycle()` inside composables to ensure lifecycle awareness.
* **Dependency Injection**: Use standard constructor injection.
* **Responsive Layouts**: Do not use hardcoded widths or heights. Rely on fluid constraints (`fillMaxWidth()`, etc.) and Material 3 design tokens.

### 2. UI Testability (TestTags)
* All interactive and key visual elements **MUST** specify a unique `testTag`.
* Use `snake_case` naming for test tags.
  ```kotlin
  Button(
      onClick = { /* ... */ },
      modifier = Modifier.testTag("submit_button")
  ) {
      Text("Submit")
  }
  ```

### 3. Resource Management
* **Strings**: Do not hardcode user-facing strings. Add them to `res/values/strings.xml` with descriptive lowercase IDs.
* **Icons**: Utilize Google Material Symbols. Favor `Icons.AutoMirrored` for directional items.

---

## 🛡️ Security Practices (MANDATORY)

Since Privacy Lock is a security-first utility, every contributor must follow these rules:
1. **Never Hardcode Credentials**: All configuration secrets must reside in Gradle-injected properties or environment setups.
2. **Fail Securely**: All security-critical operations (PIN validation, database queries) must fail closed. If an exception occurs, deny access by default.
3. **Cryptographic Randomness**: Always use `java.security.SecureRandom` instead of `kotlin.random.Random` for cryptographic features, keypad shuffling, or key generation.
4. **Data Isolation**: Local databases must never store plaintext PINs, passwords, or keys. Always store salted SHA-256 hashes with secure salts.

---

## 📝 Commit Message Guidelines

We encourage the **Conventional Commits** format:

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

* **feat**: A new feature (e.g., `feat(auth): add biometric lock screen fallback`)
* **fix**: A bug fix (e.g., `fix(keypad): center 0 in bottom row of scrambled keypad`)
* **docs**: Documentation updates (e.g., `docs(readme): link to FAQ and Roadmap`)
* **style**: Code style changes (whitespace, formatting, missing semi-colons, etc.)
* **refactor**: Code changes that neither fix a bug nor add a feature
* **perf**: Performance improvements
* **test**: Adding or correcting tests

---

## 📋 Pull Request Checklist

Before submitting your Pull Request, ensure that:
- [ ] The app builds successfully with `./gradlew compileDebugSources`.
- [ ] All unit and Robolectric tests pass.
- [ ] No hardcoded passwords, private keys, or API tokens are checked in.
- [ ] No debug code (e.g. `Log.d` with sensitive information or unfinished mock files) is present.
- [ ] All interactive UI components have a unique `testTag` for automated UI tests.
- [ ] The PR title follows Conventional Commits formatting.
