# Privacy Lock Configuration Guide

This document explains the technical configurations, manifest settings, XML resources, and compiler flags that govern how the **Privacy Lock** Android app behaves at runtime.

---

## 🛠️ 1. Core Build System Variables

Privacy Lock uses a Kotlin DSL-based Gradle configuration. Key parameters are defined in `/app/build.gradle.kts` and can be customized for specific deployment environments.

### Key Build Variables:
* **`compileSdk`**: Set to API 36 (Android 16 Developer Preview).
* **`targetSdk`**: Set to API 36 to comply with modern Android security and background behavior policies.
* **`minSdk`**: Set to API 24 (Android 7.0) to balance security features with compatibility for older devices.
* **`applicationId`**: Set to `com.aistudio.privacylock.pnvxld` to ensure a unique namespace on devices and in app stores.

---

## 📝 2. Android Manifest Declarations

The `AndroidManifest.xml` file registers the application's components, services, and security rules:

* **`android:allowBackup`**: Set to `true`, but restricted by `backup_rules.xml` to prevent sensitive SQLite database tables from being backed up to unencrypted cloud storage.
* **`android:fullBackupOnly`**: Set to `true` to enforce modern backup rules on Android 6.0+ devices.
* **`android:dataExtractionRules`**: Points to `data_extraction_rules.xml` to block USB debugging extraction tools from pulling configuration databases.

---

## ⚙️ 3. Accessibility Service Configuration (`accessibility_service_config.xml`)

The behavior of the background intercept service is defined in `/app/src/main/res/xml/accessibility_service_config.xml`:

```xml
<accessibility-service xmlns:android="http://schemas.android.com/apk/res/android"
    android:accessibilityEventTypes="typeWindowStateChanged"
    android:accessibilityFeedbackType="feedbackGeneric"
    android:accessibilityFlags="flagDefault|flagIncludeNotImportantViews|flagRetrieveInteractiveWindows"
    android:canRetrieveWindowContent="false"
    android:notificationTimeout="50"
    android:description="@string/accessibility_service_description" />
```

### Parameter Rationale:
* **`accessibilityEventTypes`**: Restricted to `typeWindowStateChanged` to only monitor foreground app launches, reducing CPU overhead and protecting user privacy.
* **`accessibilityFlags`**: Uses `flagRetrieveInteractiveWindows` to reliably detect when apps are launched from split-screen windows or floating overlays.
* **`canRetrieveWindowContent`**: Set to `false` to ensure Privacy Lock cannot read screen content or keystrokes, protecting user data and maintaining a zero-knowledge architecture.

---

## 🔒 4. Backup & Data Extraction Rules

Sensitive database files and preferences are protected using custom XML rules:

### Backup Rules (`backup_rules.xml`)
Excludes database and shared preference files from standard cloud backups:
```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <exclude domain="database" path="privacy_lock_db" />
    <exclude domain="sharedpref" path="com.example_preferences.xml" />
</full-backup-content>
```

### Data Extraction Rules (`data_extraction_rules.xml`)
Prevents local USB and ADB extraction tools from pulling application data:
```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="database" path="privacy_lock_db" />
    </cloud-backup>
    <device-transfer>
        <exclude domain="database" path="privacy_lock_db" />
    </device-transfer>
</data-extraction-rules>
```

---

## 🛡️ 5. ProGuard & R8 Shrinking Rules

To prevent reverse-engineering and reduce package sizes, configure ProGuard rules in `/app/proguard-rules.pro`:

* **Code Obfuscation**: Renames non-public classes, methods, and variables to random characters to protect the app's security logic.
* **Room Table Preservation**: Keeps Room-generated class structures intact to ensure SQL database schemas remain valid at runtime:
  ```proguard
  -keep class * extends androidx.room.RoomDatabase
  -keep class com.example.data.** { *; }
  ```
* **Moshi & Serialization Rules**: Prevents R8 from stripping serialization helper methods used for importing and exporting settings configurations:
  ```proguard
  -keepclassmembers class * {
      @com.squareup.moshi.Json *;
  }
  ```
