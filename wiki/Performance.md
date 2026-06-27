# Privacy Lock App Performance Guide

Welcome to the **Privacy Lock App Performance Guide**. This document outlines our performance budgets, memory management practices, database read/write optimizations, and profiling guidelines designed to keep the background security service lightweight and efficient.

---

## 🎨 Navigation Menu

| **Core Manuals** | **Technical Guides** | **Governance & Process** |
| :--- | :--- | :--- |
| 🏠 [[Home]] | 📐 [[Architecture]] | 🛠️ [[Contributing]] |
| 🚀 [[Getting Started]] | 🔌 [[Permissions]] | 🤖 [[CI-CD-Pipeline]] |
| 📲 [[Installation]] | ⚙️ [[Accessibility-Service]] | 🏷️ [[Release-Process]] |
| 📦 [[Features]] | 🔒 [[Security-Model]] | 🗺️ [[Roadmap]] |
| 👤 [[User-Guide]] | 💾 [[Database]] | 🐛 [[Troubleshooting]] |
| 💻 [[Developer-Guide]] | 🧠 [[Lock-Engine]] | ❓ [[FAQ]] |
| 🛡️ [[Administrator-Guide]] | 🧪 [[Testing]] | 📜 [[License]] |

---

## ⚙️ 1. Resource Constraints & Budgets

Because Privacy Lock runs a persistent service in the background, we enforce strict resource footprints to prevent system warnings, aggressive OS closures, or battery drain:

| Resource Type | Budget / Goal | Implementation Boundary |
| :--- | :--- | :--- |
| **CPU Usage** | < 0.5% average | Inactive during standard app usage; runs event evaluations only. |
| **Memory (RAM)** | < 35 MB | Caches active configurations; utilizes compact object allocations. |
| **Database Read Lag** | < 15ms | Executes lock checks entirely in-memory using `AppLockManager` caches. |
| **Overlay Render Latency** | < 120ms | Keeps Compose overlays highly optimized, avoiding deep layouts. |

---

## 💾 2. Room Database Latency Optimization

To prevent user frustration, the background Accessibility Service must evaluate package lock statuses instantly. We implement the following strategies:

*   **In-Memory Lock Filtering**: We load the set of locked application package names into a thread-safe JVM cache inside `AppLockManager` on launch. Subsequent interception events check this cache, bypassing disk I/O entirely.
*   **WAL (Write-Ahead Logging)**: SQLite is configured in WAL mode to allow reading concurrently while logging events, reducing transaction blockades.
*   **Asynchronous Logging**: Timeline events and intruder log recordings are executed on specialized I/O threads using Kotlin Coroutines on `Dispatchers.IO`.

---

## 🔌 3. Background Thread Memory Leaks

Persistent services are prone to memory leakage over long operational cycles. We prevent leaks by enforcing these coding rules:

*   **Avoid Context Retaining**: Never store static references to `Activity` or `View` objects inside singleton classes (e.g., `AppLockManager`). Always use the application context (`applicationContext`) for singleton initialization.
*   **Safe Listener Deregistration**: Clear or unbind UI observer listeners during activity lifecycle endings (`onDestroy` or `onPause`).
*   **LeakCanary Integration**: In local debug compiles, we include **LeakCanary** to automatically track and report heap leaks when testing app flows.

---

## 🛠️ 4. Local Profiling Guidelines

We recommend testing resource behaviors using the **Android Studio Profiler**:

1.  Connect your physical test device via ADB.
2.  In Android Studio, select **Run -> Profile 'app'**.
3.  Open the **CPU Profiler** to check thread behaviors during package transitions.
4.  Use the **Memory Profiler** to trigger garbage collections and verify that closing `LockActivity` returns overall heap sizes to baseline levels.

---

[<< Return to Testing Guide](Testing) | [Proceed to CI-CD Pipeline Guide >>](CI-CD-Pipeline)
