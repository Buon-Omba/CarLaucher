package com.carlauncherthatsucks.data

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SpeedState(
    /** Velocità in km/h, null se non c'è un fix GPS recente. */
    val kmh: Int?,
    val hasGpsFix: Boolean,
    val accuracyMeters: Float?,
)

/**
 * Velocità dal GPS (senza Google Play Services) e ultima posizione nota per il meteo.
 */
class LocationRepository(private val context: Context) {

    private val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    private val _speed = MutableStateFlow(SpeedState(null, false, null))
    val speed: StateFlow<SpeedState> = _speed.asStateFlow()

    private val _location = MutableStateFlow<Location?>(null)
    val location: StateFlow<Location?> = _location.asStateFlow()

    private var lastGpsFix: Location? = null
    private var started = false

    private val gpsListener = object : LocationListener {
        override fun onLocationChanged(location: Location) = onGps(location)
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {
            _speed.value = SpeedState(null, false, null)
        }
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    private val networkListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (_location.value == null || !_speed.value.hasGpsFix) _location.value = location
        }
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    fun hasPermission(): Boolean =
        context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission")
    fun start() {
        if (started || !hasPermission()) return
        started = true
        // Posizione iniziale per il meteo, anche se vecchia.
        val last = lm.allProviders.mapNotNull {
            try {
                lm.getLastKnownLocation(it)
            } catch (_: Exception) {
                null
            }
        }.maxByOrNull { it.time }
        if (last != null && _location.value == null) _location.value = last

        try {
            if (lm.allProviders.contains(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 1000L, 0f, gpsListener, Looper.getMainLooper()
                )
            }
        } catch (_: Exception) {
        }
        try {
            if (lm.allProviders.contains(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 60_000L, 100f, networkListener,
                    Looper.getMainLooper()
                )
            }
        } catch (_: Exception) {
        }
    }

    fun stop() {
        if (!started) return
        started = false
        lm.removeUpdates(gpsListener)
        lm.removeUpdates(networkListener)
    }

    /** Da chiamare periodicamente: se il GPS tace da troppo, la velocità non è più valida. */
    fun checkStale() {
        val fix = lastGpsFix ?: return
        val ageMs = (SystemClock.elapsedRealtimeNanos() - fix.elapsedRealtimeNanos) / 1_000_000
        if (ageMs > STALE_MS && _speed.value.hasGpsFix) {
            _speed.value = SpeedState(null, false, null)
        }
    }

    private fun onGps(loc: Location) {
        val prev = lastGpsFix
        lastGpsFix = loc
        _location.value = loc

        var mps: Float? = if (loc.hasSpeed()) loc.speed else null
        if (mps == null && prev != null) {
            // Alcuni chip non forniscono la velocità: la stimiamo da distanza / tempo.
            val dt = (loc.elapsedRealtimeNanos - prev.elapsedRealtimeNanos) / 1e9
            if (dt in 0.5..10.0) mps = (prev.distanceTo(loc) / dt).toFloat()
        }
        val kmh = mps?.let { mpsToKmh(it) }
        _speed.value = SpeedState(
            kmh = kmh,
            hasGpsFix = true,
            accuracyMeters = if (loc.hasAccuracy()) loc.accuracy else null,
        )
    }

    companion object {
        private const val STALE_MS = 5_000L

        /** m/s → km/h arrotondati; sotto i 2 km/h (rumore del GPS da fermi) mostra 0. */
        fun mpsToKmh(mps: Float): Int {
            val kmh = mps * 3.6f
            return if (kmh < 2f) 0 else Math.round(kmh)
        }
    }
}
