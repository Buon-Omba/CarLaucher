package com.carlauncherthatsucks.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

enum class WeatherKind { CLEAR, PARTLY_CLOUDY, CLOUDY, FOG, DRIZZLE, RAIN, SNOW, THUNDER, UNKNOWN }

data class Weather(
    val temperatureC: Double,
    val code: Int,
    val isDay: Boolean,
    val fetchedAt: Long,
    val latitude: Double,
    val longitude: Double,
) {
    val kind: WeatherKind
        get() = when (code) {
            0, 1 -> WeatherKind.CLEAR
            2 -> WeatherKind.PARTLY_CLOUDY
            3 -> WeatherKind.CLOUDY
            45, 48 -> WeatherKind.FOG
            51, 53, 55, 56, 57 -> WeatherKind.DRIZZLE
            61, 63, 65, 66, 67, 80, 81, 82 -> WeatherKind.RAIN
            71, 73, 75, 77, 85, 86 -> WeatherKind.SNOW
            95, 96, 99 -> WeatherKind.THUNDER
            else -> WeatherKind.UNKNOWN
        }

    val description: String
        get() = when (kind) {
            WeatherKind.CLEAR -> "Sereno"
            WeatherKind.PARTLY_CLOUDY -> "Poco nuvoloso"
            WeatherKind.CLOUDY -> "Nuvoloso"
            WeatherKind.FOG -> "Nebbia"
            WeatherKind.DRIZZLE -> "Pioviggine"
            WeatherKind.RAIN -> "Pioggia"
            WeatherKind.SNOW -> "Neve"
            WeatherKind.THUNDER -> "Temporale"
            WeatherKind.UNKNOWN -> "—"
        }
}

/**
 * Meteo attuale da Open-Meteo: gratuito, senza chiave API e senza account.
 */
object WeatherRepository {
    suspend fun fetch(lat: Double, lon: Double): Weather = withContext(Dispatchers.IO) {
        val url = URL(
            String.format(
                Locale.US,
                "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f" +
                    "&current=temperature_2m,weather_code,is_day&timezone=auto",
                lat, lon
            )
        )
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            conn.setRequestProperty("User-Agent", "CarLauncherThatSucks/1.0")
            if (conn.responseCode !in 200..299) error("HTTP ${conn.responseCode}")
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val current = JSONObject(body).getJSONObject("current")
            Weather(
                temperatureC = current.getDouble("temperature_2m"),
                code = current.optInt("weather_code", -1),
                isDay = current.optInt("is_day", 1) == 1,
                fetchedAt = System.currentTimeMillis(),
                latitude = lat,
                longitude = lon,
            )
        } finally {
            conn.disconnect()
        }
    }
}
