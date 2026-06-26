# Contributing to Privacy Lock

We are excited that you want to contribute to **Privacy Lock**! To maintain a production-grade, secure, and performant enterprise application, please review the guidelines below.

---

## Code of Conduct

By participating in this project, you agree to abide by our [Code of Conduct](CODE_OF_CONDUCT.md).

---

## Development Workflow

We follow a typical feature branch workflow:

1. **Fork & Clone**: Fork the repository and clone it to your local machine.
2. **Create Branch**: Create a feature or bugfix branch:
   - Features: `feature/your-feature-name`
   - Bugfixes: `bugfix/issue-description`
   - Security: `security/mitigation-name`
3. **Write Code**: Implement your changes adhering to our Kotlin and Compose standards.
4. **Test Your Changes**: Verify compilation and run JVM-level unit/Robolectric tests before submitting.
5. **Commit**: Keep commits small and use professional, descriptive commit messages (see [Commit Style](#commit-message-guidelines) below).
6. **Push & PR**: Push changes and open a Pull Request against the `main` branch.

---

## Coding Standards

### 1. Kotlin & Jetpack Compose
- **Architecture**: Follow the clean Model-View-ViewModel (MVVM) architecture with single sources of truth (e.g., Room database).
- **State Management**: Use `StateFlow` or `MutableState` and collect states using `collectAsStateWithLifecycle()` to remain lifecycle-aware.
- **Dependency Injection**: Use simple constructor injection where possible.
- **Modifiers**: Keep layouts flexible. Always avoid hardcoded heights and widths. Prefer container bounds (`fillMaxWidth()`, etc.) and Material 3 design tokens.

### 2. UI Testability (TestTags)
- Interactive and key visual elements **MUST** specify a unique `testTag`.
- Use `snake_case` naming for test tags.
  ```kotlin
  Button(
      onClick = { /* ... */ },
      modifier = Modifier.testTag("submit_button")
  ) {
      Text("Submit")
  }
  ```

### 3. Resource Management
- **Strings**: Do not hardcode user-facing strings. Add them to `res/values/strings.xml` with descriptive lowercase IDs.
- **Icons**: Utilize Google Material Symbols. Favor `Icons.AutoMirrored` for directional items.

---

## Security Practices (Mandatory)

Since **Privacy Lock** is a security-first utility, every contributor must follow these rules:
1. **Never Hardcode Credentials**: All keys, secrets, or configurations must reside in environment configurations or gradle-injected properties.
2. **Fail Securely**: All security-critical operations (PIN validation, database access, Keystore encryption) must fail closed. If an exception occurs, deny access by default.
3. **Randomness**: Always use `java.security.SecureRandom` instead of `kotlin.random.Random` for cryptographic features or code generation.
4. **Data Privacy**: Local caches or logs must never store plaintext user PINs, passwords, or personal keys.

---

## Commit Message Guidelines

We encourage the **Conventional Commits** format:

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

- **feat**: A new feature (e.g., `feat(auth): add biometric lock screen`)
- **fix**: A bug fix (e.g., `fix(db): resolve thread block during Room migration`)
- **docs**: Documentation updates (e.g., `docs(readme): expand installation instructions`)
- **style**: Code style changes (whitespace, formatting, missing semi-colons, etc.)
- **refactor**: Code changes that neither fix a bug nor add a feature
- **perf**: Performance improvements
- **test**: Adding or correcting tests

---

## Pull Request Checklist

Before submitting your Pull Request, ensure that:
- [ ] The app builds successfully with `./gradlew compileDebugSources`.
- [ ] All unit and Robolectric tests pass.
- [ ] No hardcoded passwords, private keys, or API tokens are checked in.
- [ ] No debug code (e.g., `Log.d` with sensitive information or unfinished mocks) is present.
- [ ] All interactive UI components have a unique `testTag` for automation testing.
