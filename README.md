# PAL 🎥 
### High-Performance Local-First Vlogging & Decentralized Social Pipeline
[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Framework](https://img.shields.io/badge/Framework-Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack-compose)
[![Backend](https://img.shields.io/badge/Backend-Supabase-3ECF8E?logo=supabase&logoColor=white)](https://supabase.com/)

PAL is a cutting-edge Android application redefining micro-vlogging through an ultra-low-latency, local-first media architecture combined with an immersive "Liquid Glass" design system. Built for seamless cross-screen streaming, PAL achieves instantaneous video processing, frame-accurate slideshow transitions, and sub-millisecond perceived interface latency.

---

## 🛠 Tech Stack & Workspace Architecture

PAL implements a modular framework engineered for predictable memory allocation and zero UI-thread blocking.

*   **UI Architecture:** Declarative Jetpack Compose tightly integrated with state-driven MVI/MVVM architectural models.
*   **Media Processing Layer:** Google Media3 Ecosystem (`ExoPlayer`), CameraX API, and native hardware decoder configurations (`MediaCodec`).
*   **Persistent & Cache Data Fabric:** 
    *   Jetpack DataStore (Proto-backed structural state isolation).
    *   Atomic shared cache files via isolated multi-process `SharedPreferences`.
    *   Deterministic `Context.cacheDir` disk-space registry garbage collector.
*   **Backend Infrastructure:** Supabase PostgREST Client, PostgreSQL Relational Store, and Edge CDN Storage Infrastructure.

---

## 📐 User Navigation & Flow Architecture

The user journey transitions smoothly between full-screen continuous media recording and interactive group dashboard layouts. Below is the structural state-machine path:

```text
[ Launch App (MainActivity) ] ──> Runs Background Cache GC (0ms Impact)
              │
              ▼
     [ Home Dashboard ] <───► [ Group / Pal Slider View ]
              │
              ├───► [ Camera Capture View ] ───► Local Persistent File (.mp4)
              │                                           │
              ▼                                           ▼
   [ Upload Service Loop ] ◄───────────────── [ Captured Preview Screen ]
```

*(Supabase Background Sync)                       (Instant Local JPEG Latch)*

### High-Fidelity Interface Blueprints

```text
====================================================================================
1. HOME FEED VIEW                         2. CAMERA CAPTURE HUD
====================================================================================
+---------------------------------------+ +---------------------------------------+
|  PAL [Vlog Engine]     [Settings]     | | [X] Close           [Flash: Auto]     |
+---------------------------------------+ +---------------------------------------+
|  +-------------+     +-------------+  | |                                       |
|  |   Vlog #1   |     |   Vlog #2   |  | |                 [.]                   |
|  |             |     |             |  | |             Camera Sensor             |
|  | [0ms Thumb] |     | [0ms Thumb] |  | |            TextureView Feed           |
|  +-------------+     +-------------+  | |                 [.]                   |
|                                       | |                                       |
|  +---------------------------------+  | |                                       |
|  | Pals Group Slider (Glassmorphic) |  | | +-----------------------------------+ |
|  |  [Pal A]   [Pal B]   [Pal C]    |  | | | (R) RECORD       00:15 / 01:00 max| |
|  +---------------------------------+  | | +-----------------------------------+ |
+---------------------------------------+ +---------------------------------------+

====================================================================================
3. ULTRA-LOW LATENCY PREVIEW SCREEN       4. APP STORAGE & CACHE PROFILE
====================================================================================
+---------------------------------------+ +---------------------------------------+
| [Back]   Vlog Preview Frame   [Send]  | | App Info > Storage                    |
+---------------------------------------+ +---------------------------------------+
|  +---------------------------------+  | |                                       |
|  |                                 |  | |  PAL Application Space                |
|  |       Instant Static JPEG       |  | |  =====================                |
|  |             Overlay             |  | |  Total Space:           142.5 MB      |
|  |   (Fades out when Video Ready)  |  | |  App Size:               84.2 MB      |
|  |                                 |  | |  User Data:              58.3 MB      |
|  |     [ TextureView Backed ]      |  | |  Cache Size:              0.00 MB     |
|  |  (exoPlayer.setVideoSurface)    |  | |  <<<< [Clean GC Eviction] >>>>        |
|  +---------------------------------+  | |                                       |
|                                       | | +-----------------------------------+ |
| [Pals Selector Menu] [Filter Layer]   | | | [Force Stop]     [Clear Storage]  | |
+---------------------------------------+ +---------------------------------------+
```

---

## ⚡ Engineered Quality & Performance Paradigms

### 1. 0ms Perceived Transition Latency (The State-Latch Gate)

To completely bypass Android's variable media-hardware warm-up latency, PAL drops the concept of structural loading screens.

* **The Latch Logic:** When a video clip is committed, a high-speed background worker instantly grabs a static keyframe using `MediaMetadataRetriever` at `timeUs = 0`, caching it locally as a precise JPEG file.
* **The UI Binding:** UI Layout containers render this static image overlay immediately. Concurrently, `ExoPlayer` mounts its `TextureView` via `onSurfaceTextureAvailable` underneath. The second `onRenderedFirstFrame` triggers, the image seamlessly cross-fades out, delivering immediate visual response.

### 2. Algorithmic Cache Garbage Collection

To protect the runtime system heap, files generated during capture, filtering, and cross-screen sharing are systematically managed by a specialized lookup Garbage Collector:

* **Registry Verification:** Active processing files are cross-checked against a system-level preferences registry (`vlog_paths`).
* **Orphan Cleanups:** On both app launch (`onCreate`) and termination (`onDestroy`), unreferenced temporary data variants (`temp_preview_save_*`, `cached_pal_*`) are wiped via standard low-level system execution. This keeps the reported **App Info Storage Size** at a minimal footprint.

### 3. Asynchronous Sync Shifting

Network uploads run detached from critical UI processing tracks. Database insert actions enforce distinct returned constraints (`.select("id")`), blocking heavy rows from sending unnecessary payloads back across the network, reducing data overhead.

---

## 🚀 Initialization Framework

Clone and build the system using the following release sequence:

```bash
# 1. Clean build directories and erase standard task caches
./gradlew clean

# 2. Compile full production variants
./gradlew assembleRelease

# 3. Perform atomic target device uninstallation followed by installation
./gradlew uninstallRelease installRelease
```

*Note: Ensure target environment signing flags (`signingConfig`) are securely declared inside your active modules before deploying the system release variants.*

---

## 🛡️ License
All rights reserved by Finrein.
