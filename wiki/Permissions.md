# Permissions

To function as an on-device application blocker on modern Android versions, Privacy Lock requires specific system permissions. This page details each permission, its technical necessity, how to enable it, and troubleshooting steps.

---

## Permission Summary Table

| Permission ID | Name | Request Method | Purpose |
| :--- | :--- | :--- | :--- |
| `android.permission.BIND_ACCESSIBILITY_SERVICE` | **Accessibility Service** | Intent to System Settings | Monitors window state changes to detect target app launches. |
| `android.permission.PACKAGE_USAGE_STATS` | **Usage Access** | Intent to System Settings | Matches running processes with app categories. |
| `android.permission.SYSTEM_ALERT_WINDOW` | **Display Over Other Apps** | Intent to System Settings | Draws the secure keypad overlay on top of protected apps. |
| `android.permission.POST_NOTIFICATIONS` | **Notification Permission** | Compose Dynamic Request | Displays persistent status indicators. |
| `android.permission.QUERY_ALL_PACKAGES` | **Query All Packages** | Manifest Declared | Queries installed applications on the device. |

---

## Detailed Explanations & Setup Guides

### 1. Accessibility Service (Critical)
* **Why it exists**: Android prevents ordinary background apps from knowing what other apps are running. The Accessibility Service is the only stable, real-time mechanism on modern Android versions to observe window transitions (`TYPE_WINDOW_STATE_CHANGED`) and block access instantly.
* **How to enable**:
  1. Go to **Settings > Accessibility**.
  2. Locate **Privacy Lock** under *Installed Apps* (or *Downloaded Apps*).
  3. Toggle the permission switch **ON**.
  4. Confirm the system warning dialog.

---

### 2. Display Over Other Apps / System Overlay
* **Why it exists**: When the Accessibility Service identifies a locked app launch, it must immediately render a security shield. This permission allows `LockActivity` to draw its fullscreen layout on top of any other running applications.
* **How to enable**:
  1. Go to **Settings > Apps > Special App Access > Display over other apps**.
  2. Select **Privacy Lock** from the list.
  3. Toggle the switch to **Allow display over other apps**.

---

### 3. Usage Access
* **Why it exists**: Provides access to package categorization and history. This allows Privacy Lock to dynamically group your applications into standardized lists (e.g., *Social*, *Finance*, *Utilities*) for bulk configurations.
* **How to enable**:
  1. Go to **Settings > Apps > Special App Access > Usage Access**.
  2. Tap **Privacy Lock**.
  3. Toggle **Permit usage access** to ON.

---

### 4. Query All Packages (Manifest Permission)
* **Why it exists**: Declared directly in the `AndroidManifest.xml` manifest. It allows the local repository to scan and compile a list of all application packages installed on the device, allowing you to search and select apps to lock.
* **Technical Note**: Since Android 11 (API 30), package visibility is restricted by default. This permission is necessary for any app lock or system utility to build an interactive app selection dashboard.

---

## System-Specific Issues & Workarounds

### "Restricted Setting" Grayed Out (Android 13+)
On Android 13 and later, if an app is sideloaded or installed from outside the Google Play Store, the system may prevent enabling the Accessibility Service, graying out the option with a "Restricted Setting" warning.

```
+---------------------------------------------+
|              RESTRICTED SETTING             |
|                                             |
| For your security, this setting is          |
| currently unavailable.                      |
|                                             |
|                   [ OK ]                    |
+---------------------------------------------+
```

* **Workaround**:
  1. Open the primary device **Settings > Apps**.
  2. Select **Privacy Lock** from the list of installed applications.
  3. Tap the **Three Dots Icon (More)** in the top-right corner of the App Info screen.
  4. Tap **Allow restricted settings**.
  5. Provide your device PIN or biometric verification.
  6. Return to **Settings > Accessibility**; you can now toggle the Privacy Lock Accessibility Service.

### Brand-Specific Service Termination (OEM Task Killers)
Certain manufacturers (e.g., Xiaomi, Huawei, Samsung) implement aggressive battery-saving background task managers that terminate accessibility services to conserve battery.
* **Fix**:
  1. Navigate to **App Info** for Privacy Lock.
  2. Select **Battery / Battery Optimization**.
  3. Choose **Don't Optimize** (or *Unrestricted*).
  4. Pin Privacy Lock in your recent app tray if supported by your launcher.

---

[[Home]] | [<< Security](Security) | [[Settings >>](Settings)]
