# Developer Guide

This developer guide provides step-by-step instructions for open-source contributors looking to extend Privacy Lock's features, modify database schemas, or add new screens.

---

## 1. Project Architecture Reference

```
+------------------------------------------------------------+
|                       DEVELOPER PATHWAY                    |
+------------------------------------------------------------+
  1. Define Entity ----> 2. Create DAO ----> 3. Add to Database
  4. Repository --------> 5. ViewModel ----> 6. Compose UI Screen
```

Before writing code, ensure you understand our core design principles:
* **Reactive Data Flow**: Always expose database models to the UI as cold or hot Flows.
* **Thread Safety**: Never write to SQLite or DataStore on the Main Thread. Always offload operations using `Dispatchers.IO`.
* **Dynamic Colors**: Rely on Material 3 standard design tokens so your UI adapts to system color schemes.

---

## 2. Modifying the Database Schema

If your feature requires storing new configuration settings or logs, follow this schema update guide:

### Step 1: Update or Create the Entity
To add a new setting to the global configuration, open `data/Entities.kt` and add a property to the `SecurityConfig` data class:

```kotlin
@Entity(tableName = "security_config")
data class SecurityConfig(
    @PrimaryKey val id: String = "global",
    // Existing properties...
    val newFeatureEnabled: Boolean = false // Your new setting
)
```

### Step 2: Implement Database Migrations
Open `data/AppDatabase.kt` and declare an incremental migration path to update existing databases on user devices:

```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE security_config ADD COLUMN newFeatureEnabled INTEGER NOT NULL DEFAULT 0")
    }
}
```

Add the migration to your database builder:
```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, "privacy_lock.db")
    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
    .build()
```

---

## 3. Creating a New Compose Screen

To add a new configuration page or visual view:

### Step 1: Create the Composable Screen
Create a new file under `ui/screens/` (e.g., `NewFeatureScreen.kt`):

```kotlin
@Composable
fun NewFeatureScreen(
    viewModel: PrivacyViewModel,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            SmallTopAppBar(title = { Text("New Feature") })
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Text("Your custom layout here")
        }
    }
}
```

### Step 2: Register the Navigation Route
We use **Navigation Compose** to manage routing. Register your new screen in `MainActivity.kt` inside the root navigation host:

```kotlin
composable("new_feature") {
    NewFeatureScreen(
        viewModel = viewModel,
        onBack = { navController.popBackStack() }
    )
}
```

---

## 4. Writing & Running Automated Tests

To maintain code quality and prevent regressions, all core logic should be accompanied by tests.

### Running Tests Locally
We use **Robolectric** to run unit and integration tests locally without requiring an emulator:

* **Run all tests**:
  ```bash
  gradle :app:testDebugUnitTest
  ```
* **Verify visual layouts (Screenshot Tests)**:
  We use **Roborazzi** to verify layouts and prevent visual regression. Run screenshot tests using:
  ```bash
  gradle :app:verifyRoborazziDebug
  ```
* **Record reference screenshots** (if you've intentionally changed visual layouts):
  ```bash
  gradle :app:recordRoborazziDebug
  ```

---

## 5. Debugging and Diagnostics

### Inspecting Local Logs
Use **Logcat** in Android Studio to monitor application state and event updates. We filter logs using the following tags:
* `PrivacyViewModel`: Logs setting changes and database transactions.
* `AppAccessibilityService`: Logs foreground package detection and window state changes.
* `LockActivity`: Logs keypad inputs and credential validation attempts.

```bash
# Filter logs using ADB in your terminal
adb logcat -s "AppAccessibilityService:D" "LockActivity:D"
```

---

[[Home]] | [<< API Reference](API-Reference) | [[License >>](License)]
