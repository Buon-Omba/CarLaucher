package com.carlauncherthatsucks.screenshots

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import com.carlauncherthatsucks.data.CarNotification
import com.carlauncherthatsucks.data.FavoriteContact
import com.carlauncherthatsucks.data.MediaState
import com.carlauncherthatsucks.data.NavigationInfo
import com.carlauncherthatsucks.data.SpeedState
import com.carlauncherthatsucks.data.Weather

/** Dati di esempio per gli screenshot. */
object Fixtures {
    private const val NOW = 1_762_000_000_000L

    fun art(c1: Int = 0xFFE07A5F.toInt(), c2: Int = 0xFF3D405B.toInt()): Bitmap {
        val b = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        val p = Paint().apply { shader = LinearGradient(0f, 0f, 200f, 200f, c1, c2, Shader.TileMode.CLAMP) }
        Canvas(b).drawRect(0f, 0f, 200f, 200f, p)
        return b
    }

    val speed = SpeedState(kmh = 87, hasGpsFix = true, accuracyMeters = 4f)
    val weather = Weather(18.4, 2, true, NOW, 45.46, 9.19)

    val media = MediaState(
        packageName = "org.videolan.vlc", appLabel = "VLC",
        title = "Autostrada del Sole", artist = "I Viaggiatori", album = "Km 0",
        art = art(), isPlaying = true, durationMs = 214_000, positionMs = 81_000,
        positionUpdatedAt = Long.MAX_VALUE / 2, playbackSpeed = 1f,
        canSkipNext = true, canSkipPrevious = true,
    )

    val navigation = NavigationInfo(
        packageName = "app.comaps.fdroid", title = "300 m", text = "Svolta a destra in Via Emilia",
        subText = "Arrivo 18:42 · 23 km", turnIcon = null, contentIntent = null,
    )

    val notifications = listOf(
        CarNotification("1", "org.telegram", "Telegram", "Giulia", "Sono già al casello, ti aspetto lì 👋",
            NOW - 60_000, null, art(0xFF81B29A.toInt(), 0xFF3D405B.toInt()), null, true),
        CarNotification("2", "com.whatsapp", "WhatsApp", "Calcetto giovedì", "Marco: chi porta il pallone?",
            NOW - 600_000, null, null, null, true),
        CarNotification("3", "com.google.android.gm", "Email", "Conferma prenotazione",
            "La tua prenotazione per il parcheggio è confermata per sabato.", NOW - 3_600_000, null, null, null, true),
    )

    val favorites = listOf(
        FavoriteContact("a", "Giulia Bianchi", "1", null),
        FavoriteContact("b", "Mamma", "2", null),
        FavoriteContact("c", "Marco Rossi", "3", null),
        FavoriteContact("d", "Officina Ferri", "4", null),
        FavoriteContact("e", "Luca", "5", null),
    )
}
