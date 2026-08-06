<div align="center">

# PALZEE

### Next-generation private social platform

**Privacy-first • Native Android • Local-first • Realtime**

![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white)
![Android](https://img.shields.io/badge/Android-Native-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Supabase](https://img.shields.io/badge/Supabase-Backend-3ECF8E?style=for-the-badge&logo=supabase&logoColor=white)

> **Building meaningful relationships—not endless scrolling.**

</div>

---

## Why PALZEE?

PALZEE is an Android-first social platform focused on **privacy**, **trusted communities**, and **user control**. Instead of maximizing engagement through algorithms, it prioritizes authentic sharing, native performance, and a distraction-free experience.

---

## Architecture

```mermaid
flowchart LR

UI["Compose UI"]
VM["ViewModels"]
REPO["Repositories"]

ROOM[(Room)]
DATA[(DataStore)]
SUPA[(Supabase)]
PG[(PostgreSQL)]
STORE[(Storage)]
REAL["Realtime"]

UI --> VM
VM --> REPO

REPO --> ROOM
REPO --> DATA
REPO --> SUPA

SUPA --> PG
SUPA --> STORE
SUPA --> REAL
```

---

## Media Flow

```mermaid
flowchart LR

CameraX --> Processing
Processing --> Media3
Media3 --> Cache
Cache --> Upload
Upload --> Supabase
```

---

## Tech Stack

| Android | Backend | Tools |
|----------|---------|-------|
| Kotlin | Supabase | Gradle |
| Jetpack Compose | PostgreSQL | Git |
| CameraX | Storage | Android Studio |
| Media3 | Realtime | Firebase |

---

## Project Structure

```text
app
├── core
├── feature
│   ├── auth
│   ├── camera
│   ├── chat
│   ├── home
│   └── pals
├── services
└── utils
```

---

## Getting Started

```bash
git clone https://github.com/asurxeth/PALZEE.git
cd PALZEE
./gradlew assembleDebug
```

For architecture, backend, deployment and release details, see the **/docs** directory.

---

<div align="center">

⭐ **Star the repository if you like the project.**

Building the future of **private social networking**.

</div>
