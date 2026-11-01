package com.carlauncherthatsucks.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Settings(
    val homeAddress: String = "",
    val workAddress: String = "",
    val keepScreenOn: Boolean = true,
    /** App fissate nella barra in basso (id = package/activity). */
    val dockApps: List<String> = emptyList(),
    /** Ultime destinazioni cercate, la più recente per prima. */
    val recentDestinations: List<String> = emptyList(),
)

class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("car_launcher", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    private fun read() = Settings(
        homeAddress = sp.getString(KEY_HOME, "").orEmpty(),
        workAddress = sp.getString(KEY_WORK, "").orEmpty(),
        keepScreenOn = sp.getBoolean(KEY_SCREEN_ON, true),
        dockApps = sp.getString(KEY_DOCK, "").orEmpty().split(SEP).filter { it.isNotBlank() },
        recentDestinations = sp.getString(KEY_RECENTS, "").orEmpty().split(SEP)
            .filter { it.isNotBlank() },
    )

    private fun write(s: Settings) {
        sp.edit()
            .putString(KEY_HOME, s.homeAddress)
            .putString(KEY_WORK, s.workAddress)
            .putBoolean(KEY_SCREEN_ON, s.keepScreenOn)
            .putString(KEY_DOCK, s.dockApps.joinToString(SEP))
            .putString(KEY_RECENTS, s.recentDestinations.joinToString(SEP))
            .apply()
        _settings.value = s
    }

    fun update(transform: (Settings) -> Settings) = write(transform(_settings.value))

    fun addRecent(destination: String) = update { it.withRecentDestination(destination) }

    fun toggleDock(appId: String) = update { it.withDockToggled(appId) }

    companion object {
        const val MAX_DOCK = 4
        const val MAX_RECENTS = 6
        internal const val SEP = "\u001F"
        private const val KEY_HOME = "home"
        private const val KEY_WORK = "work"
        private const val KEY_SCREEN_ON = "screen_on"
        private const val KEY_DOCK = "dock"
        private const val KEY_RECENTS = "recents"
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
