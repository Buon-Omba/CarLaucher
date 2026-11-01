package com.carlauncherthatsucks.data

import android.app.ActivityOptions
import android.app.PendingIntent
import android.content.Context
import android.os.Build

/**
 * Invia un PendingIntent di un'altra app (notifica, sessione multimediale) consentendo
 * l'avvio dell'activity: da Android 14 il mittente deve dichiararlo esplicitamente.
 */
fun PendingIntent.sendFromForeground(context: Context): Boolean = try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        val options = ActivityOptions.makeBasic()
        options.setPendingIntentBackgroundActivityStartMode(
            ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED
        )
        send(context, 0, null, null, null, null, options.toBundle())
    } else {
        send()
    }
    true
} catch (_: Exception) {
    false
}
