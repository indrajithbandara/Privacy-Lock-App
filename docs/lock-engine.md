# Privacy Lock Engine Configuration Guide

This document explains the technical details, cache structures, thread-safe synchronization rules, and lifecycle states managed by **AppLockManager**—the core engine that handles locked app lists and active user sessions.

---

## 🏗️ 1. Core Lock Engine Design

`AppLockManager` is implemented as a thread-safe Kotlin singleton. It manages two main sets of operations:
1. **Dynamic Caching**: Caches locked app package lists in an in-memory `HashSet` to enable sub-millisecond lookup times, avoiding database bottlenecks during fast window transitions.
2. **Session Token Tracking**: Tracks unlocked application packages and calculates active session lifetimes using precise timestamps.

---

## 🗂️ 2. Memory State & Cache Structures

To protect your device efficiently, `AppLockManager` maintains several private, local caches:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                             AppLockManager                              │
├─────────────────────────────────────────────────────────────────────────┤
│  - lockedPackages: MutableSet<String>               [Active lock list]  │
│  - temporarilyUnlockedPackages: MutableSet<String>  [Active sessions]   │
│  - unlockedTimestamps: MutableMap<String, Long>     [Launch timestamps] │
└─────────────────────────────────────────────────────────────────────────┘
```

* **`lockedPackages`**: An in-memory cache of applications marked as locked by the user. This cache is initialized from the Room database on application launch and updated automatically when settings are modified.
* **`temporarilyUnlockedPackages`**: Tracks applications that have been unlocked using the correct PIN. These apps can be launched without entering the PIN again until the session expires.
* **`unlockedTimestamps`**: A map that pairs unlocked package names with the epoch timestamp of when they were unlocked. This map is used to calculate and enforce active session lifetimes.

---

## ⏱️ 3. Immediate vs Timed Auto-Lock Sessions

The application's auto-lock behavior is configured using the `autoLockTimeoutSeconds` parameter in the global configuration settings:

### 1. Immediate Lock (`timeout = 0`)
The application is locked again immediately when it enters the background. This is the most secure setting, as it prevents other users from accessing the app if you navigate away from it.

### 2. Timed Session Lock (`timeout > 0`)
The application remains unlocked for a specific duration after entering the background (e.g., 30s, 1m, 5m). This prevents having to enter your PIN repeatedly if you are switching back and forth between two applications frequently.

### Session Verification Logic (`AppLockManager.kt`):
```kotlin
fun isPackageTemporarilyUnlocked(packageName: String): Boolean {
    return synchronized(temporarilyUnlockedPackages) {
        if (!temporarilyUnlockedPackages.contains(packageName)) return false
        val timeout = synchronized(this@AppLockManager) { autoLockTimeoutSeconds }
        if (timeout > 0) {
            val unlockTime = unlockedTimestamps[packageName] ?: return false
            val elapsed = (System.currentTimeMillis() - unlockTime) / 1000
            if (elapsed > timeout) {
                temporarilyUnlockedPackages.remove(packageName)
                unlockedTimestamps.remove(packageName)
                return false
            }
        }
        return true
    }
}
```

---

## 🔒 4. Thread-Safe Synchronization Rules

Because accessibility events and settings updates can run on different background threads, the lock engine uses explicit synchronization locks to prevent concurrent data conflicts and state errors:

* **Object Locks**: All operations on `lockedPackages`, `temporarilyUnlockedPackages`, and `unlockedTimestamps` are wrapped inside `synchronized()` blocks:
  ```kotlin
  fun temporarilyUnlockPackage(packageName: String) {
      synchronized(temporarilyUnlockedPackages) {
          temporarilyUnlockedPackages.add(packageName)
          unlockedTimestamps[packageName] = System.currentTimeMillis()
      }
  }
  ```
* **Thread-Safe Flow Emission**: Exposes state updates to ViewModels using an immutable, read-only `StateFlow`:
  ```kotlin
  private val _lockedAppsFlow = MutableStateFlow<Set<String>>(emptySet())
  val lockedAppsFlow = _lockedAppsFlow.asStateFlow()
  ```
