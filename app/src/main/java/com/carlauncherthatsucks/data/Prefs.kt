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

    fun addRecent(destination: String) = update { s ->
        val d = destination.trim().replace(SEP, " ")
        if (d.isEmpty()) s
        else s.copy(recentDestinations = (listOf(d) + s.recentDestinations.filter {
            !it.equals(d, ignoreCase = true)
        }).take(MAX_RECENTS))
    }

    fun toggleDock(appId: String) = update { s ->
        if (appId in s.dockApps) s.copy(dockApps = s.dockApps - appId)
        else s.copy(dockApps = (s.dockApps + appId).take(MAX_DOCK))
    }

    companion object {
        const val MAX_DOCK = 4
        private const val MAX_RECENTS = 6
        private const val SEP = "\u001F"
        private const val KEY_HOME = "home"
        private const val KEY_WORK = "work"
        private const val KEY_SCREEN_ON = "screen_on"
        private const val KEY_DOCK = "dock"
        private const val KEY_RECENTS = "recents"
    }
}
