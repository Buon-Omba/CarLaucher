package com.carlauncherthatsucks.drive

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.carlauncherthatsucks.MainActivity
import com.carlauncherthatsucks.R
import com.carlauncherthatsucks.data.LocationRepository
import com.carlauncherthatsucks.data.MediaRepository
import com.carlauncherthatsucks.data.NotificationStore
import com.carlauncherthatsucks.ui.CarTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Modalità guida: CoMaps resta a schermo intero (mappa, percorso, indicazioni) e sopra
 * mostriamo un pannello trascinabile con velocità e controlli multimediali.
 */
class DriveOverlayService : Service() {

    private lateinit var wm: WindowManager
    private lateinit var location: LocationRepository
    private lateinit var media: MediaRepository
    private val owner = OverlayOwner()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var view: ComposeView? = null
    private lateinit var params: WindowManager.LayoutParams

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (!startAsForeground()) {
            // Senza permesso di posizione Android non consente il servizio in primo piano.
            stopSelf()
            return
        }
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
        location = LocationRepository(this).also { it.start() }
        media = MediaRepository(this).also { it.start() }
        owner.create()
        addOverlay()

        scope.launch {
            while (isActive) {
                location.checkStale()
                delay(1_000)
            }
        }
        // Quando la navigazione di CoMaps finisce, il pannello si chiude da solo.
        scope.launch {
            var sawNavigation = false
            NotificationStore.navigation.collect { nav ->
                if (nav != null) sawNavigation = true
                else if (sawNavigation) stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) stopSelf()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        view?.let {
            try {
                wm.removeView(it)
            } catch (_: Exception) {
            }
        }
        view = null
        if (::location.isInitialized) {
            owner.destroy()
            location.stop()
            media.stop()
        }
        super.onDestroy()
    }

    private fun addOverlay() {
        val prefs = getSharedPreferences(PREFS, MODE_PRIVATE)
        params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT,
        ).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = prefs.getInt(KEY_Y, defaultOffsetPx())
        }

        val compose = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                CarTheme {
                    DrivePanel(
                        speedFlow = location.speed,
                        mediaFlow = media.state,
                        onPlayPause = media::playPause,
                        onNext = media::next,
                        onPrevious = media::previous,
                        onDrag = { dy -> moveBy(dy) },
                        onDragEnd = { prefs.edit().putInt(KEY_Y, params.y).apply() },
                        onHome = { goHome() },
                    )
                }
            }
        }
        wm.addView(compose, params)
        view = compose
    }

    /** Sposta il pannello in verticale (dy positivo = verso il basso). */
    private fun moveBy(dy: Float) {
        val v = view ?: return
        val maxY = resources.displayMetrics.heightPixels - v.height - defaultOffsetPx()
        params.y = (params.y - dy.toInt()).coerceIn(0, maxY.coerceAtLeast(0))
        try {
            wm.updateViewLayout(v, params)
        } catch (_: Exception) {
        }
    }

    private fun defaultOffsetPx(): Int = (96 * resources.displayMetrics.density).toInt()

    private fun goHome() {
        // Consentito anche da un servizio: l'app ha il permesso "mostra sopra altre app".
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
        stopSelf()
    }

    private fun startAsForeground(): Boolean {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL, "Modalità guida", NotificationManager.IMPORTANCE_LOW)
        )
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val stop = PendingIntent.getService(
            this, 1, Intent(this, DriveOverlayService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Modalità guida")
            .setContentText("Pannello velocità e musica sopra CoMaps")
            .setOngoing(true)
            .setContentIntent(open)
            .addAction(0, "Chiudi pannello", stop)
            .build()
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        } else {
            0
        }
        return try {
            ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
            true
        } catch (_: Exception) {
            false
        }
    }

    companion object {
        private const val CHANNEL = "drive_mode"
        private const val NOTIFICATION_ID = 42
        private const val ACTION_STOP = "com.carlauncherthatsucks.STOP_DRIVE"
        private const val PREFS = "drive_overlay"
        private const val KEY_Y = "y"

        fun canDrawOverlays(context: Context): Boolean = Settings.canDrawOverlays(context)

        fun start(context: Context) {
            if (!canDrawOverlays(context)) return
            try {
                context.startForegroundService(Intent(context, DriveOverlayService::class.java))
            } catch (_: Exception) {
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, DriveOverlayService::class.java))
        }
    }
}

/** Lifecycle "finto" per ospitare Compose in una finestra creata da un Service. */
private class OverlayOwner : LifecycleOwner, SavedStateRegistryOwner, ViewModelStoreOwner {
    private val registry = LifecycleRegistry(this)
    private val savedState = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = registry
    override val savedStateRegistry: SavedStateRegistry get() = savedState.savedStateRegistry
    override val viewModelStore: ViewModelStore get() = store

    fun create() {
        savedState.performRestore(null)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun destroy() {
        registry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}
