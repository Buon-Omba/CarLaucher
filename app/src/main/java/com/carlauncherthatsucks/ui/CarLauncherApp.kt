package com.carlauncherthatsucks.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.carlauncherthatsucks.LauncherViewModel
import com.carlauncherthatsucks.data.AppsRepository
import com.carlauncherthatsucks.data.CoMaps
import com.carlauncherthatsucks.data.NotificationStore
import com.carlauncherthatsucks.data.Prefs
import com.carlauncherthatsucks.data.ClaudeAssistant
import com.carlauncherthatsucks.data.sendFromForeground

private enum class Overlay { NONE, APPS, SETTINGS, PHONE, NAVIGATION }

@Composable
fun CarLauncherApp(
    vm: LauncherViewModel,
    homePresses: Int,
    requestLocationPermission: () -> Unit,
) {
    val context = LocalContext.current
    var overlay by remember { mutableStateOf(Overlay.NONE) }
    var showSearch by remember { mutableStateOf(false) }

    val settings by vm.prefs.settings.collectAsStateWithLifecycle()
    val speed by vm.location.speed.collectAsStateWithLifecycle()
    val weather by vm.weather.collectAsStateWithLifecycle()
    val weatherStatus by vm.weatherStatus.collectAsStateWithLifecycle()
    val contacts by vm.contacts.collectAsStateWithLifecycle()
    val contactsPermission by vm.contactsPermission.collectAsStateWithLifecycle()
    val callPermission by vm.callPermission.collectAsStateWithLifecycle()
    val media by vm.media.state.collectAsStateWithLifecycle()
    val navigation by NotificationStore.navigation.collectAsStateWithLifecycle()
    val notifications by NotificationStore.notifications.collectAsStateWithLifecycle()
    val notificationAccess by vm.notificationAccess.collectAsStateWithLifecycle()
    val locationPermission by vm.locationPermission.collectAsStateWithLifecycle()
    val listenerConnected by NotificationStore.listenerConnected.collectAsStateWithLifecycle()
    val apps by vm.apps.collectAsStateWithLifecycle()

    // Il tasto Home riporta sempre alla schermata principale.
    LaunchedEffect(homePresses) {
        overlay = Overlay.NONE
        showSearch = false
    }
    // Appena il listener si collega, possiamo leggere anche le sessioni multimediali.
    LaunchedEffect(listenerConnected) {
        if (listenerConnected) {
            vm.refreshPermissions()
            vm.media.start()
        }
    }

    val phonePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { vm.loadContacts() }

    val openMap = {
        val pi = navigation?.contentIntent
        if (pi == null || !pi.sendFromForeground(context)) CoMaps.open(context)
    }

    // Il launcher non si "chiude" con Indietro: chiude solo i pannelli aperti.
    BackHandler(enabled = true) { overlay = Overlay.NONE }

    val dockApps = settings.dockApps.mapNotNull { id -> apps.firstOrNull { it.id == id } }
        .take(Prefs.MAX_DOCK)

    Surface(color = CarColors.Background, contentColor = CarColors.TextPrimary) {
    Box(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!locationPermission || !notificationAccess) {
                PermissionBanner(
                    needsLocation = !locationPermission,
                    needsNotifications = !notificationAccess,
                    onLocation = requestLocationPermission,
                    onNotifications = { openNotificationAccess(context) },
                )
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SpeedIndicator(speed = speed, modifier = Modifier.weight(1.15f))
                Column(
                    Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ClockCard(Modifier.weight(1f))
                    WeatherIndicator(
                        weather = weather,
                        status = weatherStatus,
                        onClick = { vm.refreshWeatherNow() },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            navigation?.let { nav ->
                NavigationCard(navigation = nav, onClick = { overlay = Overlay.NAVIGATION })
            }

            MediaCard(
                media = media,
                hasAccess = notificationAccess,
                onPlayPause = vm.media::playPause,
                onNext = vm.media::next,
                onPrevious = vm.media::previous,
                onOpen = vm.media::openPlayer,
            )

            NotificationsCard(
                notifications = notifications,
                hasAccess = notificationAccess,
                onGrantAccess = { openNotificationAccess(context) },
                onOpen = { n ->
                    val ok = n.contentIntent?.sendFromForeground(context) == true
                    if (!ok) {
                        context.packageManager.getLaunchIntentForPackage(n.packageName)?.let {
                            context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    }
                    if (n.clearable) NotificationStore.dismiss(n.key)
                },
                onDismiss = { NotificationStore.dismiss(it.key) },
                onDismissAll = { NotificationStore.dismissAll() },
                modifier = Modifier.weight(1f),
            )

            Dock(
                dockApps = dockApps,
                navigationActive = navigation != null,
                onApps = { overlay = Overlay.APPS },
                onNavigation = {
                    if (navigation != null) overlay = Overlay.NAVIGATION else showSearch = true
                },
                onPhone = { overlay = Overlay.PHONE },
                onClaude = { ClaudeAssistant.startVoice(context) },
                onLaunch = { AppsRepository.launch(context, it) },
            )
        }

        when (overlay) {
            Overlay.APPS -> AppDrawer(
                apps = apps,
                dock = settings.dockApps,
                onLaunch = {
                    AppsRepository.launch(context, it)
                    overlay = Overlay.NONE
                },
                onToggleDock = { vm.prefs.toggleDock(it.id) },
                onSettings = { overlay = Overlay.SETTINGS },
                onClose = { overlay = Overlay.NONE },
            )
            Overlay.SETTINGS -> SettingsScreen(
                settings = settings,
                prefs = vm.prefs,
                notificationAccess = notificationAccess,
                locationPermission = locationPermission,
                contactsPermission = contactsPermission,
                callPermission = callPermission,
                onRequestLocation = requestLocationPermission,
                onRequestPhone = {
                    phonePermissions.launch(
                        arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE)
                    )
                },
                onSetWeatherCity = { vm.setWeatherCity(it) },
                onNotificationAccess = { openNotificationAccess(context) },
                onDefaultLauncher = { openHomeSettings(context) },
                onAppInfo = { openAppInfo(context) },
                onClose = { overlay = Overlay.NONE },
            )
            Overlay.PHONE -> PhoneScreen(
                favorites = settings.favoriteContacts,
                contacts = contacts,
                contactsPermission = contactsPermission,
                callPermission = callPermission,
                onLoadContacts = { vm.loadContacts() },
                onPermissionsChanged = { vm.refreshPermissions() },
                onToggleFavorite = { vm.prefs.toggleFavorite(it) },
                onClose = { overlay = Overlay.NONE },
            )
            Overlay.NAVIGATION -> NavigationScreen(
                navigation = navigation,
                speed = speed,
                onOpenMap = openMap,
                onClose = { overlay = Overlay.NONE },
            )
            Overlay.NONE -> Unit
        }

        if (showSearch) {
            DestinationDialog(
                recents = settings.recentDestinations,
                onSearch = { q ->
                    vm.prefs.addRecent(q)
                    showSearch = false
                    CoMaps.search(context, q)
                },
                onDismiss = { showSearch = false },
            )
        }
    }
    }
}

private fun safeStart(context: Context, intent: Intent) {
    try {
        context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: Exception) {
        Toast.makeText(context, "Impossibile aprire", Toast.LENGTH_SHORT).show()
    }
}

private fun openNotificationAccess(context: Context) =
    safeStart(context, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))

private fun openHomeSettings(context: Context) =
    safeStart(context, Intent(Settings.ACTION_HOME_SETTINGS))

private fun openAppInfo(context: Context) = safeStart(
    context,
    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + context.packageName))
)

