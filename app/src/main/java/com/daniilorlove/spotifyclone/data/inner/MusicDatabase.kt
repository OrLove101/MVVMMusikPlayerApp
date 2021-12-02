package com.daniilorlove.spotifyclone.data.inner

import android.content.Context
import com.daniilorlove.spotifyclone.R
import com.daniilorlove.spotifyclone.data.models.Song
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject

class MusicDatabase @Inject constructor(@ApplicationContext val context: Context) {

    private fun readTextFile(inputStream: InputStream): String{
        val outputStream = ByteArrayOutputStream()
        val buf = ByteArray(1024)
        var len: Int
        try {
            while (inputStream.read(buf).also { len = it } != -1) {
                outputStream.write(buf, 0, len)
            }
            outputStream.close()
            inputStream.close()
        } catch (e: IOException) {
        }
        return outputStream.toString()
    }

    public fun getAllSongs(): List<Song>? {
        return try {
            val rawSongs: InputStream =
                context.resources.openRawResource(R.raw.songs) // TODO paste context as parameter in class and get resources from it
            val strSongs: String = readTextFile(rawSongs)
            val gson = Gson()
            val listSongType = object : TypeToken<List<Song>>() {}.type // may be another typeToken

            gson.fromJson(strSongs, listSongType);
        } catch (e: Throwable) {
            Timber.d("Songs conversion error")
            emptyList()
        }
    }
}