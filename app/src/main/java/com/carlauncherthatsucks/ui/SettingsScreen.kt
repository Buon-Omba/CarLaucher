package com.carlauncherthatsucks.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncherthatsucks.data.Prefs
import com.carlauncherthatsucks.data.Settings

@Composable
fun SettingsScreen(
    settings: Settings,
    prefs: Prefs,
    notificationAccess: Boolean,
    locationPermission: Boolean,
    contactsPermission: Boolean,
    callPermission: Boolean,
    onRequestLocation: () -> Unit,
    onRequestPhone: () -> Unit,
    onSetWeatherCity: suspend (String) -> Boolean,
    onNotificationAccess: () -> Unit,
    overlayPermission: Boolean,
    onOverlayPermission: () -> Unit,
    onDefaultLauncher: () -> Unit,
    onAppInfo: () -> Unit,
    onClose: () -> Unit,
) {
    var city by remember { mutableStateOf(settings.weatherPlace?.label?.substringBefore(",").orEmpty()) }
    var cityError by remember { mutableStateOf(false) }
    var searchingCity by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val saveCity = {
        searchingCity = true
        scope.launch {
            cityError = !onSetWeatherCity(city)
            searchingCity = false
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Dash.Asphalt)
            .blockTouches()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Impostazioni", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
            IconButton(onClick = onClose, modifier = Modifier.size(60.dp)) {
                Icon(Icons.Filled.Close, "Chiudi", modifier = Modifier.size(32.dp))
            }
        }

        SectionTitle("Meteo")
        Text(
            "Il meteo usa la tua posizione. Se il telefono non ne trova una (GPS al chiuso, nessun " +
                "servizio di localizzazione di rete), usa questa città.",
            style = MaterialTheme.typography.bodyMedium,
            color = Dash.Label,
        )
        OutlinedTextField(
            value = city,
            onValueChange = {
                city = it
                cityError = false
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(20.dp),
            label = { Text("Città di riserva") },
            leadingIcon = { Icon(Icons.Filled.LocationCity, null) },
            isError = cityError,
            supportingText = {
                Text(
                    when {
                        searchingCity -> "Ricerca…"
                        cityError -> "Città non trovata o nessuna connessione"
                        settings.weatherPlace != null -> settings.weatherPlace.label
                        else -> "Nessuna città impostata"
                    }
                )
            },
            trailingIcon = {
                IconButton(onClick = { saveCity() }) { Icon(Icons.Filled.Check, "Salva") }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { saveCity() }),
        )

        if (settings.recentDestinations.isNotEmpty()) {
            SectionTitle("Navigazione")
            TextButton(onClick = { prefs.update { it.copy(recentDestinations = emptyList()) } }) {
                Text("Cancella cronologia ricerche")
            }
        }

        SectionTitle("Schermo")
        Row(
            Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.medium)
                .background(Dash.Panel)
                .clickable { prefs.update { it.copy(keepScreenOn = !it.keepScreenOn) } }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Mantieni lo schermo acceso", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Mentre il launcher è in primo piano",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Dash.Label,
                )
            }
            Switch(
                checked = settings.keepScreenOn,
                onCheckedChange = { v -> prefs.update { it.copy(keepScreenOn = v) } },
            )
        }

        SectionTitle("Permessi")
        val permissions = listOf(
            Triple("Posizione", "Velocità e meteo", locationPermission) to onRequestLocation,
            Triple("Accesso alle notifiche", "Notifiche, musica, manovre di CoMaps", notificationAccess) to onNotificationAccess,
            Triple("Contatti e chiamate", "Preferiti del telefono", contactsPermission && callPermission) to onRequestPhone,
            Triple("Mostra sopra altre app", "Pannello guida sopra CoMaps", overlayPermission) to onOverlayPermission,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            permissions.forEachIndexed { i, (info, action) ->
                StatusRow(info.first, info.second, info.third, segmentedShape(i, permissions.size), action)
            }
        }
        SectionTitle("Sistema")
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            LinkRow("Imposta come launcher predefinito", segmentedShape(0, 2), onDefaultLauncher)
            LinkRow("Informazioni app", segmentedShape(1, 2), onAppInfo)
        }
        Text(
            "Se l'accesso alle notifiche è disattivato e non si attiva: apri \"Informazioni app\", " +
                "menu ⋮ → \"Consenti impostazioni con restrizioni\" (Android 13+ per le app installate da APK).",
            style = MaterialTheme.typography.bodySmall,
            color = Dash.Note,
        )
    }
}

/** Intestazione di sezione: piccola, spaziata, maiuscola — come le scritte del cruscotto. */
@Composable
private fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Dash.Label,
        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun StatusRow(title: String, detail: String, ok: Boolean, shape: Shape, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = shape, color = Dash.Panel) {
        Row(
            Modifier
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Spia: ambra se manca, spenta se è tutto a posto.
            if (ok) {
                Icon(Icons.Filled.CheckCircle, "Concesso", tint = Dash.Label, modifier = Modifier.size(20.dp))
            } else {
                Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) { Lamp(size = 10.dp) }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = Dash.Numeral)
                Text(
                    if (ok) detail else "Da concedere · $detail",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (ok) Dash.Label else Dash.WarningLamp,
                )
            }
            Icon(Icons.Filled.ChevronRight, null, tint = Dash.Note)
        }
    }
}

@Composable
private fun LinkRow(label: String, shape: Shape, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = shape, color = Dash.Panel) {
        Row(
            Modifier
                .heightIn(min = 56.dp)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, null, tint = Dash.Note)
        }
    }
}

@Composable
fun DestinationDialog(
    recents: List<String>,
    onSearch: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        try {
            focus.requestFocus()
        } catch (_: Exception) {
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dove andiamo?") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focus),
                    singleLine = true,
                    shape = RoundedCornerShape(28.dp),
                    placeholder = { Text("Indirizzo, luogo, categoria…") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = {
                        if (query.isNotBlank()) onSearch(query)
                    }),
                )
                recents.forEach { r ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSearch(r) }
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Filled.History, null, tint = Dash.Label)
                        Spacer(Modifier.width(10.dp))
                        Text(r, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSearch(query) }, enabled = query.isNotBlank()) {
                Text("Cerca in CoMaps")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annulla") }
        },
    )
}
