package com.carlauncherthatsucks.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class SettingsTest {
    @Test
    fun `destinazione recente va in cima`() {
        val s = Settings(recentDestinations = listOf("Milano", "Roma"))
            .withRecentDestination("Torino")
        assertEquals(listOf("Torino", "Milano", "Roma"), s.recentDestinations)
    }

    @Test
    fun `nessun duplicato, ignorando le maiuscole`() {
        val s = Settings(recentDestinations = listOf("Milano", "Roma"))
            .withRecentDestination("  roma ")
        assertEquals(listOf("roma", "Milano"), s.recentDestinations)
    }

    @Test
    fun `cronologia limitata`() {
        var s = Settings()
        (1..10).forEach { s = s.withRecentDestination("Luogo $it") }
        assertEquals(Prefs.MAX_RECENTS, s.recentDestinations.size)
        assertEquals("Luogo 10", s.recentDestinations.first())
    }

    @Test
    fun `destinazione vuota ignorata`() {
        val s = Settings(recentDestinations = listOf("Milano"))
        assertSame(s, s.withRecentDestination("   "))
    }

    @Test
    fun `il separatore interno non finisce nei dati salvati`() {
        val s = Settings().withRecentDestination("Via\u001FRoma")
        assertEquals(listOf("Via Roma"), s.recentDestinations)
    }

    @Test
    fun `fissa e rimuovi app dal dock`() {
        val added = Settings().withDockToggled("a/A")
        assertEquals(listOf("a/A"), added.dockApps)
        assertEquals(emptyList<String>(), added.withDockToggled("a/A").dockApps)
    }

    @Test
    fun `dock pieno non accetta altre app`() {
        val full = Settings(dockApps = (1..Prefs.MAX_DOCK).map { "p$it/A" })
        assertSame(full, full.withDockToggled("nuova/A"))
        // ma si può ancora togliere un'app
        assertEquals(Prefs.MAX_DOCK - 1, full.withDockToggled("p1/A").dockApps.size)
    }
}
