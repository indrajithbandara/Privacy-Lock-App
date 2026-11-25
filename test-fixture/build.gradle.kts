plugins { alias(libs.plugins.android.application) }

android {
    namespace = "com.example.protectionfixture"
    compileSdk { version = release(36) { minorApiLevel = 1 } }
    defaultConfig {
        applicationId = "com.example.protectionfixture"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1"
    }
}

androidComponents {
    beforeVariants(selector().withBuildType("release")) { it.enable = false }
}
