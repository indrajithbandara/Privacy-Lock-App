plugins { alias(libs.plugins.android.application) }

android {
    namespace = "com.example.legacyfixture"
    compileSdk { version = release(36) { minorApiLevel = 1 } }
    defaultConfig {
        // Intentionally identical to the historical app, for disposable emulator upgrades only.
        applicationId = "com.aistudio.privacylock.pnvxld"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0-fixture"
    }
}

androidComponents {
    beforeVariants(selector().withBuildType("release")) { it.enable = false }
}
