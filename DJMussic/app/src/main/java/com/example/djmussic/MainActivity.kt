package com.example.djmussic

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.djmussic.adapter.FeatureAdapter
import com.example.djmussic.adapter.FeatureItem
import com.example.djmussic.adapter.SongAdapter
import com.example.djmussic.data.MusicRepository
import com.example.djmussic.service.MusicService
import com.example.djmussic.ui.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {

    private lateinit var featuresRecyclerView: RecyclerView
    private lateinit var songsRecyclerView: RecyclerView
    private val repo = MusicRepository()
    private var songAdapter: SongAdapter? = null
    private var musicService: MusicService? = null
    private var isBound = false

    private val features = listOf(
        FeatureItem("DJ Mixer", R.drawable.ic_dj_mixer, DjMixerActivity::class.java),
        FeatureItem("Drum Pad", R.drawable.ic_drum_pad, DrumPadActivity::class.java),
        FeatureItem("Ringtones Cutter", R.drawable.ic_cutter, RingtoneCutterActivity::class.java),
        FeatureItem("Audio Mixer", R.drawable.ic_audio_mixer, AudioMixerActivity::class.java),
        FeatureItem("Sound Merger", R.drawable.ic_merger, SoundMergerActivity::class.java)
    )

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.MusicBinder
            musicService = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupBottomNavigation()
        setupFeaturesGrid()
        setupLibraryAndSettings()

        // Bind service
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)

        // Khởi tạo adapter rỗng trước
        setupEmptySongList()

        if (hasPermission()) {
            loadSongs()
        } else {
            requestPermission()
        }
    }

    private fun initViews() {
        featuresRecyclerView = findViewById(R.id.features_grid)
        songsRecyclerView = findViewById(R.id.songs_recycler_view)
    }

    private fun setupEmptySongList() {
        songAdapter = SongAdapter(emptyList()) { song, shouldPlay ->
            if (shouldPlay) {
                playSong(song.uri)
            } else {
                pauseSong()
            }
        }
        songsRecyclerView.layoutManager = LinearLayoutManager(this)
        songsRecyclerView.adapter = songAdapter
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_iap -> {
                    Toast.makeText(this, "In-App Purchases", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }

    private fun setupFeaturesGrid() {
        val featureAdapter = FeatureAdapter(features) { feature ->
            feature.targetActivity?.let {
                startActivity(Intent(this, it))
            } ?: run {
                Toast.makeText(this, "${feature.title} - Coming Soon!", Toast.LENGTH_SHORT).show()
            }
        }
        featuresRecyclerView.layoutManager = LinearLayoutManager(this)
        featuresRecyclerView.adapter = featureAdapter
    }

    private fun setupLibraryAndSettings() {
        val libraryCard = findViewById<CardView>(R.id.library_card)
        val settingsCard = findViewById<CardView>(R.id.settings_card)

        libraryCard.setOnClickListener {
            openMyLibrary()
        }

        settingsCard.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
    }

    private fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED
        } else {
            checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                loadSongs()
            } else {
                Toast.makeText(this, "Permission denied! Cannot access music files.", Toast.LENGTH_LONG).show()
                songAdapter?.updateSongs(emptyList())
            }
        }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            permissionLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun loadSongs() {
        lifecycleScope.launch {
            val songs = repo.getSongs(this@MainActivity)
            if (songs.isEmpty()) {
                Toast.makeText(this@MainActivity, "No songs found on device!", Toast.LENGTH_LONG).show()
            }
            songAdapter?.updateSongs(songs)
        }
    }

    private fun playSong(uri: String) {
        try {
            val intent = Intent(this, MusicService::class.java).apply {
                action = MusicService.ACTION_PLAY
                putExtra(MusicService.EXTRA_URI, uri)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
            Toast.makeText(this, "Playing...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error playing: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pauseSong() {
        musicService?.pause()
        Toast.makeText(this, "Paused", Toast.LENGTH_SHORT).show()
    }
    // Thêm hàm này vào MainActivity
    private fun openMyLibrary() {
        val intent = Intent(this, MyLibraryActivity::class.java)
        startActivity(intent)
    }



    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(serviceConnection)
            isBound = false
        }
        val intent = Intent(this, MusicService::class.java).apply {
            action = MusicService.ACTION_STOP
        }
        stopService(intent)
    }
}