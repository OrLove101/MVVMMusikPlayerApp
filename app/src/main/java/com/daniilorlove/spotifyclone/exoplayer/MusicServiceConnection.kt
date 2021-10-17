package com.daniilorlove.spotifyclone.exoplayer

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.daniilorlove.spotifyclone.util.Event
import com.daniilorlove.spotifyclone.util.Resource
import dagger.hilt.android.qualifiers.ApplicationContext

class MusicServiceConnection(
    @ApplicationContext context: Context
) {
    private val _isConnected = MutableLiveData<Event<Resource<Boolean>>>()
    val isConnected: LiveData<Event<Resource<Boolean>>> = _isConnected

    private val _networkError = MutableLiveData<Event<Resource<Boolean>>>()
    val networkError: LiveData<Event<Resource<Boolean>>> = _networkError

    private val _playbackState = MutableLiveData<PlaybackStateCompat?>()
    val playbackState: LiveData<PlaybackStateCompat?> = _playbackState

    private val _curPlayingSong = MutableLiveData<MediaMetadataCompat>()
    val curPlayingSong: LiveData<MediaMetadataCompat> = _curPlayingSong
}