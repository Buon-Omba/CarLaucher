# CarLaucherThatSucks
A vibe-coded phone car laucher that as minimal feature because my car doesn't support anything ad i don't want to pay anything.

Launcher Android per l'auto, **solo verticale**, tema scuro e pulsanti grandi. Niente Google Play Services, niente account, niente chiavi API.

## Funzioni

| Sezione | Cosa fa |
|---|---|
| **Velocità** | km/h letti dal GPS del telefono (con stima da distanza/tempo se il chip non fornisce la velocità). |
| **Meteo** | Icona + temperatura da [Open-Meteo](https://open-meteo.com) (gratuito, senza chiave). Usa GPS, rete, "fused" e posizione passiva; se non c'è nessuna posizione usa la **città di riserva** impostata nelle impostazioni. Tocca per aggiornare. |
| **Navigazione (CoMaps)** | Pulsante nel footer: se CoMaps sta navigando apre la **modalità guida** — CoMaps a schermo intero (mappa, percorso, indicazioni) con sopra un pannello trascinabile con velocità e controlli musicali e un tasto per tornare alla home. Altrimenti apre la ricerca della destinazione (e poi la modalità guida). Richiede il permesso "mostra sopra altre app". |
| **Telefono** | Integrato nel launcher: griglia di contatti preferiti con foto (scelti dalla rubrica) e tastierino. Un tocco = chiamata. |
| **Claude** | Pulsante nel footer per la chat vocale: avvia la scorciatoia vocale dell'app Claude se disponibile, altrimenti apre l'app. |
| **Musica** | Copertina, titolo, artista, avanzamento e controlli ⏮ ⏯ ⏭ della sessione multimediale attiva (Spotify, VLC, YouTube Music, radio, …). |
| **Notifiche** | Ultime notifiche (escluse quelle silenziose) con app, ora, titolo e testo; tocca per aprirle, ✕ per eliminarle, “Cancella tutte”. |
| **Stile** | "Quadro strumenti di notte" su base Material You: la velocità domina in un quadrante ad arco, superfici grafite tonali, una sola retroilluminazione (il colore dinamico dello sfondo), spie ambra per gli avvisi, numeri in Barlow Semi Condensed (font ispirato alla segnaletica stradale, licenza OFL). Tema chiaro/scuro di sistema. |
| **Footer** | App · Navigazione · Telefono · fino a 3 app fissate (tieni premuta un'app nel drawer) · Claude. Le impostazioni sono nel drawer delle app (⚙). |

L'orientamento è bloccato in verticale (`screenOrientation="portrait"` + richiesta a runtime).

## Primo avvio

1. Concedi il permesso **Posizione** (velocità e meteo).
2. Concedi l'**Accesso alle notifiche** (serve per notifiche, controlli musica e manovre di CoMaps).
   Su Android 13+ con APK installato a mano: *Impostazioni → App → Car Launcher → ⋮ → Consenti impostazioni con restrizioni*, poi riprova.
3. *Impostazioni → Imposta come launcher predefinito* e scegli **Car Launcher**.
4. Opzionale: in impostazioni (drawer app → ⚙) imposta la **città di riserva** per il meteo.

CoMaps va installato a parte (es. da [F-Droid](https://f-droid.org/packages/app.comaps.fdroid/)). Le destinazioni vengono passate a CoMaps tramite URI `geo:`.

## Scaricare l'APK

Ogni push viene compilato da GitHub Actions (*Actions → Build APK → Artifacts*).

## Compilare in locale (Arch Linux e derivate)

```bash
sudo pacman -S --needed jdk17-openjdk android-tools
# SDK Android dall'AUR (es. con yay)
yay -S android-sdk-cmdline-tools-latest android-sdk-platform-tools android-platform
export ANDROID_HOME=/opt/android-sdk
sdkmanager --licenses

./gradlew testDebugUnitTest lintDebug assembleDebug   # test, lint e APK
./gradlew recordPaparazziDebug                        # screenshot in app/src/test/snapshots/images
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Struttura

```
app/src/main/java/com/carlauncherthatsucks/
├── MainActivity.kt            # activity HOME, solo verticale
├── LauncherViewModel.kt       # avvia/ferma GPS, media, meteo
├── data/
│   ├── CarNotificationListener.kt  # notifiche + manovre CoMaps
│   ├── NotificationStore.kt
│   ├── MediaRepository.kt     # MediaSessionManager
│   ├── LocationRepository.kt  # velocità GPS (LocationManager)
│   ├── WeatherRepository.kt   # Open-Meteo
│   ├── CoMaps.kt              # intent verso CoMaps
│   ├── AppsRepository.kt
│   └── Prefs.kt
└── ui/                        # Jetpack Compose
```
