# Ultimo-Mobile

[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://developer.android.com/)
[![Language](https://img.shields.io/badge/Language-Kotlin%202.0-blue.svg)](https://kotlinlang.org/)
[![UI](https://img.shields.io/badge/UI-Jetpack%20Compose-blueviolet.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

Native Android client for the Ultimo multiplayer card game server. Built with Kotlin, Jetpack Compose, Coroutines, and WebSockets.

---

## Features

- **Jetpack Compose UI:** Declarative interface with Material 3 components and custom Canvas animations for card interactions.
- **Unidirectional Data Flow:** MVI/MVVM architecture with Kotlin Coroutines and `StateFlow` managing game states.
- **Real-Time WebSockets:** Low-latency bidirectional connection to the authoritative game server over RFC 6455.
- **Connection Handling:** Automatic reconnection with exponential backoff on network changes.
- **Local Cache:** Room database for storing player settings, offline match history, and game stats.

---

## Getting Started

### Prerequisites
- Android Studio Ladybug or newer
- Android SDK 34+
- JDK 17 or 21

### Building from Source
```bash
git clone https://github.com/StanislawGolec/Ultimo-Mobile.git
cd Ultimo-Mobile
./gradlew assembleDebug
```
