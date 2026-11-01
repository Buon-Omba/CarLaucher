package com.carlauncherthatsucks.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Un numero di telefono della rubrica (un contatto può averne più di uno). */
data class PhoneContact(
    val lookupKey: String,
    val name: String,
    val number: String,
    val typeLabel: String,
    val photoUri: String?,
) {
    fun toFavorite() = FavoriteContact(lookupKey, name, number, photoUri)
    val id: String get() = "$lookupKey|$number"
}

object Contacts {
    private val photoCache = HashMap<String, Bitmap?>()

    fun canRead(context: Context) =
        context.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    fun canCall(context: Context) =
        context.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

    /** Tutti i numeri della rubrica, in ordine alfabetico. */
    suspend fun loadAll(context: Context): List<PhoneContact> = withContext(Dispatchers.IO) {
        if (!canRead(context)) return@withContext emptyList()
        val projection = arrayOf(
            Phone.LOOKUP_KEY,
            Phone.DISPLAY_NAME_PRIMARY,
            Phone.NUMBER,
            Phone.TYPE,
            Phone.LABEL,
            Phone.PHOTO_URI,
        )
        val result = ArrayList<PhoneContact>()
        try {
            context.contentResolver.query(
                Phone.CONTENT_URI, projection, null, null,
                "${Phone.DISPLAY_NAME_PRIMARY} COLLATE LOCALIZED ASC",
            )?.use { c ->
                while (c.moveToNext()) {
                    val number = c.getString(2) ?: continue
                    val type = c.getInt(3)
                    val label = c.getString(4)
                    result.add(
                        PhoneContact(
                            lookupKey = c.getString(0).orEmpty(),
                            name = c.getString(1) ?: number,
                            number = number,
                            typeLabel = Phone.getTypeLabel(context.resources, type, label).toString(),
                            photoUri = c.getString(5),
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }
        // Lo stesso numero può comparire più volte (es. account diversi): teniamone uno.
        result.distinctBy { it.lookupKey + "|" + normalizeNumber(it.number) }
    }

    suspend fun loadPhoto(context: Context, uri: String?): Bitmap? {
        if (uri.isNullOrBlank()) return null
        synchronized(photoCache) { if (photoCache.containsKey(uri)) return photoCache[uri] }
        val bmp = withContext(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(Uri.parse(uri))?.use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (_: Exception) {
                null
            }
        }
        synchronized(photoCache) { photoCache[uri] = bmp }
        return bmp
    }

    /** Chiama direttamente se c'è il permesso, altrimenti apre il compositore con il numero. */
    fun call(context: Context, number: String) {
        val uri = Uri.fromParts("tel", number, null)
        val intent = if (canCall(context)) Intent(Intent.ACTION_CALL, uri) else Intent(Intent.ACTION_DIAL, uri)
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            Toast.makeText(context, "Impossibile avviare la chiamata", Toast.LENGTH_SHORT).show()
        }
    }
}

/** Toglie spazi, trattini e parentesi per confrontare i numeri. */
fun normalizeNumber(number: String): String = number.filter { it.isDigit() || it == '+' }

/** Iniziali per l'avatar quando il contatto non ha foto. */
fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() && it[0].isLetterOrDigit() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(1).uppercase()
        else -> (parts[0].take(1) + parts.last().take(1)).uppercase()
    }
}
