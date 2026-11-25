# Build and validation

Use JDK 21, Gradle 9.3.1, Android SDK Platform 36.1 and platform tools. The repository currently does not include a checked-in Gradle wrapper. Install Gradle from its official distribution, then run these commands from the repository root.

```bash
gradle :app:assembleOpensourceDebug :app:assemblePlayDebug
gradle :app:lintOpensourceDebug :app:lintPlayDebug
gradle :app:testOpensourceDebugUnitTest :app:testPlayDebugUnitTest
```

For connected instrumentation testing, start an Android device or emulator with supported API level and run:

```bash
gradle :app:connectedOpensourceDebugAndroidTest
```

CI has jobs for API 24 and API 36. Test reports are uploaded when available.

Optimized release validation without signing:

```bash
gradle :app:assembleOpensourceRelease :app:assemblePlayRelease :app:bundleOpensourceRelease :app:bundlePlayRelease
```

Unsigned artifacts must not be distributed as production builds. For existing application updates, the package ID and original signing identity must match. See [RELEASE_PROCESS.md](RELEASE_PROCESS.md).
