package com.carlauncherthatsucks.screenshots

import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityOptionsCompat
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import com.carlauncherthatsucks.data.WeatherStatus
import com.carlauncherthatsucks.drive.DrivePanel
import com.carlauncherthatsucks.ui.CarTheme
import com.carlauncherthatsucks.ui.HomeScreen
import com.carlauncherthatsucks.ui.PhoneScreen
import com.carlauncherthatsucks.ui.SettingsScreen
import com.carlauncherthatsucks.data.Prefs
import com.carlauncherthatsucks.data.ApiConfig
import com.carlauncherthatsucks.data.AssistantProvider
import com.carlauncherthatsucks.data.VoicePhase
import com.carlauncherthatsucks.data.VoiceUiState
import com.carlauncherthatsucks.ui.VoiceChatScreen
import com.carlauncherthatsucks.data.Settings
import com.carlauncherthatsucks.data.WeatherPlace
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

/**
 * Screenshot delle schermate principali (./gradlew recordPaparazziDebug).
 * Servono per controllare l'interfaccia senza telefono.
 */
class Shots {
    @get:Rule val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5, maxPercentDifference = 1.0)

    @Composable
    private fun Themed(dark: Boolean = true, content: @Composable () -> Unit) =
        CarTheme(dark = dark, dynamicColor = false) {
            Surface(color = MaterialTheme.colorScheme.surface) { content() }
        }

    @Composable
    private fun Home(navigating: Boolean = false, permissions: Boolean = true) = HomeScreen(
        speed = Fixtures.speed,
        weather = Fixtures.weather,
        weatherStatus = WeatherStatus.OK,
        navigation = if (navigating) Fixtures.navigation else null,
        media = Fixtures.media,
        notifications = Fixtures.notifications,
        dockApps = emptyList(),
        locationPermission = permissions,
        notificationAccess = permissions,
        onRequestLocation = {}, onNotificationAccess = {}, onRefreshWeather = {},
        onOpenNavigation = {}, onPlayPause = {}, onNext = {}, onPrevious = {}, onOpenPlayer = {},
        onOpenNotification = {}, onDismissNotification = {}, onDismissAll = {},
        onApps = {}, onNavigationButton = {}, onPhone = {}, assistant = AssistantProvider.CLAUDE, onAssistant = {}, onLaunchApp = {},
    )

    @Test fun home_dark() = paparazzi.snapshot { Themed { Home() } }

    @Test fun home_light() = paparazzi.snapshot { Themed(dark = false) { Home() } }

    @Test fun home_navigating() = paparazzi.snapshot { Themed { Home(navigating = true) } }

    @Test fun home_no_permissions() = paparazzi.snapshot { Themed { Home(permissions = false) } }

    @Test fun drive_panel() = paparazzi.snapshot {
        Themed {
            Box(Modifier.fillMaxSize().background(Color(0xFFB9C7B4)), contentAlignment = Alignment.BottomCenter) {
            DrivePanel(
                speedFlow = MutableStateFlow(Fixtures.speed),
                mediaFlow = MutableStateFlow(Fixtures.media),
                onPlayPause = {}, onNext = {}, onPrevious = {},
                onDrag = {}, onDragEnd = {}, onHome = {},
            )
            }
        }
    }

    @Test fun settings() = paparazzi.snapshot {
        Themed {
            val ctx = androidx.compose.ui.platform.LocalContext.current
            SettingsScreen(
                settings = Settings(
                    weatherPlace = WeatherPlace("Milano, Lombardia, Italia", 45.46, 9.19),
                    assistant = AssistantProvider.OPENAI_COMPATIBLE,
                    api = ApiConfig("https://openrouter.ai/api/v1", "sk-or-esempio", "openrouter/auto"),
                ),
                prefs = Prefs(ctx),
                notificationAccess = true, locationPermission = true,
                contactsPermission = false, callPermission = false,
                onRequestLocation = {}, onRequestPhone = {}, onSetWeatherCity = { true },
                onTestApi = { Result.success("ok") },
                onNotificationAccess = {}, overlayPermission = false, onOverlayPermission = {},
                onDefaultLauncher = {}, onAppInfo = {}, onClose = {},
            )
        }
    }

    @Test fun voice_chat() = paparazzi.snapshot {
        Themed {
            VoiceChatScreen(
                state = VoiceUiState(
                    phase = VoicePhase.SPEAKING,
                    lastQuestion = "C'è traffico in tangenziale a quest'ora?",
                    lastAnswer = "Non vedo il traffico in tempo reale, ma il venerdì sera la tangenziale è spesso " +
                        "rallentata verso le uscite principali. CoMaps ti mostra il percorso più rapido.",
                ),
                config = ApiConfig("https://openrouter.ai/api/v1", "k", "openrouter/auto"),
                onMic = {}, onNewConversation = {}, onOpenSettings = {}, onClose = {},
            )
        }
    }

    @Test fun phone() = paparazzi.snapshot {
        Themed {
            CompositionLocalProvider(LocalActivityResultRegistryOwner provides FakeRegistryOwner) {
            PhoneScreen(
                favorites = Fixtures.favorites, contacts = emptyList(),
                contactsPermission = true, callPermission = true,
                onLoadContacts = {}, onPermissionsChanged = {}, onToggleFavorite = {}, onClose = {},
            )
            }
        }
    }
}

/** Registro finto: negli screenshot le richieste di permesso non fanno nulla. */
private object FakeRegistryOwner : ActivityResultRegistryOwner {
    override val activityResultRegistry = object : ActivityResultRegistry() {
        override fun <I, O> onLaunch(
            requestCode: Int,
            contract: ActivityResultContract<I, O>,
            input: I,
            options: ActivityOptionsCompat?,
        ) = Unit
    }
}
