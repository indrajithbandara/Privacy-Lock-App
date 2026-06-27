# Privacy Lock Biometrics Integration Guide

This document describes the technical implementation, device compatibility profiles, API checks, and callback handlers that govern the biometric authentication system of **Privacy Lock**.

---

## 🏗️ 1. Biometric Integration Architecture

Privacy Lock uses the Jetpack **AndroidX Biometric Library** (`androidx.biometric`) to provide fingerprint and face unlock options. This library acts as a backward-compatible wrapper over the system's native biometric hardware, providing a consistent user experience across different Android versions.

```
┌────────────────────────────────────────────────────────┐
│                      LockActivity                      │
└───────────────────────────┬────────────────────────────┘
                            │
               Queries Sensor Compatibility
                            ▼
┌────────────────────────────────────────────────────────┐
│         BiometricManager.from(activityContext)         │
└───────────────────────────┬────────────────────────────┘
                            │
                  If Sensors Are Available:
                  Launches Secure System Prompt
                            ▼
┌────────────────────────────────────────────────────────┐
│                    BiometricPrompt                     │
└────────────────────────────────────────────────────────┘
```

---

## 🛠️ 2. Hardware Availability Checks

Before prompting the user for biometric authentication, the app checks if the device has compatible biometric hardware and if the user has enrolled their fingerprints or face data.

### Checking Sensor Availability (`LockActivity.kt`):
```kotlin
val biometricManager = BiometricManager.from(this)
val canAuthenticate = biometricManager.canAuthenticate(
    BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
)
if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
    // Biometric authentication is available and configured
}
```

---

## 🔐 3. Prompt Configuration & Callbacks

The biometric prompt is constructed using `BiometricPrompt.PromptInfo` to ensure a consistent presentation that aligns with Material Design guidelines.

### Callback Implementation:
```kotlin
val executor = ContextCompat.getMainExecutor(this)
val biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        // Handle error states (e.g., too many failed attempts, cancelled by user)
    }

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        unlockApp() // Session token is generated on successful authentication
    }

    override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        // Provide visual/haptic feedback on failed authentication attempts
    }
})

val promptInfo = BiometricPrompt.PromptInfo.Builder()
    .setTitle("Unlock $appName")
    .setSubtitle("Authenticate to access locked application")
    .setNegativeButtonText("Use PIN")
    .build()

biometricPrompt.authenticate(promptInfo)
```

---

## 🛡️ 4. Dynamic Biometric Modification Monitoring

To protect your device if an unauthorized user gains access to your passcode, Privacy Lock supports monitoring for changes to the system's enrolled biometrics:

* **Key Invalidation**: When a new fingerprint or face profile is enrolled in system settings, the app can automatically invalidate the biometric unlock key.
* **Master PIN Fallback**: If biometric modifications are detected, the app will disable biometric unlock and force the user to enter their Master PIN to re-enable it. This prevents unauthorized users from bypassing the lock screen by enrolling their own fingerprints.
