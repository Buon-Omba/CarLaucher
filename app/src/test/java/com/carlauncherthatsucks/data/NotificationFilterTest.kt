package com.carlauncherthatsucks.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NotificationFilterTest {
    // Valori di android.app.NotificationManager.IMPORTANCE_* (costanti: copiate a compile time).
    private val unspecified = -1000
    private val none = 0
    private val min = 1
    private val low = 2
    private val default = 3
    private val high = 4

    @Test
    fun `notifiche silenziose ignorate`() {
        assertTrue(isSilentImportance(none))
        assertTrue(isSilentImportance(min))
        assertTrue(isSilentImportance(low))
    }

    @Test
    fun `notifiche con suono mostrate`() {
        assertFalse(isSilentImportance(default))
        assertFalse(isSilentImportance(high))
        assertFalse(isSilentImportance(unspecified))
    }
}
