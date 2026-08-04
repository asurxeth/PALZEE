# Palzee 🎥
### Low-Latency Local-First Media Architecture & Native Android App & Web Ecosystem

[![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://developer.android.com/)
[![Design-System](https://img.shields.io/badge/Design_System-Material_3-6750A4?logo=materialdesign&logoColor=white)](https://m3.material.io/)
[![Framework](https://img.shields.io/badge/Framework-Jetpack_Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack-compose)
[![Backend](https://img.shields.io/badge/Backend-Supabase-3ECF8E?logo=supabase&logoColor=white)](https://supabase.com/)
[![Website](https://img.shields.io/badge/Website-palzee.fun-FF007A?logo=googlechrome&logoColor=white)](https://palzee.fun/)

Palzee is a native Android application and web ecosystem engineering micro-vlogging for close friend groups through a low-latency, local-first media architecture. Built completely around standard system components, Jetpack Compose, and the Android Material 3 Design framework, Palzee achieves instantaneous video processing, frame-accurate slideshow transitions, sub-millisecond perceived interface responsiveness, and seamless in-app legal/feedback overlay navigation.

---

## 🌐 Web Ecosystem & Legal Transparency (`palzee.fun`)

Palzee hosts a lightweight, responsive web domain at **[https://palzee.fun](https://palzee.fun)** providing full transparency, legal compliance, and user feedback channels.

*   **Official Website:** [https://palzee.fun/](https://palzee.fun/)
*   📜 **Privacy Policy:** [https://palzee.fun/privacy.html](https://palzee.fun/privacy.html) — Outlines data protection and user privacy guarantees.
*   ⚖️ **Terms of Service:** [https://palzee.fun/tos.html](https://palzee.fun/tos.html) — Service usage terms and community guidelines.
*   🛡️ **CSAM Policy & Safety:** [https://palzee.fun/csampolicy.html](https://palzee.fun/csampolicy.html) — Zero-tolerance Child Sexual Abuse Material prevention & reporting standard.
*   💬 **Feedback & Suggestions:** [https://palzee.fun/feedback.html](https://palzee.fun/feedback.html) — Dedicated feedback portal routing to `feedback@palzee.fun`.

---

## 📱 Native In-App Web Overlay (`InAppWebOverlay`)

To avoid context switching out of the mobile application, Palzee embeds a high-performance native Android `InAppWebOverlay` composable:

*   **Zero-External-Browser Context Shift:** Policy pages and the Feedback form open directly inside the app with a dedicated header, page title, and close `(X)` button.
*   **Profile (...) Menu Integration:**
    *   Tapping **feedback** on the main profile screen directly launches `https://palzee.fun/feedback.html`.
    *   Tapping **privacy policy**, **terms of service**, or **csam policy** inside Profile Settings opens their respective pages instantly.
*   **Intent Handling:** Web `mailto:` links (such as `feedback@palzee.fun`) trigger native device email launchers automatically.

---

## 🛠 Tech Stack & Workspace Architecture

Palzee implements a modular framework engineered for predictable memory allocation, native Android system optimizations, and zero UI-thread blocking.

*   **Android Mobile Layer:** Pure Material 3 (M3) Jetpack Compose components, Kotlin Coroutines, responsive token systems, and system icons running over standard device Android layouts.
*   **Media Processing Engine:** Google Media3 Ecosystem (`ExoPlayer`), CameraX API, and native hardware decoder configurations (`MediaCodec`).
*   **Persistent & Cache Data Fabric:** 
    *   Jetpack DataStore (Proto-backed structural state isolation).
    *   Atomic shared cache files via isolated multi-process `SharedPreferences` (`vlog_prefs`).
    *   Deterministic `Context.cacheDir` disk-space registry garbage collector.
*   **Backend & Cloud Infrastructure:** Supabase PostgREST Client, PostgreSQL Relational Store, and Edge CDN Storage Infrastructure.
*   **Web Engine:** Vanilla CSS Woblo token system, Hind typography, Google Analytics 4 (`G-BBRQJPLQ9D`), and XML Sitemap indexer (`/sitemap.xml`).

---

## 📐 User Navigation & Flow Architecture

The user journey transitions smoothly between full-screen continuous media recording, interactive group dashboard layouts, and native in-app web overlays:

```text
[ Launch App (MainActivity) ] ──> Runs Background Cache GC (0ms Impact)
              │
              ▼
     [ Home Dashboard ] <───► [ Group / Palzee Slider View ]
              │
              ├───► [ Camera Capture View ] ───► Local Persistent File (.mp4)
              │                                           │
              ├───► [ Profile (...) Menu ]                 ▼
              │            │                  [ Captured Preview Screen ]
              │            ├───► [ Feedback ] ───► (In-App Overlay: feedback.html)
              │            └───► [ Settings ] ───► (In-App Overlay: privacy/tos/csam)
              │
              ▼
   [ Upload Service Loop ] ◄───────────────── (Instant Local JPEG Latch)
```

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

## 🛡️ License & Support
© 2026 Fin Rein Inc. All rights reserved.  
Support & Contact: `pratham@palzee.fun` | Feedback: `feedback@palzee.fun`
