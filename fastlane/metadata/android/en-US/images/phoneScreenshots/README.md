# Screenshots & UI Visualizations

This directory is organized to host visual walkthroughs and screenshot assets for the Privacy Lock application. These images are referenced in the documentation, GitHub marketplace listings, and the repository README.

---

## 📸 Screencast & Mockup Directory

The following layout definitions specify the official screenshots captured for the application. Each image is styled around the **Sage Green Material 3** design system.

### 1. Home / Dashboard Screen (`/screenshots/home.png` & `/screenshots/dashboard.png`)
* **Composition**: Shows the main Privacy Lock interface.
* **Key Elements**:
  * Large, modern security health card displaying the dynamic **Privacy Score** (e.g., `85/100`).
  * Sub-grid of active system services (Accessibility Service status, Usage Access, and Draw Overlays).
  * Floating Action Button (FAB) at the bottom right styled with an outlined shield icon for rapid locking state toggling.
  * Recent activity cards showing the security timeline.

### 2. Secure PIN Overlay Screen (`/screenshots/lock_screen.png`)
* **Composition**: The full-screen native overlay intercepting a target application launch.
* **Key Elements**:
  * Centered security logo and prompt ("App is Locked").
  * 6-digit PIN input container displaying masked circular entry dots (`••••••`).
  * **Standard Keypad Layout**:
    ```
    1   2   3
    4   5   6
    7   8   9
    Clear   0   Backspace
    ```
  * Soft tactile buttons with responsive ripple states.
  * Fallback biometric authentication button placed cleanly beside the keypad.

### 3. Advanced Settings Screen (`/screenshots/settings.png`)
* **Composition**: System configuration interface with Material 3 standard switches and list categories.
* **Key Elements**:
  * **Master PIN Configuration** options with salt/hashing specifications.
  * **Decoy PIN Toggle** with standalone input fields.
  * **Screenshot Protection Switch** (enabling `FLAG_SECURE`).
  * **Random Keypad Switch** (shuffling keypad numbers 1–9 while preserving positions for 0, Clear, and Backspace).

### 4. System Permissions Walkthrough (`/screenshots/permissions.png`)
* **Composition**: Step-by-step onboarding wizard showing status indicators and system authorization prompts.
* **Key Elements**:
  * Permission cards showing red (missing) or green (authorized) states.
  * Custom illustrative icon showing background overlay and accessibility service binding.
  * Explicit action buttons triggering direct system intent routing.

### 5. Application Selector List (`/screenshots/app_list.png`)
* **Composition**: Paginated list of user and system apps installed on the device.
* **Key Elements**:
  * Search bar at the top to filter package names.
  * Clean lists showing app package names (e.g., `com.android.settings`, `com.android.vending`) alongside system icons.
  * Outlined Material 3 switches to toggle individual app locking states.

### 6. Security Center & Intruder Logs (`/screenshots/security_center.png`)
* **Composition**: Detailed security diagnostic and alert logs database view.
* **Key Elements**:
  * Scrollable history timeline showing failed entry attempts with masked codes (`****`).
  * Automatically generated **Procedural Avatars** assigned to intrusion events to represent different unauthorized attempts visually.
  * System warning cards for root detection or background process constraints.

---

*Note: Due to image generation rate limits, these visual assets are documented with precise architectural layout constraints above. To capture fresh live screenshots on your development machine, build the debug target, run on a connected physical device or emulator, and use Android Studio's standard Screen Capture tool to save files directly to this directory.*
