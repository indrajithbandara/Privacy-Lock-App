# Privacy Lock Performance Optimization Guide

This document explains the technical optimizations, caching structures, thread-allocation limits, and battery-conservation policies that ensure **Privacy Lock** runs efficiently in the background without causing system lags or draining battery.

---

## 🏗️ 1. Performance Optimization Overview

Running a continuous background utility requires careful performance management to prevent battery drain and system lag. Our optimization strategy focuses on three main goals:

* **Sub-Millisecond Verification**: Ensuring active package statuses are checked in less than 1ms to prevent lags when launching apps.
* **Minimal Battery Impact**: Keeping total battery usage below 1% over a standard 24-hour cycle.
* **Low Memory Footprint**: Maintaining active RAM usage under 35MB to prevent the OS from closing the service.

---

## ⚡ 2. Caching & Threading Architecture

To check lock statuses quickly without slowing down other apps, Privacy Lock uses an in-memory cache to avoid slow database queries:

```
[ Accessibility Event ] ──> [ Query In-Memory HashSet ] ( < 1ms ) ──> [ Allowed / Blocked ]
                                      ▲
                                 Synchronized
                                      │
[ SQLite Database ] ──────────> [ Load on Launch ] ( Dispatchers.IO )
```

* **In-Memory Lookups**: Active lock statuses are cached in a synchronized `HashSet` (`lockedPackages`) within `AppLockManager`. The accessibility service queries this cache in sub-millisecond times, avoiding slow database queries on every window change.
* **Asynchronous Database Syncs**: Updates to locked app configurations are written to the SQLite database asynchronously in the background using `Dispatchers.IO` to keep the main system thread free.

---

## 🔌 3. Battery Conservation & Wakeup Management

To minimize battery drain, Privacy Lock implements strict resource conservation policies:

* **Screen-Off Event Pausing**: The accessibility service listens for screen-off system broadcasts (`ACTION_SCREEN_OFF`). When the screen is turned off, active background checks are paused until the screen is turned on again.
* **Minimal Event Filtering**: The service is configured to only listen for `TYPE_WINDOW_STATE_CHANGED` events, ignoring other events like scroll movements or tap gestures. This reduces background CPU usage.
* **Avoiding WakeLocks**: The application does not use system wake-locks, allowing your device to enter deep sleep mode normally when it is locked.

---

## 💾 4. RAM & Garbage Collection Optimization

Running continuously in the background requires minimizing memory usage and preventing garbage collection (GC) lags:

* **Object Reuse**: Compose modifiers and keypad layouts are structured to reuse objects whenever possible, preventing frequent garbage collection pauses on the keypad screen.
* **Explicit Array Sanitization**: Passcode buffers are cleared and overwritten in RAM immediately after validation checks are completed to keep memory usage low.
* **No Cache Retention**: Encryption keys and plain-text configurations are cleared from memory as soon as the active user session expires.

---

## 🛡️ 5. ProGuard & R8 Code Shrinking

To keep the application footprint as small as possible, configure ProGuard/R8 shrinking rules in `/app/proguard-rules.pro`:

* **Code Shrinking**: Automatically removes unused classes, methods, and libraries from the compiled binary.
* **Optimization**: Optimizes instructions to reduce the compiled APK size.
* **Obfuscation**: Renames classes and methods to random characters, making the compiled app harder to reverse-engineer while keeping it secure.
