<div align="center">

# PALZEE

### Next-generation private social platform

**Privacy-first • Native Android • Local-first • Realtime**

<p align="center">
  <img src="https://skillicons.dev/icons?i=kotlin,androidstudio,supabase,postgres,firebase" alt="Palzee Tech Stack" />
</p>

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
