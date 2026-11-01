package com.carlauncherthatsucks.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherTest {
    private fun weather(code: Int, isDay: Boolean = true) =
        Weather(temperatureC = 20.0, code = code, isDay = isDay, fetchedAt = 0, latitude = 0.0, longitude = 0.0)

    @Test
    fun `codici WMO mappati sul tipo di meteo`() {
        val expected = mapOf(
            0 to WeatherKind.CLEAR, 1 to WeatherKind.CLEAR,
            2 to WeatherKind.PARTLY_CLOUDY, 3 to WeatherKind.CLOUDY,
            45 to WeatherKind.FOG, 48 to WeatherKind.FOG,
            51 to WeatherKind.DRIZZLE, 57 to WeatherKind.DRIZZLE,
            61 to WeatherKind.RAIN, 65 to WeatherKind.RAIN, 82 to WeatherKind.RAIN,
            71 to WeatherKind.SNOW, 86 to WeatherKind.SNOW,
            95 to WeatherKind.THUNDER, 99 to WeatherKind.THUNDER,
            -1 to WeatherKind.UNKNOWN, 42 to WeatherKind.UNKNOWN,
        )
        expected.forEach { (code, kind) -> assertEquals("codice $code", kind, weather(code).kind) }
    }

    @Test
    fun `ogni tipo di meteo ha una descrizione in italiano`() {
        assertEquals("Sereno", weather(0).description)
        assertEquals("Pioggia", weather(63).description)
        assertEquals("Temporale", weather(95).description)
        WeatherKind.entries.forEach { kind ->
            val code = (-1..100).first { weather(it).kind == kind }
            assertTrue(weather(code).description.isNotBlank())
        }
    }

    @Test
    fun `risposta di Open-Meteo`() {
        val json = """
            {"latitude":45.46,"longitude":9.19,
             "current_units":{"temperature_2m":"°C"},
             "current":{"time":"2026-11-01T18:00","interval":900,
                        "temperature_2m":12.4,"weather_code":3,"is_day":0}}
        """.trimIndent()
        val w = WeatherRepository.parseWeather(json, 45.46, 9.19, now = 1234L)
        assertEquals(12.4, w.temperatureC, 0.001)
        assertEquals(WeatherKind.CLOUDY, w.kind)
        assertFalse(w.isDay)
        assertEquals(1234L, w.fetchedAt)
        assertEquals(45.46, w.latitude, 0.0)
    }

    @Test
    fun `campi opzionali mancanti`() {
        val w = WeatherRepository.parseWeather("""{"current":{"temperature_2m":-3}}""", 0.0, 0.0, 0)
        assertEquals(-3.0, w.temperatureC, 0.0)
        assertEquals(WeatherKind.UNKNOWN, w.kind)
        assertTrue(w.isDay)
    }

    @Test(expected = Exception::class)
    fun `risposta senza dati attuali`() {
        WeatherRepository.parseWeather("""{"error":true,"reason":"bad"}""", 0.0, 0.0, 0)
    }
}
