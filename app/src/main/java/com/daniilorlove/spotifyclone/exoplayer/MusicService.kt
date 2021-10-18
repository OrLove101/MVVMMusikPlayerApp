package com.daniilorlove.spotifyclone.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.daniilorlove.spotifyclone.exoplayer.callbacks.MusicPlaybackPreparer
import com.daniilorlove.spotifyclone.exoplayer.callbacks.MusicPlayerEventListener
import com.daniilorlove.spotifyclone.exoplayer.callbacks.MusicPlayerNotificationListener
import com.daniilorlove.spotifyclone.util.Constants.MEDIA_ROOT_ID
import com.daniilorlove.spotifyclone.util.Constants.NETWORK_ERROR
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import javax.inject.Inject


private const val SERVICE_TAG = "MusicService"

@AndroidEntryPoint
class MusicService: MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var musicSource: MusicSource

    private var musicNotificationManager: MusicNotificationManager? = null

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var mediaSession: MediaSessionCompat? = null
    private var mediaSessionConnector: MediaSessionConnector? = null

    var isForegroundService = false

    private var curPlayingSong: MediaMetadataCompat? = null

    private var isPlayerInitialized = false

    private var musicPlayerEventListener: MusicPlayerEventListener? = null

    companion object {
        var curSongDuration = 0L
            private set
    }

    override fun onCreate() {
        super.onCreate()

        musicSource.fetchMediaData()

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
                curSongDuration = exoPlayer.duration
            }

            val musicPlaybackPreparer = MusicPlaybackPreparer(musicSource) { mediaMetadataCompat ->
                curPlayingSong = mediaMetadataCompat
                preparePlayer(
                    musicSource.songs,
                    mediaMetadataCompat,
                    true
                )
            }

            mediaSessionConnector = MediaSessionConnector(it)
            mediaSessionConnector?.setPlaybackPreparer(musicPlaybackPreparer)
        }
        mediaSessionConnector?.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector?.setPlayer(exoPlayer)

        musicPlayerEventListener = MusicPlayerEventListener(this)
        musicPlayerEventListener?.let { exoPlayer.addListener(it) }
        musicNotificationManager?.showNotification(exoPlayer)
    }

    private inner class MusicQueueNavigator: TimelineQueueNavigator(mediaSession!!) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
           return musicSource.songs[windowIndex].description
        }
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val curSongIndex = if (curPlayingSong == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.prepare(musicSource.asMediaSource(dataSourceFactory))
        exoPlayer.seekTo(curSongIndex, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()

        musicPlayerEventListener?.let { exoPlayer.removeListener(it) }

        exoPlayer.release()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            MEDIA_ROOT_ID -> {
                val resultsSent = musicSource.whenReady { isInitialized ->
                    if (isInitialized) {
                        result.sendResult(musicSource.asMediaItems())
                        if (!isPlayerInitialized && musicSource.songs.isNotEmpty()) {
                            preparePlayer(musicSource.songs, musicSource.songs[0], false)
                            isPlayerInitialized = true
                        }
                    } else {
                        mediaSession?.sendSessionEvent(NETWORK_ERROR, null)
                        result.sendResult(null)
                    }
                }
                if (!resultsSent) {
                    result.detach()
                }
            }
        }
    }
}