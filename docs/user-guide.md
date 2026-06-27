# Privacy Lock User Manual

Welcome to **Privacy Lock**—your offline-first, highly secure application locker and local privacy protection center for Android. This manual explains how to configure and use every security feature of the app to protect your sensitive applications.

---

## 🎨 1. Application Core Features

Privacy Lock runs entirely on your device without using an internet connection. It features an advanced suite of security features to protect your personal apps:

* **Background App Interceptor**: Monitors app launch events and displays a secure locking screen before protected apps can be accessed.
* **Dual Secure Credentials**: Supports setting a master PIN alongside decoy and panic PINs to protect you under coercion or shoulder-surfing.
* **Randomized Keypad**: Shuffles the positions of the number buttons on the overlay keypad to prevent finger-smudge analysis.
* **Intruder Detection Logs**: Records unauthorized access attempts with timestamps and details inside a local database.
* **Screenshot Protection Shield**: Blocks screenshots and screen recordings on protected app screens and masks app previews in the Recents carousel.
* **Dynamic Dark Theme**: Adapts to your device's light and dark settings for eye-safe use at night.

---

## 🚀 2. Initial Setup Wizard & Permissions

When launching Privacy Lock for the first time, an onboarding wizard guides you through the necessary system permissions:

### 1. Accessibility Service (Required)
Required to detect when a target application is launched and display the lock screen overlay immediately.
* *How to enable*: Click **Grant Permission** in the wizard. In the system Accessibility menu, select **Privacy Lock** and toggle the switch to **On**.

### 2. Usage Stats Access (Optional)
Allows the app to verify active package changes more reliably on custom Android interfaces.
* *How to enable*: Select **Usage Access** in settings and toggle the permission on for Privacy Lock.

### 3. Draw Over Other Apps Overlay (Required)
Required to display the secure overlay screen over other open applications.
* *How to enable*: Select **Display Over Other Apps** and grant permission.

---

## 🔐 3. Configuring Passcodes & PIN Modes

Privacy Lock supports setting different PIN credentials, each triggering a specific behavior:

### Standard Master PIN
Your primary passcode used to access the Privacy Lock settings dashboard and unlock protected applications.
* *Setup*: Navigate to **Settings -> Set Master PIN**, choose a secure 6-digit number, and confirm it.

### Decoy PIN (Anti-Coercion)
An alternative passcode that successfully unlocks the app overlay but hides your actual locked apps list.
* *Purpose*: Protects your privacy if you are forced or coerced into unlocking your device.

### Panic PIN (Emergency Exit)
An alternative passcode that instantly closes the app overlay and returns you to the home screen.
* *Purpose*: Quickly aborts authentication attempts in emergency situations.

---

## 🔒 4. Locking Your Applications

From the main dashboard, you can easily secure individual applications:

1. **Open the Dashboard**: Select the **Apps** tab.
2. **Browse Apps**: Use the search bar or filter by category (e.g., *Financial, Social, System*).
3. **Toggle Lock**: Click the lock icon next to any app to protect it. It will now require PIN verification whenever it is opened.
4. **Favorites**: Tap the star icon on any app to pin it to the top of the list for quick access.

---

## ⚙️ 5. Custom Keypad & Layout Settings

Protect your passcodes from shoulder-surfing with customizable keypad configurations:

* **Sequential Layout**: Keys are arranged in the standard 1–9 matrix.
* **Randomized Keypad**: Shuffles keys 1–9 every time the keypad is displayed. This prevents onlookers from guessing your PIN based on hand movements and keeps your screen free from oily smudge patterns in fixed spots.
* **Vibrate on Keypress**: Provides subtle haptic feedback on button taps.

---

## 📸 6. Screenshot & Task Masking Protection

Privacy Lock uses advanced security flags to block unauthorized screen captures and multitasking snooping:

* **Screenshot Blocker**: Disables screenshots and video recordings on both the settings dashboard and lock overlay screens.
* **Recents Switcher Masking**: Replaces the app's preview in the system Recents carousel with a secure, solid screen to prevent sensitive details from being visible when multitasking.

---

## 🪵 7. On-Device Intruder Center

The Intruder Center acts as a secure local log for recording unauthorized access attempts:

* **Visual Timeline**: View a detailed, chronological list of failed login attempts, showing timestamps and target applications.
* **Masked Passcode Entry**: Logs invalid PIN entries using masked characters (`******`) to prevent the intruder's attempted code from being visible.
* **Intruder Avatars**: Assigns a unique, illustrated silhouette icon to each failed attempt to help you identify and organize security incidents.

---

## 💾 8. Safe Configuration Backups

Backup your application lists and configurations without relying on cloud servers:

### Exporting Settings:
1. Go to **Settings -> Export Configuration**.
2. Copy the generated semi-colon-separated string.
3. Save the string securely in an encrypted text file or notebook.

### Importing Settings:
1. Copy your saved backup string.
2. Navigate to **Settings -> Import Configuration**.
3. Paste the string into the text field and click **Import**. The app will verify the data integrity and restore your locked apps list and preferences.
