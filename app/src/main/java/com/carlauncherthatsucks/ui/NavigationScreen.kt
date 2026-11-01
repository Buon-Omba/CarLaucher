package com.carlauncherthatsucks.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncherthatsucks.data.NavigationInfo
import com.carlauncherthatsucks.data.SpeedState

/**
 * Pagina di guida dentro il launcher: manovra di CoMaps a tutto schermo + velocità.
 * (La mappa di un'altra app non può essere incorporata: per quella c'è "Apri mappa".)
 */
@Composable
fun NavigationScreen(
    navigation: NavigationInfo?,
    speed: SpeedState,
    onOpenMap: () -> Unit,
    onClose: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxSize()
            .background(scheme.surface)
            .blockTouches()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Navigazione", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            IconButton(onClick = onClose, modifier = Modifier.size(60.dp)) {
                Icon(Icons.Filled.Close, "Chiudi", modifier = Modifier.size(32.dp))
            }
        }

        CarCard(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            onClick = onOpenMap,
            color = scheme.primary,
            shape = MaterialTheme.shapes.extraLarge,
            padding = 20,
        ) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (navigation == null) {
                    Text(
                        "Navigazione terminata",
                        style = MaterialTheme.typography.headlineMedium,
                        textAlign = TextAlign.Center,
                    )
                    return@Column
                }
                Box(
                    Modifier
                        .size(180.dp)
                        .clip(RoundedCornerShape(56.dp))
                        .background(scheme.onPrimary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    val icon = navigation.turnIcon
                    if (icon != null) {
                        Image(icon.asImageBitmap(), contentDescription = null, modifier = Modifier.size(150.dp))
                    } else {
                        Icon(Icons.Filled.Navigation, null, modifier = Modifier.size(110.dp))
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    navigation.title.ifBlank { "Navigazione in corso" },
                    fontSize = 56.sp,
                    lineHeight = 60.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (navigation.text.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        navigation.text,
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (navigation.subText.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        navigation.subText,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }

        Row(
            Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SpeedIndicator(speed, Modifier.weight(1f))
            Button(
                onClick = onOpenMap,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize(),
                shape = RoundedCornerShape(36.dp),
            ) {
                Icon(Icons.Filled.Map, null, modifier = Modifier.size(32.dp))
                Spacer(Modifier.width(8.dp))
                Text("Apri mappa", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
