package com.daniilorlove.spotifyclone.exoplayer

import android.app.PendingIntent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.daniilorlove.spotifyclone.exoplayer.callbacks.MusicPlayerNotificationListener
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel


private const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
class MusicService(
    private val dataSourceFactory: DefaultDataSourceFactory,
    private val exoPlayer: SimpleExoPlayer
): MediaBrowserServiceCompat() {

    private var musicNotificationManager: MusicNotificationManager? = null

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null

    var isForegroundService = false

    override fun onCreate() {
        super.onCreate()
        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        mediaSession?.let {
            sessionToken = it.sessionToken
            musicNotificationManager = MusicNotificationManager(
                this,
                it.sessionToken,
                MusicPlayerNotificationListener(this)
            ) {
                // TODO
            }
            mediaSessionConnector = MediaSessionConnector(it)
        }
        mediaSessionConnector?.setPlayer(exoPlayer)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        TODO("Not yet implemented")
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        TODO("Not yet implemented")
    }
}