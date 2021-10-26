package com.daniilorlove.spotifyclone.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.daniilorlove.spotifyclone.R
import com.daniilorlove.spotifyclone.adapters.SwipeSongAdapter
import com.daniilorlove.spotifyclone.data.inner.MusicDatabase
import com.daniilorlove.spotifyclone.data.models.Song
import com.daniilorlove.spotifyclone.databinding.ActivityMainBinding
import com.daniilorlove.spotifyclone.exoplayer.isPlaying
import com.daniilorlove.spotifyclone.exoplayer.toSong
import com.daniilorlove.spotifyclone.ui.viewmodels.MainViewModel
import com.daniilorlove.spotifyclone.util.Status
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity @Inject constructor() : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    @Inject
    lateinit var glide: RequestManager

    private var curPlayingSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        subscribeToObservers()

        binding.vpSong.adapter = swipeSongAdapter

        binding.vpSong.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if(playbackState?.isPlaying == true) {
                    mainViewModel.playOrToggleSong(swipeSongAdapter.songs[position])
                } else {
                    curPlayingSong = swipeSongAdapter.songs[position]
                }
            }
        })

        binding.ivPlayPause.setOnClickListener {
            curPlayingSong?.let {
                mainViewModel.playOrToggleSong(it, true)
            }
        }
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        val newItemIndex = swipeSongAdapter.songs.indexOf(song)

        if(newItemIndex != -1) {
            binding.vpSong.currentItem = newItemIndex
            curPlayingSong = song
        }
    }

    private fun subscribeToObservers() {
        mainViewModel.mediaItems.observe(this) {
            it?.let { result ->
                when(result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            swipeSongAdapter.songs = songs
                            if (songs.isNotEmpty()) {
                                glide.load((curPlayingSong ?: songs[0]).bitmapUri).into(binding.ivCurSongImage)
                            }
                            switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
            mainViewModel.curPlayingSong.observe(this) { curSong->
                if(curSong == null) return@observe

                curPlayingSong = curSong.toSong()

                glide.load(curPlayingSong?.bitmapUri).into(binding.ivCurSongImage)
                switchViewPagerToCurrentSong(curPlayingSong ?: return@observe)
            }
            mainViewModel.playbackState.observe(this) {
                playbackState = it
                binding.ivPlayPause.setImageResource(
                    if(playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
                )
            }
            mainViewModel.isConnected.observe(this) {
                it?.getContentIfNotHandled()?.let { result ->
                    when(result.status) {
                        Status.ERROR -> Snackbar.make(
                            binding.rootLayout,
                            result.message ?: "An unknown error occured",
                            Snackbar.LENGTH_LONG
                        ).show()
                        else -> Unit
                    }
                }
            }
            mainViewModel.networkError.observe(this) {
                it?.getContentIfNotHandled()?.let { result ->
                    when(result.status) {
                        Status.ERROR -> Snackbar.make(
                            binding.rootLayout,
                            result.message ?: "An unknown error occured",
                            Snackbar.LENGTH_LONG
                        ).show()
                        else -> Unit
                    }
                }
            }
        }
    }
}

private const val TAG = "MainActivity"