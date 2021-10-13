package com.daniilorlove.spotifyclone.data.models

data class Song(
    val mediaId: String,
    val title: String,
    val artist: String,
    val bitmapUri: String,
    val trackUri: String,
    val duration: Long,
)

