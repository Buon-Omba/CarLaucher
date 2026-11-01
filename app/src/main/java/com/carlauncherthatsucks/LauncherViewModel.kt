package com.carlauncherthatsucks

import android.app.Application
import android.location.Location
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.carlauncherthatsucks.data.AppInfo
import com.carlauncherthatsucks.data.AppsRepository
import com.carlauncherthatsucks.data.Contacts
import com.carlauncherthatsucks.data.LocationRepository
import com.carlauncherthatsucks.data.MediaRepository
import com.carlauncherthatsucks.data.PhoneContact
import com.carlauncherthatsucks.data.Prefs
import com.carlauncherthatsucks.data.Weather
import com.carlauncherthatsucks.data.WeatherPlace
import com.carlauncherthatsucks.data.WeatherRepository
import com.carlauncherthatsucks.data.WeatherStatus
import com.carlauncherthatsucks.drive.DriveOverlayService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class LauncherViewModel(app: Application) : AndroidViewModel(app) {

    val prefs = Prefs(app)
    val location = LocationRepository(app)
    val media = MediaRepository(app)

    private val _weather = MutableStateFlow<Weather?>(null)
    val weather: StateFlow<Weather?> = _weather.asStateFlow()

    private val _weatherStatus = MutableStateFlow(WeatherStatus.LOADING)
    val weatherStatus: StateFlow<WeatherStatus> = _weatherStatus.asStateFlow()

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _contacts = MutableStateFlow<List<PhoneContact>>(emptyList())
    val contacts: StateFlow<List<PhoneContact>> = _contacts.asStateFlow()

    private val _notificationAccess = MutableStateFlow(false)
    val notificationAccess: StateFlow<Boolean> = _notificationAccess.asStateFlow()

    private val _locationPermission = MutableStateFlow(false)
    val locationPermission: StateFlow<Boolean> = _locationPermission.asStateFlow()

    private val _contactsPermission = MutableStateFlow(false)
    val contactsPermission: StateFlow<Boolean> = _contactsPermission.asStateFlow()

    private val _overlayPermission = MutableStateFlow(false)
    val overlayPermission: StateFlow<Boolean> = _overlayPermission.asStateFlow()

    private val _callPermission = MutableStateFlow(false)
    val callPermission: StateFlow<Boolean> = _callPermission.asStateFlow()

    private var tickerJob: Job? = null
    private var weatherJob: Job? = null
    private var lastWeatherAttempt = 0L

    fun onStart() {
        refreshPermissions()
        location.start()
        media.start()
        loadApps()
        if (tickerJob?.isActive != true) {
            tickerJob = viewModelScope.launch {
                while (isActive) {
                    location.checkStale()
                    delay(1_000)
                }
            }
        }
        if (weatherJob?.isActive != true) {
            weatherJob = viewModelScope.launch {
                while (isActive) {
                    maybeRefreshWeather()
                    delay(10_000)
                }
            }
        }
    }

    fun onStop() {
        tickerJob?.cancel()
        weatherJob?.cancel()
        location.stop()
        media.stop()
    }

    fun refreshPermissions() {
        val ctx = getApplication<Application>()
        _notificationAccess.value =
            NotificationManagerCompat.getEnabledListenerPackages(ctx).contains(ctx.packageName)
        _locationPermission.value = location.hasPermission()
        _contactsPermission.value = Contacts.canRead(ctx)
        _callPermission.value = Contacts.canCall(ctx)
        _overlayPermission.value = DriveOverlayService.canDrawOverlays(ctx)
    }

    fun onLocationPermissionResult() {
        refreshPermissions()
        location.start()
        refreshWeatherNow()
    }

    fun loadApps() {
        viewModelScope.launch {
            _apps.value = AppsRepository.load(getApplication<Application>())
        }
    }

    fun loadContacts() {
        refreshPermissions()
        viewModelScope.launch {
            _contacts.value = Contacts.loadAll(getApplication<Application>())
        }
    }

    fun refreshWeatherNow() {
        lastWeatherAttempt = 0L
        _weather.value = _weather.value?.copy(fetchedAt = 0L)
        viewModelScope.launch { maybeRefreshWeather() }
    }

    /** Imposta la città di riserva per il meteo; ritorna false se non è stata trovata. */
    suspend fun setWeatherCity(name: String): Boolean {
        if (name.isBlank()) {
            prefs.update { it.copy(weatherPlace = null) }
            return true
        }
        val place = try {
            WeatherRepository.geocode(name)
        } catch (_: Exception) {
            null
        } ?: return false
        prefs.update { it.copy(weatherPlace = place) }
        refreshWeatherNow()
        return true
    }

    private suspend fun maybeRefreshWeather() {
        // Priorità alla posizione reale; altrimenti la città scelta nelle impostazioni.
        val loc: Location? = location.location.value
        val place: WeatherPlace? = prefs.settings.value.weatherPlace
        val lat = loc?.latitude ?: place?.latitude
        val lon = loc?.longitude ?: place?.longitude
        if (lat == null || lon == null) {
            if (_weather.value == null) _weatherStatus.value = WeatherStatus.NO_LOCATION
            return
        }

        val now = System.currentTimeMillis()
        val w = _weather.value
        val moved = w == null || run {
            val out = FloatArray(1)
            Location.distanceBetween(w.latitude, w.longitude, lat, lon, out)
            out[0] > 10_000f
        }
        val old = w == null || now - w.fetchedAt > WEATHER_MAX_AGE_MS
        if (!moved && !old) return
        // In caso di errore (es. niente rete) riproviamo al massimo ogni 30 secondi.
        if (now - lastWeatherAttempt < 30_000L) return
        lastWeatherAttempt = now
        if (w == null) _weatherStatus.value = WeatherStatus.LOADING
        try {
            _weather.value = WeatherRepository.fetch(lat, lon)
            _weatherStatus.value = WeatherStatus.OK
        } catch (_: Exception) {
            if (_weather.value == null) _weatherStatus.value = WeatherStatus.NETWORK_ERROR
        }
    }

    override fun onCleared() {
        onStop()
        super.onCleared()
    }

    companion object {
        private const val WEATHER_MAX_AGE_MS = 15 * 60_000L
    }
}
