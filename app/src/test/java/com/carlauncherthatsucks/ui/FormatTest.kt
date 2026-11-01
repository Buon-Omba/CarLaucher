package com.carlauncherthatsucks.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test
    fun `durate brevi in minuti e secondi`() {
        assertEquals("0:00", formatDuration(0))
        assertEquals("0:59", formatDuration(59_999))
        assertEquals("3:05", formatDuration(185_000))
    }

    @Test
    fun `durate lunghe con le ore`() {
        assertEquals("1:00:00", formatDuration(3_600_000))
        assertEquals("2:03:04", formatDuration(7_384_000))
    }
}
