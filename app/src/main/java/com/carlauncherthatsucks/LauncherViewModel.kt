package com.carlauncherthatsucks

import android.app.Application
import android.location.Location
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.carlauncherthatsucks.data.AppInfo
import com.carlauncherthatsucks.data.AppsRepository
import com.carlauncherthatsucks.data.LocationRepository
import com.carlauncherthatsucks.data.MediaRepository
import com.carlauncherthatsucks.data.Prefs
import com.carlauncherthatsucks.data.Weather
import com.carlauncherthatsucks.data.WeatherRepository
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

    private val _apps = MutableStateFlow<List<AppInfo>>(emptyList())
    val apps: StateFlow<List<AppInfo>> = _apps.asStateFlow()

    private val _notificationAccess = MutableStateFlow(false)
    val notificationAccess: StateFlow<Boolean> = _notificationAccess.asStateFlow()

    private val _locationPermission = MutableStateFlow(false)
    val locationPermission: StateFlow<Boolean> = _locationPermission.asStateFlow()

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
                    location.location.value?.let { maybeRefreshWeather(it) }
                    delay(15_000)
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
    }

    fun onLocationPermissionResult() {
        refreshPermissions()
        location.start()
    }

    fun loadApps() {
        viewModelScope.launch {
            _apps.value = AppsRepository.load(getApplication<Application>())
        }
    }

    fun refreshWeatherNow() {
        lastWeatherAttempt = 0L
        _weather.value = _weather.value?.copy(fetchedAt = 0L)
        location.location.value?.let { loc -> viewModelScope.launch { maybeRefreshWeather(loc) } }
    }

    private suspend fun maybeRefreshWeather(loc: Location) {
        val now = System.currentTimeMillis()
        val w = _weather.value
        val moved = w == null || run {
            val out = FloatArray(1)
            Location.distanceBetween(w.latitude, w.longitude, loc.latitude, loc.longitude, out)
            out[0] > 10_000f
        }
        val old = w == null || now - w.fetchedAt > WEATHER_MAX_AGE_MS
        if (!moved && !old) return
        // In caso di errore (es. niente rete) riproviamo al massimo ogni minuto.
        if (now - lastWeatherAttempt < 60_000L) return
        lastWeatherAttempt = now
        try {
            _weather.value = WeatherRepository.fetch(loc.latitude, loc.longitude)
        } catch (_: Exception) {
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
