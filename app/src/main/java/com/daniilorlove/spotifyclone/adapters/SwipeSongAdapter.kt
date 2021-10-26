package com.daniilorlove.spotifyclone.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.daniilorlove.spotifyclone.data.models.Song
import com.daniilorlove.spotifyclone.databinding.ListItemBinding
import com.daniilorlove.spotifyclone.databinding.SwipeItemBinding
import javax.inject.Inject

class SwipeSongAdapter @Inject constructor(
    private val glide: RequestManager
): RecyclerView.Adapter<SwipeSongAdapter.SongViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val binding = SwipeItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val currentItem = songs[position]
        holder.bind(currentItem)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    private val diffCallback = object : DiffUtil.ItemCallback<Song>() {
        override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.mediaId == newItem.mediaId
        }

        override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var songs: List<Song>
        get() = differ.currentList
        set(value) = differ.submitList(value)

    private var onItemClickListener: ((Song) -> Unit)? = null

    fun setOnItemClickListener(listener: (Song) -> Unit) {
        onItemClickListener = listener
    }

    inner class SongViewHolder(private val binding: SwipeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Song) {
            binding.apply {
                val text = "${item.title}"
                tvPrimary.text = text

                root.setOnClickListener {
                    onItemClickListener?.let { click ->
                        click(item)
                    }
                }
            }
        }
    }
}