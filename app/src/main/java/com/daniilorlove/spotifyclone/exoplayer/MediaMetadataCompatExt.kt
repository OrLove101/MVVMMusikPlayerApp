package com.daniilorlove.spotifyclone.exoplayer

import android.support.v4.media.MediaMetadataCompat
import com.daniilorlove.spotifyclone.data.models.Song

fun MediaMetadataCompat.toSong(): Song? {
    return this.description?.let {
        Song(
            it.mediaId ?: "",
            it.title.toString(),
            it.description.toString(),
            it.iconUri.toString(),
            it.mediaUri.toString(),
            0
        )
    }
}