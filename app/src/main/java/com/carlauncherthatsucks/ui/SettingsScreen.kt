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
            .background(CarColors.Background)
            .blockTouches()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Impostazioni", fontSize = 26.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = onClose, modifier = Modifier.size(60.dp)) {
                Icon(Icons.Filled.Close, "Chiudi", modifier = Modifier.size(32.dp))
            }
        }

        SectionTitle("Meteo")
        Text(
            "Il meteo usa la tua posizione. Se il telefono non ne trova una (GPS al chiuso, nessun " +
                "servizio di localizzazione di rete), usa questa città.",
            fontSize = 13.sp,
            color = CarColors.TextSecondary,
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
                .clip(RoundedCornerShape(24.dp))
                .background(CarColors.Card)
                .clickable { prefs.update { it.copy(keepScreenOn = !it.keepScreenOn) } }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text("Mantieni lo schermo acceso", fontSize = 16.sp)
                Text(
                    "Mentre il launcher è in primo piano",
                    fontSize = 13.sp,
                    color = CarColors.TextSecondary,
                )
            }
            Switch(
                checked = settings.keepScreenOn,
                onCheckedChange = { v -> prefs.update { it.copy(keepScreenOn = v) } },
            )
        }

        SectionTitle("Permessi e sistema")
        StatusRow("Posizione (velocità e meteo)", locationPermission, onRequestLocation)
        StatusRow("Accesso alle notifiche (notifiche, musica, navigazione)", notificationAccess, onNotificationAccess)
        StatusRow("Contatti e chiamate (telefono)", contactsPermission && callPermission, onRequestPhone)
        LinkRow("Imposta come launcher predefinito", onDefaultLauncher)
        LinkRow("Informazioni app / permessi", onAppInfo)
        Text(
            "Se l'accesso alle notifiche è disattivato e non si attiva: apri \"Informazioni app\", " +
                "menu ⋮ → \"Consenti impostazioni con restrizioni\" (Android 13+ per le app installate da APK).",
            fontSize = 12.sp,
            color = CarColors.TextSecondary,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        fontSize = 15.sp,
        fontWeight = FontWeight.SemiBold,
        color = CarColors.Accent,
        modifier = Modifier.padding(top = 8.dp),
    )
}

@Composable
private fun StatusRow(label: String, ok: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(CarColors.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (ok) Icons.Filled.CheckCircle else Icons.Filled.Warning,
            null,
            tint = if (ok) CarColors.Accent else CarColors.Warning,
        )
        Spacer(Modifier.width(12.dp))
        Text(label, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, null, tint = CarColors.TextSecondary)
    }
}

@Composable
private fun LinkRow(label: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(CarColors.Card)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Filled.ChevronRight, null, tint = CarColors.TextSecondary)
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
                        Icon(Icons.Filled.History, null, tint = CarColors.TextSecondary)
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
