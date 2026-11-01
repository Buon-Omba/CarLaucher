package com.carlauncherthatsucks.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/** Contatto preferito mostrato nella griglia del telefono. */
data class FavoriteContact(
    val lookupKey: String,
    val name: String,
    val number: String,
    val photoUri: String?,
) {
    val id: String get() = "$lookupKey|$number"
}

/** Città usata per il meteo quando non c'è una posizione disponibile. */
data class WeatherPlace(
    val label: String,
    val latitude: Double,
    val longitude: Double,
)

data class Settings(
    val keepScreenOn: Boolean = true,
    /** App fissate nella barra in basso (id = package/activity). */
    val dockApps: List<String> = emptyList(),
    /** Ultime destinazioni cercate, la più recente per prima. */
    val recentDestinations: List<String> = emptyList(),
    val favoriteContacts: List<FavoriteContact> = emptyList(),
    val weatherPlace: WeatherPlace? = null,
    val assistant: AssistantProvider = AssistantProvider.CLAUDE,
    val api: ApiConfig = ApiConfig(),
)

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("car_launcher", Context.MODE_PRIVATE)
    /** Chiave API a parte: questo file è escluso dal backup (vedi res/xml, regole di backup). */
    private val secrets = context.getSharedPreferences(SECRETS_FILE, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    private fun read() = Settings(
        keepScreenOn = sp.getBoolean(KEY_SCREEN_ON, true),
        dockApps = sp.getString(KEY_DOCK, "").orEmpty().split(SEP).filter { it.isNotBlank() },
        recentDestinations = sp.getString(KEY_RECENTS, "").orEmpty().split(SEP)
            .filter { it.isNotBlank() },
        favoriteContacts = decodeFavorites(sp.getString(KEY_FAVORITES, null)),
        weatherPlace = decodeWeatherPlace(sp.getString(KEY_WEATHER_PLACE, null)),
        assistant = AssistantProvider.entries.firstOrNull { it.name == sp.getString(KEY_ASSISTANT, null) }
            ?: AssistantProvider.CLAUDE,
        api = ApiConfig(
            baseUrl = sp.getString(KEY_API_URL, "").orEmpty(),
            apiKey = secrets.getString(KEY_API_KEY, "").orEmpty(),
            model = sp.getString(KEY_API_MODEL, "").orEmpty(),
        ),
    )

    private fun write(s: Settings) {
        sp.edit()
            .putBoolean(KEY_SCREEN_ON, s.keepScreenOn)
            .putString(KEY_DOCK, s.dockApps.joinToString(SEP))
            .putString(KEY_RECENTS, s.recentDestinations.joinToString(SEP))
            .putString(KEY_FAVORITES, encodeFavorites(s.favoriteContacts))
            .putString(KEY_WEATHER_PLACE, s.weatherPlace?.let { encodeWeatherPlace(it) })
            .putString(KEY_ASSISTANT, s.assistant.name)
            .putString(KEY_API_URL, s.api.baseUrl)
            .putString(KEY_API_MODEL, s.api.model)
            .apply()
        secrets.edit().putString(KEY_API_KEY, s.api.apiKey).apply()
        _settings.value = s
    }

    fun update(transform: (Settings) -> Settings) = write(transform(_settings.value))

    fun addRecent(destination: String) = update { it.withRecentDestination(destination) }

    fun toggleDock(appId: String) = update { it.withDockToggled(appId) }

    fun toggleFavorite(contact: FavoriteContact) = update { it.withFavoriteToggled(contact) }

    companion object {
        const val MAX_DOCK = 3
        const val MAX_RECENTS = 6
        const val MAX_FAVORITES = 12
        internal const val SEP = "\u001F"
        private const val KEY_SCREEN_ON = "screen_on"
        private const val KEY_DOCK = "dock"
        private const val KEY_RECENTS = "recents"
        private const val KEY_FAVORITES = "favorite_contacts"
        private const val KEY_WEATHER_PLACE = "weather_place"
        private const val KEY_ASSISTANT = "assistant"
        private const val KEY_API_URL = "api_base_url"
        private const val KEY_API_MODEL = "api_model"
        private const val KEY_API_KEY = "api_key"
        const val SECRETS_FILE = "assistant_secrets"
    }
}

/** Aggiunge una destinazione in cima alla cronologia, senza duplicati (ignorando maiuscole). */
fun Settings.withRecentDestination(destination: String): Settings {
    val d = destination.trim().replace(Prefs.SEP, " ")
    if (d.isEmpty()) return this
    val others = recentDestinations.filter { !it.equals(d, ignoreCase = true) }
    return copy(recentDestinations = (listOf(d) + others).take(Prefs.MAX_RECENTS))
}

/** Fissa o rimuove un'app dalla barra in basso; oltre [Prefs.MAX_DOCK] app non ne aggiunge altre. */
fun Settings.withDockToggled(appId: String): Settings = when {
    appId in dockApps -> copy(dockApps = dockApps - appId)
    dockApps.size >= Prefs.MAX_DOCK -> this
    else -> copy(dockApps = dockApps + appId)
}

/** Aggiunge o toglie un contatto dai preferiti (oltre [Prefs.MAX_FAVORITES] non ne aggiunge). */
fun Settings.withFavoriteToggled(contact: FavoriteContact): Settings = when {
    favoriteContacts.any { it.id == contact.id } ->
        copy(favoriteContacts = favoriteContacts.filterNot { it.id == contact.id })
    favoriteContacts.size >= Prefs.MAX_FAVORITES -> this
    else -> copy(favoriteContacts = favoriteContacts + contact)
}

fun encodeFavorites(list: List<FavoriteContact>): String = JSONArray().apply {
    list.forEach { c ->
        put(
            JSONObject()
                .put("lookup", c.lookupKey)
                .put("name", c.name)
                .put("number", c.number)
                .put("photo", c.photoUri ?: "")
        )
    }
}.toString()

fun decodeFavorites(json: String?): List<FavoriteContact> {
    if (json.isNullOrBlank()) return emptyList()
    return try {
        val arr = JSONArray(json)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            FavoriteContact(
                lookupKey = o.optString("lookup"),
                name = o.optString("name"),
                number = o.optString("number"),
                photoUri = o.optString("photo").ifBlank { null },
            )
        }.filter { it.number.isNotBlank() }
    } catch (_: Exception) {
        emptyList()
    }
}

fun encodeWeatherPlace(p: WeatherPlace): String = JSONObject()
    .put("label", p.label)
    .put("lat", p.latitude)
    .put("lon", p.longitude)
    .toString()

fun decodeWeatherPlace(json: String?): WeatherPlace? {
    if (json.isNullOrBlank()) return null
    return try {
        val o = JSONObject(json)
        WeatherPlace(o.getString("label"), o.getDouble("lat"), o.getDouble("lon"))
    } catch (_: Exception) {
        null
    }
}
