# CarLauncher
A vibe-coded phone car launcher with minimal features, because my car doesn't support anything and I don't want to pay for anything.

Android car launcher, **portrait only**, with a dark theme and large buttons. No Google Play Services, no accounts, no API keys required.

**Requirements:** Android 8.0 (API 26) or later.

## Features

| Section | What it does |
|---|---|
| **Speed** | km/h from the phone's GPS (estimated from distance and time if the chip does not report speed). |
| **Weather** | Icon and temperature from [Open-Meteo](https://open-meteo.com) (free, no key). Uses GPS, network, fused and passive location; if no location is available it falls back to the **backup city** set in the settings. Tap to refresh. |
| **Navigation (CoMaps)** | Footer button: if CoMaps is navigating, it opens **drive mode** — CoMaps full screen (map, route, directions) with a draggable panel on top showing speed, music controls and a button to return home. Otherwise it opens the destination search (and then drive mode). Requires the "display over other apps" permission. |
| **Phone** | Built into the launcher: a grid of favourite contacts with photos (picked from the address book) and a dial pad. One tap = call. |
| **Voice assistant** | Footer button, configurable in the settings: **Claude** (app, voice mode when available), **Gemini** (app, Gemini Live when available) or **any OpenAI-compatible API** (`/chat/completions`): voice chat inside the launcher — system speech recognition, a short model reply read aloud, then it listens again on its own. Presets for OpenAI, Gemini API, Claude API, OpenRouter and Ollama on the local network. The API key stays on the phone and is excluded from backups. |
| **Music** | Cover art, title, artist, progress and ⏮ ⏯ ⏭ controls for the active media session (Spotify, VLC, YouTube Music, radio, …). |
| **Notifications** | Latest notifications (silent ones excluded) with app, time, title and text; tap to open, ✕ to dismiss, "Clear all". |
| **Style** | A "night instrument cluster" on top of Material You: speed dominates in an arc gauge, tonal graphite surfaces, a single backlight (the dynamic wallpaper colour), amber warning lights, numbers in Barlow Semi Condensed (a font inspired by road signage, OFL licence). Follows the system light/dark theme. |
| **Footer** | Apps · Navigation · Phone · up to 3 pinned apps (long-press an app in the drawer) · Voice assistant. Settings are in the app drawer (⚙). |

Orientation is locked to portrait (`screenOrientation="portrait"` plus a runtime request).

## First launch

1. Grant the **Location** permission (speed and weather).
2. Grant **Notification access** (needed for notifications, music controls and CoMaps maneuvers).
   On Android 13+ with a manually installed APK: *Settings → Apps → Car Launcher → ⋮ → Allow restricted settings*, then try again.
3. *Settings → Set as default launcher* and choose **Car Launcher**.
4. Optional: in the settings (app drawer → ⚙) set the **backup city** for the weather.

CoMaps must be installed separately (e.g. from [F-Droid](https://f-droid.org/packages/app.comaps.fdroid/)). Destinations are passed to CoMaps through `geo:` URIs.

## Download the APK

Every push is built by GitHub Actions (*Actions → Build APK → Artifacts*).

## Build locally (Arch Linux and derivatives)

```bash
sudo pacman -S --needed jdk17-openjdk android-tools
# Android SDK from the AUR (e.g. with yay)
yay -S android-sdk-cmdline-tools-latest android-sdk-platform-tools android-platform
export ANDROID_HOME=/opt/android-sdk
sdkmanager --licenses

./gradlew testDebugUnitTest lintDebug assembleDebug   # tests, lint and APK
./gradlew recordPaparazziDebug                        # screenshots in app/src/test/snapshots/images
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Project structure

```
app/src/main/java/com/carlauncherthatsucks/
├── MainActivity.kt            # HOME activity, portrait only
├── LauncherViewModel.kt       # starts/stops GPS, media, weather
├── data/
│   ├── CarNotificationListener.kt  # notifications + CoMaps maneuvers
│   ├── NotificationStore.kt
│   ├── MediaRepository.kt     # MediaSessionManager
│   ├── LocationRepository.kt  # GPS speed (LocationManager)
│   ├── WeatherRepository.kt   # Open-Meteo
│   ├── CoMaps.kt              # intents to CoMaps
│   ├── AppsRepository.kt
│   └── Prefs.kt
└── ui/                        # Jetpack Compose
```
