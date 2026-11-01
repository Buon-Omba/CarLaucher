package com.carlauncherthatsucks.data

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeedAndMediaTest {
    @Test
    fun `conversione m al secondo in km orari`() {
        assertEquals(36, LocationRepository.mpsToKmh(10f))
        assertEquals(130, LocationRepository.mpsToKmh(36.11f))
        assertEquals(50, LocationRepository.mpsToKmh(13.9f))
    }

    @Test
    fun `da fermi il rumore del GPS diventa zero`() {
        assertEquals(0, LocationRepository.mpsToKmh(0f))
        assertEquals(0, LocationRepository.mpsToKmh(0.5f)) // 1.8 km/h
        assertEquals(2, LocationRepository.mpsToKmh(0.56f)) // 2.016 km/h
    }

    private fun media(playing: Boolean, pos: Long = 10_000, duration: Long = 180_000, speed: Float = 1f) =
        MediaState(
            packageName = "p", appLabel = "App", title = "t", artist = "a", album = "",
            art = null, isPlaying = playing, durationMs = duration, positionMs = pos,
            positionUpdatedAt = 1_000L, playbackSpeed = speed,
            canSkipNext = true, canSkipPrevious = true,
        )

    @Test
    fun `in pausa la posizione resta ferma`() {
        assertEquals(10_000, media(playing = false).currentPosition(now = 60_000))
    }

    @Test
    fun `in riproduzione la posizione avanza col tempo`() {
        assertEquals(15_000, media(playing = true).currentPosition(now = 6_000))
        assertEquals(20_000, media(playing = true, speed = 2f).currentPosition(now = 6_000))
    }

    @Test
    fun `la posizione non supera la durata`() {
        assertEquals(180_000, media(playing = true).currentPosition(now = 10_000_000))
    }

    @Test
    fun `stream senza durata avanza senza limite`() {
        assertEquals(1_009_000, media(playing = true, duration = 0).currentPosition(now = 1_000_000))
    }
}
