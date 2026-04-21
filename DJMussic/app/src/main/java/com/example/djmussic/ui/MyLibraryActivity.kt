package com.example.djmussic.ui

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.djmussic.R
import com.example.djmussic.data.MusicRepository
import com.example.djmussic.data.Song
import kotlinx.coroutines.launch
import kotlinx.coroutines.MainScope
import java.io.File

class MyLibraryActivity : AppCompatActivity() {

    private lateinit var songsRecyclerView: RecyclerView
    private lateinit var folderContainer: LinearLayout
    private lateinit var scanBtn: Button
    private lateinit var playlistsContainer: LinearLayout

    private val repo = MusicRepository()
    private val mainScope = MainScope()
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_library)

        setupToolbar()
        initViews()
        setupListeners()
        checkPermission()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "My Library"
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun initViews() {
        songsRecyclerView = findViewById(R.id.songs_recycler_view)
        folderContainer = findViewById(R.id.folder_container)
        scanBtn = findViewById(R.id.scan_btn)
        playlistsContainer = findViewById(R.id.playlists_container)
    }

    private fun setupListeners() {
        scanBtn.setOnClickListener {
            scanMedia()
        }

        loadFolders()
        loadPlaylists()
    }

    private fun loadFolders() {
        val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
        if (musicDir.exists()) {
            val folders = musicDir.listFiles()?.filter { it.isDirectory } ?: emptyList()
            for (folder in folders) {
                addFolderToUI(folder)
            }
        }
    }

    private fun addFolderToUI(folder: File) {
        val folderView = layoutInflater.inflate(R.layout.item_folder, folderContainer, false)
        val folderName = folderView.findViewById<TextView>(R.id.folder_name)
        val songCount = folderView.findViewById<TextView>(R.id.song_count)
        val openBtn = folderView.findViewById<Button>(R.id.open_folder_btn)

        val songsInFolder = folder.listFiles()?.filter { it.extension in listOf("mp3", "wav", "m4a") } ?: emptyList()
        folderName.text = folder.name
        songCount.text = "${songsInFolder.size} songs"

        openBtn.setOnClickListener {
            if (songsInFolder.isNotEmpty()) {
                showSongsDialog(songsInFolder.map {
                    Song(title = it.nameWithoutExtension, artist = "Folder", uri = it.absolutePath)
                })
            } else {
                Toast.makeText(this, "No songs in this folder", Toast.LENGTH_SHORT).show()
            }
        }

        folderContainer.addView(folderView)
    }

    private fun loadPlaylists() {
        val playlists = listOf("Favorites", "Recently Added", "Most Played", "My Playlist")
        for (playlist in playlists) {
            addPlaylist(playlist)
        }
    }

    private fun addPlaylist(name: String) {
        val playlistView = layoutInflater.inflate(R.layout.item_playlist, playlistsContainer, false)
        val playlistName = playlistView.findViewById<TextView>(R.id.playlist_name)

        playlistName.text = name

        playlistView.setOnClickListener {
            Toast.makeText(this, "Opening playlist: $name", Toast.LENGTH_SHORT).show()
        }

        playlistsContainer.addView(playlistView)
    }

    private fun scanMedia() {
        if (hasPermission()) {
            mainScope.launch {
                val songs = repo.getSongs(this@MyLibraryActivity)
                if (songs.isNotEmpty()) {
                    showSongsDialog(songs)
                } else {
                    Toast.makeText(this@MyLibraryActivity, "No songs found!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            requestPermission()
        }
    }

    private fun showSongsDialog(songs: List<Song>) {
        val songNames = songs.map { "${it.title} - ${it.artist}" }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Select Song")
            .setItems(songNames) { _, which ->
                val song = songs[which]
                playSong(song.uri)
            }
            .setPositiveButton("Close", null)
            .show()
    }

    private fun playSong(uri: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(uri)
                prepare()
                start()
            }
            Toast.makeText(this, "Playing...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error playing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 100)
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }
    }

    private fun checkPermission() {
        if (hasPermission()) {
            scanMedia()
        } else {
            requestPermission()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            scanMedia()
        } else {
            Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
    }
}