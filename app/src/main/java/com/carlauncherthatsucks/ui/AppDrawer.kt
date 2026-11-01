package com.carlauncherthatsucks.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncherthatsucks.data.AppInfo
import com.carlauncherthatsucks.data.Prefs

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppDrawer(
    apps: List<AppInfo>,
    dock: List<String>,
    onLaunch: (AppInfo) -> Unit,
    onToggleDock: (AppInfo) -> Unit,
    onSettings: () -> Unit,
    onClose: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val filtered = if (query.isBlank()) apps
    else apps.filter { it.label.contains(query.trim(), ignoreCase = true) }

    Column(
        Modifier
            .fillMaxSize()
            .background(CarColors.Background)
            .blockTouches()
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                placeholder = { Text("Cerca app") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
            )
            IconButton(onClick = onSettings, modifier = Modifier.size(56.dp)) {
                Icon(Icons.Filled.Settings, "Impostazioni", modifier = Modifier.size(28.dp))
            }
            IconButton(onClick = onClose, modifier = Modifier.size(60.dp)) {
                Icon(Icons.Filled.Close, "Chiudi", modifier = Modifier.size(32.dp))
            }
        }
        Text(
            "Tieni premuto un'app per fissarla nella barra in basso (max ${Prefs.MAX_DOCK})",
            fontSize = 12.sp,
            color = CarColors.TextSecondary,
            modifier = Modifier.padding(vertical = 6.dp),
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(filtered, key = { it.id }) { app ->
                val pinned = app.id in dock
                Column(
                    Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (pinned) CarColors.AccentDim else CarColors.Background)
                        .combinedClickable(
                            onClick = { onLaunch(app) },
                            onLongClick = { onToggleDock(app) },
                        )
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box {
                        val icon = app.icon
                        if (icon != null) {
                            Image(icon.asImageBitmap(), null, modifier = Modifier.size(56.dp))
                        } else {
                            Box(
                                Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(CarColors.CardHigh)
                            )
                        }
                        if (pinned) {
                            Icon(
                                Icons.Filled.PushPin, "Fissata",
                                tint = CarColors.Accent,
                                modifier = Modifier
                                    .size(18.dp)
                                    .align(Alignment.TopEnd),
                            )
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        app.label,
                        fontSize = 13.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
