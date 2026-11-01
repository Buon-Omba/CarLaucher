package com.carlauncherthatsucks.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DestinationsTest {
    @Test
    fun `coordinate separate da virgola`() {
        assertEquals(45.4642 to 9.19, parseCoordinates("45.4642, 9.1900"))
    }

    @Test
    fun `coordinate separate da spazio o punto e virgola`() {
        assertEquals(41.9 to 12.5, parseCoordinates("41.9 12.5"))
        assertEquals(41.9 to 12.5, parseCoordinates(" 41.9;12.5 "))
    }

    @Test
    fun `coordinate negative`() {
        assertEquals(-33.86 to -151.2, parseCoordinates("-33.86,-151.2"))
    }

    @Test
    fun `un indirizzo non e una coordinata`() {
        assertNull(parseCoordinates("Via Roma 1, Milano"))
        assertNull(parseCoordinates("Piazza 4 Novembre 12"))
        assertNull(parseCoordinates(""))
    }

    @Test
    fun `coordinate fuori intervallo vengono rifiutate`() {
        assertNull(parseCoordinates("95.0, 10.0"))
        assertNull(parseCoordinates("45.0, 190.0"))
    }
}
