package com.example.djmussic.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.djmussic.R
import com.example.djmussic.data.Song

class SongAdapter(
    private var songs: List<Song>,
    private val onPlayPause: (Song, Boolean) -> Unit
) : RecyclerView.Adapter<SongAdapter.ViewHolder>() {

    private var currentPlayingUri: String? = null

    class ViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.song_title)
        val artistView: TextView = view.findViewById(R.id.song_artist)
        val playPauseBtn: ImageButton = view.findViewById(R.id.play_pause_btn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = songs.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs[position]
        holder.titleView.text = song.title
        holder.artistView.text = song.artist

        // Cập nhật icon dựa vào bài hát đang phát
        val isPlaying = currentPlayingUri == song.uri
        holder.playPauseBtn.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )

        holder.playPauseBtn.setOnClickListener {
            val willPlay = currentPlayingUri != song.uri
            onPlayPause(song, willPlay)
            if (willPlay) {
                currentPlayingUri = song.uri
            } else {
                currentPlayingUri = null
            }
            notifyDataSetChanged()
        }
    }

    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        currentPlayingUri = null
        notifyDataSetChanged()
    }

    fun resetPlayingState() {
        currentPlayingUri = null
        notifyDataSetChanged()
    }
}