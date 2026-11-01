# Design system — CarLauncherThatSucks

## Direzione

**Quadro strumenti di notte.** Chi guarda è un guidatore con il telefono sul cruscotto, a un braccio di distanza, che dà un'occhiata di mezzo secondo, di giorno e di notte. Deve: leggere la velocità, cambiare brano, vedere chi ha scritto, chiamare qualcuno.
Sensazione: calma, scura, i numeri "accesi", nulla si muove se non serve.

- Base Material You: colori dinamici dallo sfondo, tema chiaro/scuro di sistema.
- **Un solo punto focale per vista.** In home è la velocità; durante la navigazione è la manovra (il quadrante si rimpicciolisce).
- **Elemento distintivo:** il quadrante ad arco della velocità (`SpeedDial`), ripetuto in piccolo nel pannello sopra CoMaps.

## Token (`ui/Theme.kt` → `object Dash`)

Usare sempre i token, mai colori letterali (eccezioni documentate sotto).

| Token | Ruolo | Mappa su |
|---|---|---|
| `Asphalt` | fondo | `surface` |
| `Panel` | pannelli, righe di lista, footer | `surfaceContainer` |
| `PanelRaised` | elementi dentro un pannello | `surfaceContainerHigh` |
| `Track` | traccia del quadrante, monogrammi, controlli inattivi | `surfaceContainerHighest` |
| `Numeral` | testo primario, numeri | `onSurface` |
| `Label` | testo secondario, icone | `onSurfaceVariant` |
| `Note` | metadati (app · ora), icone di chiusura | `onSurfaceVariant` 72% |
| `Off` | spento/disabilitato, "--" senza GPS | `onSurface` 38% |
| `Backlight` / `OnBacklight` | **unico accento pieno**: arco del quadrante, scheda manovra | `primary` |
| `BacklightDim` / `OnBacklightDim` | accento tenue: play, tasto navigazione attivo, app fissate, "Aggiungi" | `primaryContainer` |
| `WarningLamp` | spie ambra: permessi mancanti, GPS assente | ambra (chiaro/scuro) |
| `CallGreen` | solo il pulsante "chiama" | verde fisso |
| `Sun`, `Rain` | icone meteo (hanno un significato) | fissi |

Regole colore:
- ~60/30/10: grafite / tono dei pannelli / accento. Il `Backlight` pieno compare al massimo in due punti per vista.
- Niente `secondaryContainer`/`tertiaryContainer` per decorare: un solo tono, cambia solo la luminosità.
- Avvisi = spia ambra (`Lamp()` + testo), non banner rossi. `error` solo per azioni distruttive (es. rimuovi preferito in modifica).

## Profondità

Solo **cambio di tono** tra superfici, niente bordi, niente ombre.
Unica eccezione: `DrivePanel` (`shadowElevation = 6.dp`) perché galleggia sopra la mappa di un'altra app.
Gli `OutlinedTextField` nei form (impostazioni, ricerca app) sono ammessi: bordi solo sugli input.

## Tipografia

- **Signage** = Barlow Semi Condensed (Medium/SemiBold/Bold, `res/font/barlow_*`, licenza OFL in `third_party/`) per numeri e titoli; font di sistema per il resto.
- Cifre tabulari (`tnum`) su tutti i numeri che cambiano.
- Scala ~1.25 su base 14:

| Stile | Uso | Valori |
|---|---|---|
| `displayLarge` | velocità | Signage Bold 92/92, −0.02em, tnum |
| `headlineMedium` | distanza manovra, iniziali grandi, numero digitato | Signage Bold 34/38, tnum |
| `headlineSmall` | titoli di pagina, ora, temperatura, tasti del tastierino | Signage SemiBold 26/30 |
| `titleLarge` | titolo brano | Signage SemiBold 20/24, tnum |
| `titleMedium` | titoli di riga/notifica | sistema 16/22 SemiBold |
| `titleSmall` | iniziali piccole, titoli spia | sistema 14 SemiBold |
| `bodyMedium` | testo secondario | sistema 14/20 |
| `bodySmall` | note lunghe | sistema 12/16 |
| `labelLarge` | data, pulsanti testuali, "km/h" | sistema 14 Medium |
| `labelMedium` | metadati (app · ora) | sistema 12 Medium, tnum |
| `labelSmall` | intestazioni di sezione MAIUSCOLE | sistema 11 SemiBold, +0.1em |

La gerarchia si fa con peso + colore (`Numeral`/`Label`/`Note`/`Off`), non solo con la dimensione.

## Spaziatura e forme

- Base 4dp. Margine pagina **16 orizzontale / 8 verticale** (home, telefono, impostazioni, drawer).
- Tra gruppi 12dp; righe dello stesso gruppo 4dp; dentro i pannelli padding 16 (righe 12).
- Raggi (`MaterialTheme.shapes`): small 12 (copertina, icona manovra) · medium 20 (righe, spie) · large 28 (pannelli) · extraLarge 36 (footer, pannello guida).
- **Raggi concentrici:** interno = esterno − padding (pannello 28 − 16 → 12; footer 36 − 8 → 28; selettore 28 − 4 → 24).
- Liste: `segmentedShape(index, count)` → 20 all'esterno del gruppo, 6 tra un elemento e l'altro.
- Aree di tocco ≥ 48dp (controlli principali 56dp).

## Componenti ricorrenti

- **SpeedDial** — arco 240° (inizio 150°), tacche ogni 20 km/h fino a 180, traccia `Track`, riempimento `Backlight`, altezza = 0.84 × diametro, numero al centro del cerchio, "km/h" nella fessura. Home 236dp/stroke 12; in navigazione 156dp/stroke 8/numero 60sp senza tacche; pannello guida 84dp/stroke 5/numero 32sp.
- **StatusStrip** — riga ≥48dp: ora (`headlineSmall`) + data (`labelLarge`/`Label`) · spia GPS se manca il fix · icona meteo 24 + temperatura (`headlineSmall`), toccabile.
- **CarCard** — `Surface` `Panel`, raggio large, padding 16.
- **MediaCard** — copertina 56 (raggio 12), titolo `titleLarge`, artista · app `bodyMedium`/`Label`, linea di avanzamento 3dp neutra (`Track`/`Label`), comandi: prev/next `IconButton` 72×56 senza sfondo, play 88×56 `BacklightDim` che cambia forma (16 in riproduzione, 28 in pausa).
- **NavigationCard** — `Backlight` pieno, icona manovra 64 (raggio 12, `OnBacklight` 14%), distanza `headlineMedium`.
- **NotificationsList** — intestazione `NOTIFICHE n` (`labelSmall`) + "Cancella tutte" in `Label`; righe `Panel` segmentate, avatar 40 (foto o `Monogram`), chiusura 48 con icona 20 `Note`.
- **Dock** — pillola 72dp `Panel` raggio 36, padding 8; pulsanti a peso uguale, raggio 28, icone 26 `Label`; attivo = `BacklightDim`.
- **PillTabs** — pillola 56dp `Panel` raggio 28, padding 4; scheda attiva `Track`, nessun bordo.
- **WarningLampRow / StatusRow** — `Lamp()` ambra 8–10dp + titolo; dettaglio in `WarningLamp` se manca, in `Label` se ok (icona check in `Label`, non accento).
- **VoiceChatScreen** — punto focale: pulsante microfono circolare 144dp; colore = stato (`BacklightDim` pronto, `Backlight` ascolto/lettura, `Track` attesa/errore) con anello di avanzamento 168dp mentre il modello risponde; didascalia `titleMedium`. Sotto: "TU" / "ASSISTENTE" in `labelSmall`, domanda `bodyLarge`/`Label`, risposta 20/28 `Numeral`.
- **Preset chip** — `Surface` raggio 20, altezza ≥48, `Panel` (selezionato `BacklightDim`), riga scorrevole orizzontale.
- **SectionTitle** — `labelSmall` maiuscolo `Label`, padding top 16.

## Movimento

- Curve: `EaseOutDash` (0.23, 1, 0.32, 1) per entrate/feedback; `EaseInOutDash` (0.77, 0, 0.175, 1) per movimenti sullo schermo.
- Quadrante: 300ms `EaseInOutDash`. Cambio forma play: 180ms `EaseOutDash`.
- Niente animazioni sul numero della velocità (si legge di sfuggita, cambia ogni secondo).

## Verifica

`./gradlew recordPaparazziDebug` → screenshot in `app/src/test/snapshots/images/` (home scura/chiara, in navigazione, senza permessi, pannello guida, telefono, impostazioni). Controllare dopo ogni modifica alla UI: squint test (si capisce subito cosa conta?), un solo accento pieno, spaziature sulla griglia da 4.
