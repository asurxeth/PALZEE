# Palzee (PALS) 📹✨

Palzee is a modern, real-time social sharing Android application designed for close friend groups to share their daily moments side-by-side. Featuring interactive hourly reminder notifications, custom camera captures with visual effects, real-time chat, and a custom soundboard, Palzee keeps friend groups connected throughout the day.

---

## 🚀 Key Features

* **Side-by-Side Group Sharing**: Cohesive grid layouts displaying daily vlogs and hourly pals of all group members.
* **Hourly & First Pal Reminders**: Custom background execution pipelines using system `AlarmManager` to check permissions, enforce sleep windows (2 AM - 8 AM shutoff), and schedule precise hourly and daily first-app-open notification triggers.
* **Rich Interactions**: Threaded group replies with animated slideshow transitions, real-time emoji reactions overlaying member avatar badges, and soundboard audio waveforms.
* **Advanced Camera Capabilities**: Implemented with Jetpack CameraX, featuring custom zoom effects, real-time camera rotations, and preview rendering.
* **Modern Material Design**: Premium glassmorphic cards, smooth animations, dark mode theme consistency, and responsive layouts.

---

## 🛠️ Technology Stack

* **UI Framework**: 100% Jetpack Compose (Modern Android UI toolkit).
* **Architecture Pattern**: MVVM/MVI Clean Architecture.
* **Backend Database & Auth**: Supabase (PostgreSQL, Storage buckets, and Realtime WebSocket clients).
* **Dependency Injection**: Dagger Hilt.
* **Background Tasks**: `AlarmManager` + `BroadcastReceiver` for highly optimized reminder notifications.
* **Asynchronous Logic**: Kotlin Coroutines and Flows for structured concurrency.
* **Media Processing**: Google Media3 ExoPlayer for video previews and vlog playback.

---

## 📁 Project Structure & Architecture

The project adheres to Clean Architecture principles, ensuring scalability and testability.

```
com.finrein.pals
├── PalApplication.kt                 # Application class & Supabase init
├── MainActivity.kt                  # Entry activity & startup broadcasts
├── data
│   └── model
│       ├── PalItem.kt               # Pal/Group representations
│       ├── SubmissionDbItem.kt      # Shared vlog/pal submissions
│       └── UserPalMapping.kt        # Group membership mappings
├── notification
│   ├── PalAlarmScheduler.kt         # AlarmManager scheduling logic
│   └── PalNotificationReceiver.kt   # Startup checks & notification manager
└── presentation
    └── home
        ├── HomeScreen.kt            # Compose layouts, Dialogs & Captured Preview
        ├── PalGroupGridScreen.kt    # Homescreen Group item cells & feed grid
        └── HomeViewModel.kt         # Mappings, states, and DB synchronizers
```

### Key Components

1. **`PalAlarmScheduler` & `PalNotificationReceiver`**: Handles background alarm scheduling according to user preferences (Off, 1 hour, or 3 hours). Validates `POST_NOTIFICATIONS` runtime permissions dynamically, respects the night-time sleep cycle (no notifications between 2 AM and 8 AM), and schedules the daily first pal reminder.
2. **`HomeViewModel`**: Syncs group memberships, submissions, chat threads, and emoji reactions in real-time from Supabase database channels.
3. **`CapturedPreviewScreen`**: A high-performance Compose-based camera media capture review overlay, optimized with cached data mappings to eliminate layout jumps and rendering glitches.

---

## 📦 Building the App

### Requirements
* Android Studio Ladybug or newer.
* JDK 17 / JDK 21.
* Connected Android Device (works across custom OS versions like ColorOS/OriginOS/FuntouchOS).

### Build Commands
To compile a clean release package and deploy it to a connected device:

```bash
# Clean and compile the Release build
./gradlew clean assembleRelease --no-build-cache --rerun-tasks

# Uninstall previous debug versions and install the release APK
./gradlew uninstallAll installRelease
```

---

## 🛡️ License
All rights reserved by Finrein.
