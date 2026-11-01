package com.carlauncherthatsucks.data

import android.app.PendingIntent
import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.lang.ref.WeakReference

/** Notifica "normale" mostrata nella lista delle ultime notifiche. */
data class CarNotification(
    val key: String,
    val packageName: String,
    val appLabel: String,
    val title: String,
    val text: String,
    val postTime: Long,
    val appIcon: Bitmap?,
    val largeIcon: Bitmap?,
    val contentIntent: PendingIntent?,
    val clearable: Boolean,
)

/** Istruzioni di navigazione lette dalla notifica in corso di CoMaps. */
data class NavigationInfo(
    val packageName: String,
    val title: String,
    val text: String,
    val subText: String,
    val turnIcon: Bitmap?,
    val contentIntent: PendingIntent?,
)

/**
 * Stato condiviso tra il NotificationListenerService (che lo alimenta) e la UI (che lo osserva).
 */
object NotificationStore {
    private const val MAX_NOTIFICATIONS = 30

    private val _notifications = MutableStateFlow<List<CarNotification>>(emptyList())
    val notifications: StateFlow<List<CarNotification>> = _notifications.asStateFlow()

    private val _navigation = MutableStateFlow<NavigationInfo?>(null)
    val navigation: StateFlow<NavigationInfo?> = _navigation.asStateFlow()

    private val _listenerConnected = MutableStateFlow(false)
    val listenerConnected: StateFlow<Boolean> = _listenerConnected.asStateFlow()

    private var listenerRef: WeakReference<CarNotificationListener>? = null

    internal fun onConnected(listener: CarNotificationListener) {
        listenerRef = WeakReference(listener)
        _listenerConnected.value = true
    }

    internal fun onDisconnected() {
        listenerRef = null
        _listenerConnected.value = false
        _navigation.value = null
    }

    internal fun replaceAll(items: List<CarNotification>, nav: NavigationInfo?) {
        _notifications.value = items.sortedByDescending { it.postTime }.take(MAX_NOTIFICATIONS)
        _navigation.value = nav
    }

    internal fun upsert(item: CarNotification) {
        val list = _notifications.value.filterNot { it.key == item.key } + item
        _notifications.value = list.sortedByDescending { it.postTime }.take(MAX_NOTIFICATIONS)
    }

    internal fun remove(key: String) {
        _notifications.value = _notifications.value.filterNot { it.key == key }
    }

    internal fun setNavigation(info: NavigationInfo?) {
        _navigation.value = info
    }

    internal fun clearNavigationIf(packageName: String) {
        if (_navigation.value?.packageName == packageName) _navigation.value = null
    }

    /** Elimina la notifica sia dalla lista sia dal sistema (se possibile). */
    fun dismiss(key: String) {
        remove(key)
        try {
            listenerRef?.get()?.cancelNotification(key)
        } catch (_: Exception) {
        }
    }

    fun dismissAll() {
        val keys = _notifications.value.filter { it.clearable }.map { it.key }
        keys.forEach { dismiss(it) }
    }
}
