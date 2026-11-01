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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
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
    onRequestLocation: () -> Unit,
    onNotificationAccess: () -> Unit,
    onDefaultLauncher: () -> Unit,
    onAppInfo: () -> Unit,
    onClose: () -> Unit,
) {
    var home by remember { mutableStateOf(settings.homeAddress) }
    var work by remember { mutableStateOf(settings.workAddress) }

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

        SectionTitle("Destinazioni preferite (CoMaps)")
        Text(
            "Scrivi un indirizzo (es. \"Via Roma 1, Milano\") oppure delle coordinate (es. \"45.4642, 9.1900\").",
            fontSize = 13.sp,
            color = CarColors.TextSecondary,
        )
        OutlinedTextField(
            value = home,
            onValueChange = {
                home = it
                prefs.update { s -> s.copy(homeAddress = it) }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Casa") },
            leadingIcon = { Icon(Icons.Filled.Home, null) },
        )
        OutlinedTextField(
            value = work,
            onValueChange = {
                work = it
                prefs.update { s -> s.copy(workAddress = it) }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = { Text("Lavoro") },
            leadingIcon = { Icon(Icons.Filled.Work, null) },
        )
        if (settings.recentDestinations.isNotEmpty()) {
            TextButton(onClick = { prefs.update { it.copy(recentDestinations = emptyList()) } }) {
                Text("Cancella cronologia ricerche")
            }
        }

        SectionTitle("Schermo")
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
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
            .clip(RoundedCornerShape(14.dp))
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
            .clip(RoundedCornerShape(14.dp))
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
        containerColor = CarColors.Card,
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
