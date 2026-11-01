package com.carlauncherthatsucks.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

class PhoneAndClaudeTest {
    private fun fav(n: Int) = FavoriteContact("key$n", "Contatto $n", "+39 333 000 00$n", null)

    @Test
    fun `aggiungi e togli un preferito`() {
        val s = Settings().withFavoriteToggled(fav(1)).withFavoriteToggled(fav(2))
        assertEquals(listOf("Contatto 1", "Contatto 2"), s.favoriteContacts.map { it.name })
        assertEquals(listOf("Contatto 2"), s.withFavoriteToggled(fav(1)).favoriteContacts.map { it.name })
    }

    @Test
    fun `stesso contatto con numeri diversi sono preferiti distinti`() {
        val casa = FavoriteContact("mamma", "Mamma", "0212345", null)
        val cell = casa.copy(number = "3331234")
        val s = Settings().withFavoriteToggled(casa).withFavoriteToggled(cell)
        assertEquals(2, s.favoriteContacts.size)
    }

    @Test
    fun `preferiti limitati`() {
        val full = Settings(favoriteContacts = (1..Prefs.MAX_FAVORITES).map { fav(it) })
        assertSame(full, full.withFavoriteToggled(fav(99)))
    }

    @Test
    fun `preferiti salvati e riletti`() {
        val list = listOf(
            FavoriteContact("k1", "Anna \"Nanà\" Rossi", "+39 333 1", "content://com.android.contacts/1/photo"),
            FavoriteContact("k2", "Bruno", "0212345", null),
        )
        assertEquals(list, decodeFavorites(encodeFavorites(list)))
    }

    @Test
    fun `preferiti corrotti o assenti`() {
        assertEquals(emptyList<FavoriteContact>(), decodeFavorites(null))
        assertEquals(emptyList<FavoriteContact>(), decodeFavorites("non è json"))
    }

    @Test
    fun `citta del meteo salvata e riletta`() {
        val p = WeatherPlace("Milano, Lombardia, Italia", 45.46427, 9.18951)
        assertEquals(p, decodeWeatherPlace(encodeWeatherPlace(p)))
        assertNull(decodeWeatherPlace(""))
    }

    @Test
    fun `iniziali per avatar senza foto`() {
        assertEquals("MR", initialsOf("Mario Rossi"))
        assertEquals("AB", initialsOf("anna maria bianchi"))
        assertEquals("L", initialsOf("Luca"))
        assertEquals("?", initialsOf("  "))
    }

    @Test
    fun `normalizzazione numeri`() {
        assertEquals("+393331234567", normalizeNumber("+39 333-123 (45) 67"))
    }

    @Test
    fun `scorciatoia vocale di Claude`() {
        val shortcuts = listOf(
            ShortcutCandidate("new_chat", "Nuova chat", null),
            ShortcutCandidate("camera", "Fotocamera", null),
            ShortcutCandidate("sc_3", "Modalità vocale", null),
        )
        assertEquals("sc_3", pickVoiceShortcut(shortcuts)?.id)
        assertEquals("voice_mode", pickVoiceShortcut(listOf(ShortcutCandidate("voice_mode", null, null)))?.id)
        assertNull(pickVoiceShortcut(shortcuts.take(2)))
    }

    @Test
    fun `geocoding di Open-Meteo`() {
        val json = """{"results":[{"name":"Milano","latitude":45.46427,"longitude":9.18951,
            "country":"Italia","admin1":"Lombardia"}],"generationtime_ms":0.2}"""
        val p = WeatherRepository.parseGeocoding(json)!!
        assertEquals("Milano, Lombardia, Italia", p.label)
        assertEquals(45.46427, p.latitude, 0.0)
        assertNull(WeatherRepository.parseGeocoding("""{"generationtime_ms":0.1}"""))
    }
}
