# Privacy Lock Settings Configuration Guide

This document describes the configurations, preferences, and UI options managed by the **Settings Screen** (`SettingsScreen.kt`).

---

## 🏗️ 1. Settings Architecture Overview

The Settings screen provides a centralized menu for configuring your security preferences. These preferences are stored in the `security_config` table and are updated automatically when changes are made.

---

## ⚙️ 2. Core Security Preferences

```
┌────────────────────────────────────────────────────────┐
│                        Settings                        │
├────────────────────────────────────────────────────────┤
│  [ Theme Selection ]       System, Light, Dark         │
│  [ Auto-Lock Timeout ]     0s, 30s, 1m, 5m             │
│  [ Randomized Keypad ]     Toggle On/Off               │
│  [ Keypress Haptics ]      Toggle On/Off               │
└────────────────────────────────────────────────────────┘
```

### 1. Theme Mode Selection
Supports Light, Dark, and System theme settings.
* **Light Theme**: Provides a clean, bright interface.
* **Dark Theme**: Eye-safe, dark palette designed to reduce strain in low-light environments.
* **System Default**: Automatically matches your device's system theme settings.

### 2. Auto-Lock Timeout
Defines how long a protected application remains unlocked after entering the background.
* **Immediately (0s)**: Locks the app again immediately when it is minimized or closed. This is the most secure setting.
* **Timed Sessions (30s, 1m, 5m)**: Keeps the app unlocked for a specific duration, preventing you from having to enter your PIN repeatedly when switching apps.

### 3. Randomized Keypad Toggle
Shuffles keys 1–9 every time the keypad is displayed. This prevents onlookers from guessing your PIN based on hand movements and keeps your screen free from oily smudge patterns in fixed spots.

### 4. Keypress Haptics
Toggles tactile haptic feedback (subtle vibrations) on button taps.

---

## 💾 3. Configuration Management

The Settings screen includes dedicated options for exporting and importing your settings:

* **Export Settings**: Converts your current configuration settings and locked applications list into a compact, semi-colon-separated backup string that you can save securely.
* **Import Settings**: Pastes a previously saved backup string to restore your locked applications list and preferences instantly.
