# Palzee 🎥
### High-Performance Local-First Vlogging Engine & Jetpack Compose Media Pipeline
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Design-System](https://img.shields.io/badge/Design_System-Material_3-6750A4?logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![Framework](https://img.shields.io/badge/Framework-Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack-compose)
[![Backend](https://img.shields.io/badge/Backend-Supabase-3ECF8E?logo=supabase&logoColor=white)](https://supabase.com/)

Palzee is a native Android application engineering micro-vlogging through a low-latency, local-first media architecture. Built completely around standard system components and the Android Material 3 Design framework, Palzee achieves instantaneous video processing, frame-accurate slideshow transitions, and sub-millisecond perceived interface responsiveness without relying on network-first blocking actions.

---

## 🛠 Tech Stack & Workspace Architecture

Palzee implements a modular framework engineered for predictable memory allocation, native Android system optimizations, and zero UI-thread blocking.

*   **Design Framework:** Pure Material 3 (M3) component patterns, responsive token systems, and system icons running over standard device Android layouts.
*   **Media Processing Layer:** Google Media3 Ecosystem (`ExoPlayer`), CameraX API, and native hardware decoder configurations (`MediaCodec`).
*   **Persistent & Cache Data Fabric:** 
    *   Jetpack DataStore (Proto-backed structural state isolation).
    *   Atomic shared cache files via isolated multi-process `SharedPreferences` (`vlog_prefs`).
    *   Deterministic `Context.cacheDir` disk-space registry garbage collector.
*   **Backend Infrastructure:** Supabase PostgREST Client, PostgreSQL Relational Store, and Edge CDN Storage Infrastructure.

---

## 📱 Material Device Viewports & App Mockups

The core user experience is delivered through dedicated native viewports optimized for real-time asset tracking and media controls.

### 1. Material 3 Home Viewport Layout
![1. Material 3 Home Viewport Layout](file:///Users/pratham/.gemini/antigravity-ide/brain/ae35c4b5-2fee-46f8-be8e-3465574a517a/editorial_dashboard_1783940242349.png)

### 2. CameraX Viewfinder HUD
![2. CameraX Viewfinder HUD](file:///Users/pratham/.gemini/antigravity-ide/brain/ae35c4b5-2fee-46f8-be8e-3465574a517a/camera_capture_1783940256558.png)

### 3. Native Playback Preview Frame
![3. Native Playback Preview Frame](file:///Users/pratham/.gemini/antigravity-ide/brain/ae35c4b5-2fee-46f8-be8e-3465574a517a/playback_preview_1783940271262.png)

### 4. Application Storage Metrics View
![4. Application Storage Metrics View](file:///Users/pratham/.gemini/antigravity-ide/brain/ae35c4b5-2fee-46f8-be8e-3465574a517a/storage_metrics_1783940292767.png)

---

## 📐 User Navigation & Flow Architecture

The user journey transitions smoothly between full-screen continuous media recording and interactive group dashboard layouts. Below is the structural state-machine path:

```text
[ Launch App (MainActivity) ] ──> Runs Background Cache GC (0ms Impact)
              │
              ▼
     [ Home Dashboard ] <───► [ Group / Palzee Slider View ]
              │
              ├───► [ Camera Capture View ] ───► Local Persistent File (.mp4)
              │                                           │
              ▼                                           ▼
   [ Upload Service Loop ] ◄───────────────── [ Captured Preview Screen ]
```

*(Supabase Background Sync)                       (Instant Local JPEG Latch)*

---

## ⚡ Engineered Quality & Performance Paradigms

### 1. 0ms Perceived Transition Latency (The State-Latch Gate)
To completely bypass Android's variable media-hardware warm-up latency, Palzee drops the concept of structural loading screens. 
*   **The Latch Logic:** When a video clip is committed, a high-speed background worker instantly grabs a static keyframe using `MediaMetadataRetriever` at `timeUs = 0`, caching it locally as a precise JPEG file. 
*   **The UI Binding:** UI Layout containers render this static image overlay immediately. Concurrently, `ExoPlayer` mounts its `TextureView` via `onSurfaceTextureAvailable` underneath. The second `onRenderedFirstFrame` triggers, the image seamlessly cross-fades out, delivering immediate visual response.

### 2. Algorithmic Cache Garbage Collector (GC)
To protect the runtime system heap and maintain an optimal storage footprint in the Android settings menu, files generated during capture, filtering, and cross-screen sharing are systematically managed by a specialized lookup Garbage Collector:
*   **Registry Verification:** Active processing files are check-checked against a system-level preferences registry (`vlog_paths`).
*   **Orphan Cleanups:** On both app launch (`onCreate`) and termination (`onDestroy`), unreferenced temporary data variants (`temp_preview_save_*`, `cached_pal_*`) are wiped via standard low-level system execution. This keeps the reported **App Info Storage Size** at a minimal footprint.

### 3. Asynchronous Sync Shifting
Network uploads run detached from critical UI processing tracks. Database insert actions enforce distinct returned constraints (`.select("id")`), blocking heavy rows from sending unnecessary payloads back across the network, reducing data overhead.

---

## 🚀 Initialization Framework

Clone and build the system using the following release sequence:

```bash
# 1. Clean build directories and erase standard task caches
./gradlew clean

# 2. Compile full production release variants
./gradlew assembleRelease

# 3. Perform atomic target device uninstallation followed by fresh installation
./gradlew uninstallRelease installRelease
```

*Note: Ensure target environment signing flags (`signingConfig`) are securely declared inside your active modules before deploying the system release variants.*

---

## 🛡️ License
All rights reserved by Finrein.
