package com.carlauncherthatsucks.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

/** Perché il meteo non è (ancora) disponibile. */
enum class WeatherStatus { LOADING, NO_LOCATION, NETWORK_ERROR, OK }

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
            parseWeather(body, lat, lon, System.currentTimeMillis())
        } finally {
            conn.disconnect()
        }
    }

    /** Interpreta la risposta JSON di Open-Meteo (`current=temperature_2m,weather_code,is_day`). */
    fun parseWeather(json: String, lat: Double, lon: Double, now: Long): Weather {
        val current = JSONObject(json).getJSONObject("current")
        return Weather(
            temperatureC = current.getDouble("temperature_2m"),
            code = current.optInt("weather_code", -1),
            isDay = current.optInt("is_day", 1) == 1,
            fetchedAt = now,
            latitude = lat,
            longitude = lon,
        )
    }

    /** Cerca una città con il geocoding di Open-Meteo (stesso servizio, senza chiave). */
    suspend fun geocode(name: String): WeatherPlace? = withContext(Dispatchers.IO) {
        val url = URL(
            "https://geocoding-api.open-meteo.com/v1/search?count=1&language=it&format=json&name=" +
                URLEncoder.encode(name.trim(), "UTF-8")
        )
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            if (conn.responseCode !in 200..299) error("HTTP ${conn.responseCode}")
            parseGeocoding(conn.inputStream.bufferedReader().use { it.readText() })
        } finally {
            conn.disconnect()
        }
    }

    fun parseGeocoding(json: String): WeatherPlace? {
        val results = JSONObject(json).optJSONArray("results") ?: return null
        if (results.length() == 0) return null
        val r = results.getJSONObject(0)
        val label = listOf(r.optString("name"), r.optString("admin1"), r.optString("country"))
            .filter { it.isNotBlank() }
            .distinct()
            .joinToString(", ")
        return WeatherPlace(label, r.getDouble("latitude"), r.getDouble("longitude"))
    }
}
