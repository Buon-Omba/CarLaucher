package com.carlauncherthatsucks.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.carlauncherthatsucks.data.AppInfo
import com.carlauncherthatsucks.data.CarNotification
import com.carlauncherthatsucks.data.MediaState
import com.carlauncherthatsucks.data.NavigationInfo
import com.carlauncherthatsucks.data.SpeedState
import com.carlauncherthatsucks.data.Weather
import com.carlauncherthatsucks.data.WeatherStatus

/** Schermata principale, senza stato: riceve i dati e notifica le azioni. */
@Composable
fun HomeScreen(
    speed: SpeedState,
    weather: Weather?,
    weatherStatus: WeatherStatus,
    navigation: NavigationInfo?,
    media: MediaState?,
    notifications: List<CarNotification>,
    dockApps: List<AppInfo>,
    locationPermission: Boolean,
    notificationAccess: Boolean,
    onRequestLocation: () -> Unit,
    onNotificationAccess: () -> Unit,
    onRefreshWeather: () -> Unit,
    onOpenNavigation: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onOpenPlayer: () -> Unit,
    onOpenNotification: (CarNotification) -> Unit,
    onDismissNotification: (CarNotification) -> Unit,
    onDismissAll: () -> Unit,
    onApps: () -> Unit,
    onNavigationButton: () -> Unit,
    onPhone: () -> Unit,
    onClaude: () -> Unit,
    onLaunchApp: (AppInfo) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        StatusStrip(
            weather = weather,
            weatherStatus = weatherStatus,
            hasGpsFix = speed.hasGpsFix || !locationPermission,
            onWeatherClick = onRefreshWeather,
        )

        if (!locationPermission || !notificationAccess) {
            Spacer(Modifier.height(8.dp))
            WarningLampRow(
                needsLocation = !locationPermission,
                needsNotifications = !notificationAccess,
                onLocation = onRequestLocation,
                onNotifications = onNotificationAccess,
            )
        }

        // Punto focale: la velocità. Durante la navigazione cede il posto alla manovra.
        val navigating = navigation != null
        Box(
            Modifier
                .fillMaxWidth()
                .padding(top = if (navigating) 4.dp else 12.dp, bottom = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            SpeedDial(
                speed = speed,
                diameter = if (navigating) 156.dp else 236.dp,
                numeralStyle = if (navigating) {
                    MaterialTheme.typography.displayLarge.copy(fontSize = 60.sp, lineHeight = 60.sp)
                } else {
                    MaterialTheme.typography.displayLarge
                },
                stroke = if (navigating) 8.dp else 12.dp,
                showTicks = !navigating,
            )
        }

        navigation?.let { nav ->
            NavigationCard(navigation = nav, onClick = onOpenNavigation)
            Spacer(Modifier.height(12.dp))
        }

        MediaCard(
            media = media,
            hasAccess = notificationAccess,
            onPlayPause = onPlayPause,
            onNext = onNext,
            onPrevious = onPrevious,
            onOpen = onOpenPlayer,
        )

        Spacer(Modifier.height(12.dp))

        NotificationsList(
            notifications = notifications,
            hasAccess = notificationAccess,
            onGrantAccess = onNotificationAccess,
            onOpen = onOpenNotification,
            onDismiss = onDismissNotification,
            onDismissAll = onDismissAll,
            modifier = Modifier.weight(1f),
        )

        Spacer(Modifier.height(12.dp))

        Dock(
            dockApps = dockApps,
            navigationActive = navigation != null,
            onApps = onApps,
            onNavigation = onNavigationButton,
            onPhone = onPhone,
            onClaude = onClaude,
            onLaunch = onLaunchApp,
        )
    }
}
