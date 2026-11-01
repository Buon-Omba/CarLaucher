package com.carlauncherthatsucks.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncherthatsucks.data.Contacts
import com.carlauncherthatsucks.data.FavoriteContact
import com.carlauncherthatsucks.data.PhoneContact
import com.carlauncherthatsucks.data.Prefs
import com.carlauncherthatsucks.data.initialsOf

private enum class PhoneTab { FAVORITES, KEYPAD }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneScreen(
    favorites: List<FavoriteContact>,
    contacts: List<PhoneContact>,
    contactsPermission: Boolean,
    callPermission: Boolean,
    onLoadContacts: () -> Unit,
    onPermissionsChanged: () -> Unit,
    onToggleFavorite: (FavoriteContact) -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    var tab by remember { mutableStateOf(PhoneTab.FAVORITES) }
    var editing by remember { mutableStateOf(false) }
    var picking by remember { mutableStateOf(false) }

    val permissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        onPermissionsChanged()
        onLoadContacts()
    }
    val askPermissions = {
        permissions.launch(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE))
    }
    // Primo ingresso: chiediamo subito contatti e chiamate se mancano.
    LaunchedEffect(Unit) {
        if (!contactsPermission || !callPermission) askPermissions() else onLoadContacts()
    }

    if (picking) {
        ContactPicker(
            contacts = contacts,
            favorites = favorites,
            onToggle = onToggleFavorite,
            onClose = { picking = false },
        )
        return
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .blockTouches()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Telefono", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
            if (tab == PhoneTab.FAVORITES && contactsPermission) {
                IconButton(onClick = { editing = !editing }, modifier = Modifier.size(56.dp)) {
                    Icon(
                        if (editing) Icons.Filled.CheckCircle else Icons.Filled.Edit,
                        if (editing) "Fine" else "Modifica preferiti",
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            IconButton(onClick = onClose, modifier = Modifier.size(60.dp)) {
                Icon(Icons.Filled.Close, "Chiudi", modifier = Modifier.size(32.dp))
            }
        }

        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = tab == PhoneTab.FAVORITES,
                onClick = { tab = PhoneTab.FAVORITES },
                shape = SegmentedButtonDefaults.itemShape(0, 2),
                icon = { Icon(Icons.Filled.Star, null) },
            ) { Text("Preferiti") }
            SegmentedButton(
                selected = tab == PhoneTab.KEYPAD,
                onClick = { tab = PhoneTab.KEYPAD; editing = false },
                shape = SegmentedButtonDefaults.itemShape(1, 2),
                icon = { Icon(Icons.Filled.Dialpad, null) },
            ) { Text("Tastierino") }
        }

        if (!callPermission || !contactsPermission) {
            CarCard(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondaryContainer, padding = 12) {
                Text(
                    when {
                        !contactsPermission -> "Consenti l'accesso ai contatti per scegliere i preferiti e vederne le foto."
                        else -> "Consenti le chiamate per telefonare con un tocco (senza, si apre il compositore)."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = askPermissions) { Text("Consenti") }
            }
        }

        when (tab) {
            PhoneTab.FAVORITES -> FavoritesGrid(
                favorites = favorites,
                editing = editing,
                canAdd = contactsPermission,
                onCall = { Contacts.call(context, it.number) },
                onRemove = onToggleFavorite,
                onAdd = {
                    onLoadContacts()
                    picking = true
                },
                modifier = Modifier.weight(1f),
            )
            PhoneTab.KEYPAD -> Keypad(
                onCall = { Contacts.call(context, it) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun FavoritesGrid(
    favorites: List<FavoriteContact>,
    editing: Boolean,
    canAdd: Boolean,
    onCall: (FavoriteContact) -> Unit,
    onRemove: (FavoriteContact) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (favorites.isEmpty() && !editing) {
        Column(
            modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Filled.Contacts, null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            Text("Nessun preferito", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(12.dp))
            if (canAdd) FilledTonalButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, null)
                Spacer(Modifier.width(6.dp))
                Text("Aggiungi contatti")
            }
        }
        return
    }
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(favorites, key = { it.id }) { fav ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box {
                    Surface(
                        onClick = { if (editing) onRemove(fav) else onCall(fav) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(36.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                    ) {
                        ContactPhoto(fav.photoUri, fav.name, size = null)
                    }
                    if (editing) {
                        Surface(
                            onClick = { onRemove(fav) },
                            modifier = Modifier
                                .size(34.dp)
                                .align(Alignment.TopEnd),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError,
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Filled.Close, "Rimuovi", modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    fav.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
        if (editing && canAdd && favorites.size < Prefs.MAX_FAVORITES) {
            item(key = "add") {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        onClick = onAdd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(36.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Add, "Aggiungi", modifier = Modifier.size(44.dp))
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Aggiungi", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/** Foto del contatto (o iniziali su sfondo tonale). [size] null = riempie il contenitore. */
@Composable
private fun ContactPhoto(photoUri: String?, name: String, size: Dp?) {
    val context = LocalContext.current
    var photo by remember(photoUri) { mutableStateOf<android.graphics.Bitmap?>(null) }
    LaunchedEffect(photoUri) { photo = Contacts.loadPhoto(context, photoUri) }
    val m = if (size != null) Modifier.size(size).clip(CircleShape) else Modifier.fillMaxSize()
    val bmp = photo
    if (bmp != null) {
        Image(bmp.asImageBitmap(), contentDescription = name, contentScale = ContentScale.Crop, modifier = m)
    } else {
        Box(
            m.background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                initialsOf(name),
                fontSize = if (size != null) 18.sp else 34.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

@Composable
private fun ContactPicker(
    contacts: List<PhoneContact>,
    favorites: List<FavoriteContact>,
    onToggle: (FavoriteContact) -> Unit,
    onClose: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val q = query.trim()
    val filtered = if (q.isEmpty()) contacts else contacts.filter {
        it.name.contains(q, ignoreCase = true) || it.number.contains(q)
    }
    val favIds = favorites.map { it.id }.toSet()
    val scheme = MaterialTheme.colorScheme

    Column(
        Modifier
            .fillMaxSize()
            .background(scheme.surface)
            .blockTouches()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Scegli i preferiti", style = MaterialTheme.typography.headlineSmall)
                Text(
                    "${favorites.size} di ${Prefs.MAX_FAVORITES}",
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onClose, modifier = Modifier.size(60.dp)) {
                Icon(Icons.Filled.CheckCircle, "Fine", modifier = Modifier.size(32.dp), tint = scheme.primary)
            }
        }
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            placeholder = { Text("Cerca contatto") },
            leadingIcon = { Icon(Icons.Filled.Search, null) },
        )
        if (contacts.isEmpty()) {
            Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Text("Nessun contatto con numero di telefono", color = scheme.onSurfaceVariant)
            }
            return@Column
        }
        LazyColumn(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(24.dp)),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            itemsIndexed(filtered, key = { _, c -> c.id }) { index, c ->
                val selected = c.id in favIds
                Surface(
                    onClick = { onToggle(c.toFavorite()) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = segmentedShape(index, filtered.size),
                    color = if (selected) scheme.primaryContainer else scheme.surfaceContainerHigh,
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ContactPhoto(c.photoUri, c.name, size = 44.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${c.typeLabel} · ${c.number}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = scheme.onSurfaceVariant,
                                maxLines = 1,
                            )
                        }
                        Icon(
                            if (selected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            null,
                            tint = if (selected) scheme.primary else scheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Keypad(onCall: (String) -> Unit, modifier: Modifier = Modifier) {
    var number by remember { mutableStateOf("") }
    val scheme = MaterialTheme.colorScheme
    val keys = listOf(
        "1" to "", "2" to "ABC", "3" to "DEF",
        "4" to "GHI", "5" to "JKL", "6" to "MNO",
        "7" to "PQRS", "8" to "TUV", "9" to "WXYZ",
        "*" to "", "0" to "+", "#" to "",
    )
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(72.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                number,
                fontSize = 36.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            if (number.isNotEmpty()) {
                IconButton(onClick = { number = number.dropLast(1) }, modifier = Modifier.size(60.dp)) {
                    Icon(Icons.AutoMirrored.Filled.Backspace, "Cancella", modifier = Modifier.size(28.dp))
                }
            }
        }
        keys.chunked(3).forEach { row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                row.forEach { (digit, letters) ->
                    Surface(
                        onClick = { if (number.length < 20) number += digit },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        shape = RoundedCornerShape(28.dp),
                        color = scheme.surfaceContainerHigh,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(digit, fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
                            if (letters.isNotEmpty()) {
                                Text(letters, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
        Surface(
            onClick = { if (number.isNotBlank()) onCall(number) },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            shape = RoundedCornerShape(36.dp),
            color = if (number.isNotBlank()) Color(0xFF1E8E3E) else scheme.surfaceContainerHighest,
            contentColor = if (number.isNotBlank()) Color.White else scheme.onSurfaceVariant,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Call, "Chiama", modifier = Modifier.size(36.dp))
            }
        }
    }
}
