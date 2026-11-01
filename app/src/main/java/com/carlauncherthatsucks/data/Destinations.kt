package com.carlauncherthatsucks.data

/** Accetta indirizzi scritti come "45.4642, 9.1900" e li tratta come coordinate. */
fun parseCoordinates(text: String): Pair<Double, Double>? {
    val m = Regex("""^\s*(-?\d{1,2}(?:\.\d+)?)\s*[,;\s]\s*(-?\d{1,3}(?:\.\d+)?)\s*$""")
        .find(text) ?: return null
    val lat = m.groupValues[1].toDoubleOrNull() ?: return null
    val lon = m.groupValues[2].toDoubleOrNull() ?: return null
    if (lat !in -90.0..90.0 || lon !in -180.0..180.0) return null
    return lat to lon
}
