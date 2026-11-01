package com.carlauncherthatsucks.data

import android.app.Notification
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.core.graphics.drawable.toBitmap

/**
 * Legge le notifiche del telefono:
 *  - le notifiche "in corso" di CoMaps diventano le istruzioni di navigazione;
 *  - le notifiche multimediali vengono ignorate (sono gestite dal MediaRepository);
 *  - tutte le altre finiscono nella lista "ultime notifiche".
 *
 * Abilitare il listener serve anche a poter leggere le sessioni multimediali attive.
 */
class CarNotificationListener : NotificationListenerService() {

    private val iconCache = HashMap<String, Bitmap?>()

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationStore.onConnected(this)
        refreshAll()
    }

    override fun onListenerDisconnected() {
        NotificationStore.onDisconnected()
        super.onListenerDisconnected()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        handlePosted(sbn)
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        NotificationStore.remove(sbn.key)
        if (CoMaps.isCoMapsPackage(sbn.packageName) && isOngoing(sbn)) {
            NotificationStore.clearNavigationIf(sbn.packageName)
        }
    }

    private fun refreshAll() {
        val active = try {
            activeNotifications ?: emptyArray()
        } catch (_: Exception) {
            emptyArray()
        }
        var nav: NavigationInfo? = null
        val list = ArrayList<CarNotification>()
        for (sbn in active) {
            when (classify(sbn)) {
                Kind.NAVIGATION -> nav = toNavigation(sbn)
                Kind.NORMAL -> toCarNotification(sbn)?.let { list.add(it) }
                Kind.IGNORE -> Unit
            }
        }
        NotificationStore.replaceAll(list, nav)
    }

    private fun handlePosted(sbn: StatusBarNotification) {
        when (classify(sbn)) {
            Kind.NAVIGATION -> NotificationStore.setNavigation(toNavigation(sbn))
            Kind.NORMAL -> toCarNotification(sbn)?.let { NotificationStore.upsert(it) }
            Kind.IGNORE -> Unit
        }
    }

    private enum class Kind { NAVIGATION, NORMAL, IGNORE }

    private fun isOngoing(sbn: StatusBarNotification): Boolean =
        sbn.isOngoing || (sbn.notification.flags and Notification.FLAG_FOREGROUND_SERVICE) != 0

    private fun classify(sbn: StatusBarNotification): Kind {
        val n = sbn.notification
        if (sbn.packageName == packageName) return Kind.IGNORE
        if (CoMaps.isCoMapsPackage(sbn.packageName) && isOngoing(sbn)) return Kind.NAVIGATION
        // Notifiche multimediali: le gestisce già la scheda "musica".
        if (n.extras?.containsKey(Notification.EXTRA_MEDIA_SESSION) == true) return Kind.IGNORE
        if (n.category == Notification.CATEGORY_TRANSPORT) return Kind.IGNORE
        // Riepiloghi di gruppo: mostriamo le singole notifiche.
        if ((n.flags and Notification.FLAG_GROUP_SUMMARY) != 0) return Kind.IGNORE
        // Notifiche persistenti (servizi in background, ecc.) non interessano alla guida.
        if (isOngoing(sbn)) return Kind.IGNORE
        val title = n.extras?.getCharSequence(Notification.EXTRA_TITLE)
        val text = n.extras?.getCharSequence(Notification.EXTRA_TEXT)
        if (title.isNullOrBlank() && text.isNullOrBlank()) return Kind.IGNORE
        return Kind.NORMAL
    }

    private fun toCarNotification(sbn: StatusBarNotification): CarNotification? {
        val n = sbn.notification
        val extras = n.extras ?: return null
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
            ?: extras.getCharSequence(Notification.EXTRA_TEXT))?.toString().orEmpty()
        return CarNotification(
            key = sbn.key,
            packageName = sbn.packageName,
            appLabel = appLabel(sbn.packageName),
            title = title,
            text = text,
            postTime = sbn.postTime,
            appIcon = appIcon(sbn.packageName),
            largeIcon = largeIcon(n),
            contentIntent = n.contentIntent,
            clearable = sbn.isClearable,
        )
    }

    private fun toNavigation(sbn: StatusBarNotification): NavigationInfo {
        val n = sbn.notification
        val extras = n.extras
        val title = extras?.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (extras?.getCharSequence(Notification.EXTRA_TEXT)
            ?: extras?.getCharSequence(Notification.EXTRA_BIG_TEXT))?.toString().orEmpty()
        val sub = extras?.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()
        val icon = largeIcon(n) ?: smallIcon(sbn)
        return NavigationInfo(
            packageName = sbn.packageName,
            title = title,
            text = text,
            subText = sub,
            turnIcon = icon,
            contentIntent = n.contentIntent,
        )
    }

    private fun largeIcon(n: Notification): Bitmap? = try {
        n.getLargeIcon()?.loadDrawable(this)?.toBitmap(128, 128)
    } catch (_: Exception) {
        null
    }

    private fun smallIcon(sbn: StatusBarNotification): Bitmap? = try {
        val ctx = createPackageContext(sbn.packageName, 0)
        sbn.notification.smallIcon?.loadDrawable(ctx)?.toBitmap(128, 128)
    } catch (_: Exception) {
        null
    }

    private fun appLabel(pkg: String): String = try {
        val pm = packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    } catch (_: PackageManager.NameNotFoundException) {
        pkg
    }

    private fun appIcon(pkg: String): Bitmap? = iconCache.getOrPut(pkg) {
        try {
            packageManager.getApplicationIcon(pkg).toBitmap(96, 96)
        } catch (_: Exception) {
            null
        }
    }
}
