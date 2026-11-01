package com.carlauncherthatsucks.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MediaState(
    val packageName: String,
    val appLabel: String,
    val title: String,
    val artist: String,
    val album: String,
    val art: Bitmap?,
    val isPlaying: Boolean,
    val durationMs: Long,
    val positionMs: Long,
    /** Momento (elapsedRealtime) a cui si riferisce [positionMs]. */
    val positionUpdatedAt: Long,
    val playbackSpeed: Float,
    val canSkipNext: Boolean,
    val canSkipPrevious: Boolean,
) {
    fun currentPosition(now: Long = SystemClock.elapsedRealtime()): Long {
        if (!isPlaying) return positionMs
        val pos = positionMs + ((now - positionUpdatedAt) * playbackSpeed).toLong()
        return if (durationMs > 0) pos.coerceIn(0, durationMs) else pos.coerceAtLeast(0)
    }
}

/**
 * Segue la sessione multimediale attiva (Spotify, VLC, YouTube Music, radio...) e la controlla.
 * Richiede che l'accesso alle notifiche sia concesso al nostro [CarNotificationListener].
 */
class MediaRepository(private val context: Context) {

    private val sessionManager =
        context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
    private val listenerComponent = ComponentName(context, CarNotificationListener::class.java)
    private val handler = Handler(Looper.getMainLooper())

    private val _state = MutableStateFlow<MediaState?>(null)
    val state: StateFlow<MediaState?> = _state.asStateFlow()

    private var controllers: List<MediaController> = emptyList()
    private var current: MediaController? = null
    private var started = false

    private val sessionsListener =
        MediaSessionManager.OnActiveSessionsChangedListener { list -> onSessions(list.orEmpty()) }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) = rebuild()
        override fun onPlaybackStateChanged(state: PlaybackState?) {
            // Se un'altra sessione inizia a suonare, passiamo a quella.
            onSessions(controllers)
        }
        override fun onSessionDestroyed() = onSessions(controllers.filter { it != current })
    }

    /** Ritorna false se manca il permesso (accesso alle notifiche). */
    fun start(): Boolean {
        if (started) return true
        return try {
            sessionManager.addOnActiveSessionsChangedListener(sessionsListener, listenerComponent, handler)
            onSessions(sessionManager.getActiveSessions(listenerComponent))
            started = true
            true
        } catch (_: SecurityException) {
            _state.value = null
            false
        }
    }

    fun stop() {
        if (!started) return
        started = false
        try {
            sessionManager.removeOnActiveSessionsChangedListener(sessionsListener)
        } catch (_: Exception) {
        }
        current?.unregisterCallback(controllerCallback)
        current = null
        controllers = emptyList()
    }

    private fun onSessions(list: List<MediaController>) {
        controllers = list
        // Preferiamo la sessione che sta suonando, altrimenti la più recente (la prima).
        val best = list.firstOrNull { it.playbackState?.state == PlaybackState.STATE_PLAYING }
            ?: list.firstOrNull { it.packageName == current?.packageName }
            ?: list.firstOrNull()
        if (best?.sessionToken != current?.sessionToken) {
            current?.unregisterCallback(controllerCallback)
            current = best
            best?.registerCallback(controllerCallback, handler)
        }
        rebuild()
    }

    private fun rebuild() {
        val c = current
        if (c == null) {
            _state.value = null
            return
        }
        val md = c.metadata
        val pb = c.playbackState
        val title = md?.getString(MediaMetadata.METADATA_KEY_TITLE)
            ?: md?.getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
            ?: ""
        val artist = md?.getString(MediaMetadata.METADATA_KEY_ARTIST)
            ?: md?.getString(MediaMetadata.METADATA_KEY_ALBUM_ARTIST)
            ?: md?.getString(MediaMetadata.METADATA_KEY_DISPLAY_SUBTITLE)
            ?: ""
        val album = md?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""
        val art = md?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
            ?: md?.getBitmap(MediaMetadata.METADATA_KEY_ART)
            ?: md?.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON)
        val actions = pb?.actions ?: 0L
        val label = try {
            val pm = context.packageManager
            pm.getApplicationLabel(pm.getApplicationInfo(c.packageName, 0)).toString()
        } catch (_: Exception) {
            c.packageName
        }
        _state.value = MediaState(
            packageName = c.packageName,
            appLabel = label,
            title = title,
            artist = artist,
            album = album,
            art = art,
            isPlaying = pb?.state == PlaybackState.STATE_PLAYING ||
                pb?.state == PlaybackState.STATE_BUFFERING,
            durationMs = md?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
            positionMs = pb?.position ?: 0L,
            positionUpdatedAt = pb?.lastPositionUpdateTime?.takeIf { it > 0 }
                ?: SystemClock.elapsedRealtime(),
            playbackSpeed = pb?.playbackSpeed?.takeIf { it > 0f } ?: 1f,
            canSkipNext = pb == null || (actions and PlaybackState.ACTION_SKIP_TO_NEXT) != 0L,
            canSkipPrevious = pb == null || (actions and PlaybackState.ACTION_SKIP_TO_PREVIOUS) != 0L,
        )
    }

    fun playPause() {
        val c = current ?: return
        if (c.playbackState?.state == PlaybackState.STATE_PLAYING) {
            c.transportControls.pause()
        } else {
            c.transportControls.play()
        }
    }

    fun next() {
        current?.transportControls?.skipToNext()
    }

    fun previous() {
        current?.transportControls?.skipToPrevious()
    }

    /** Apre l'app che sta riproducendo, oppure il lettore musicale predefinito. */
    fun openPlayer() {
        val c = current
        if (c != null) {
            val pi = c.sessionActivity
            if (pi != null && pi.sendFromForeground(context)) return
            context.packageManager.getLaunchIntentForPackage(c.packageName)?.let {
                context.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                return
            }
        }
        try {
            val music = Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(music)
        } catch (_: Exception) {
        }
    }
}
