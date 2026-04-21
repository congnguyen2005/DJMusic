package com.example.djmussic.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.djmussic.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider

class AudioMixerActivity : AppCompatActivity() {

    private lateinit var playBtn: MaterialButton
    private lateinit var addTrackBtn: MaterialButton
    private lateinit var volumeSliders: MutableList<Slider>
    private lateinit var trackNames: MutableList<TextView>
    private lateinit var trackListContainer: LinearLayout

    private val mediaPlayers = mutableListOf<MediaPlayer>()
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audio_mixer)

        volumeSliders = mutableListOf()
        trackNames = mutableListOf()

        setupToolbar()
        initViews()
        setupControls()
        addDemoTracks()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Audio Mixer"
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun initViews() {
        playBtn = findViewById(R.id.play_btn)
        addTrackBtn = findViewById(R.id.add_track_btn)
        trackListContainer = findViewById(R.id.track_list_container)
    }

    private fun setupControls() {
        playBtn.setOnClickListener {
            if (isPlaying) {
                pauseAllTracks()
                playBtn.icon = getDrawable(R.drawable.ic_play)
            } else {
                playAllTracks()
                playBtn.icon = getDrawable(R.drawable.ic_pause)
            }
        }

        addTrackBtn.setOnClickListener {
            addTrack("Track ${mediaPlayers.size + 1}")
        }
    }

    private fun addDemoTracks() {
        addTrack("Demo Track 1")
        addTrack("Demo Track 2")
        addTrack("Demo Track 3")
    }

    private fun addTrack(name: String) {
        val trackView = layoutInflater.inflate(R.layout.item_track, trackListContainer, false)
        val trackName = trackView.findViewById<TextView>(R.id.track_name)
        val volumeSlider = trackView.findViewById<Slider>(R.id.track_volume)
        val soloBtn = trackView.findViewById<ImageButton>(R.id.solo_btn)
        val muteBtn = trackView.findViewById<ImageButton>(R.id.mute_btn)

        trackName.text = name
        volumeSlider.value = 0.5f

        // Tạo MediaPlayer với file silent (hoặc demo nếu có)
        val mediaPlayer = try {
            MediaPlayer.create(this, R.raw.silent).apply {
                isLooping = true
                setVolume(0.5f, 0.5f)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Add audio files to raw folder", Toast.LENGTH_SHORT).show()
            MediaPlayer().apply {
                // Dummy player
            }
        }
        mediaPlayers.add(mediaPlayer)

        volumeSlider.addOnChangeListener { _, value, _ ->
            val index = volumeSliders.indexOf(volumeSlider)
            if (index in 0 until mediaPlayers.size) {
                mediaPlayers[index].setVolume(value, value)
            }
        }

        var isMuted = false
        muteBtn.setOnClickListener {
            isMuted = !isMuted
            volumeSlider.isEnabled = !isMuted
            val index = volumeSliders.indexOf(volumeSlider)
            if (index in 0 until mediaPlayers.size) {
                mediaPlayers[index].setVolume(
                    if (isMuted) 0f else volumeSlider.value,
                    if (isMuted) 0f else volumeSlider.value
                )
            }
            muteBtn.setImageResource(if (isMuted) R.drawable.ic_mute else R.drawable.ic_volume_up)
        }

        var isSolo = false
        soloBtn.setOnClickListener {
            isSolo = !isSolo
            soloBtn.setImageResource(if (isSolo) R.drawable.ic_solo_active else R.drawable.ic_solo)
            if (isSolo) {
                for (i in mediaPlayers.indices) {
                    mediaPlayers[i].setVolume(
                        if (i == volumeSliders.indexOf(volumeSlider)) 1f else 0f,
                        if (i == volumeSliders.indexOf(volumeSlider)) 1f else 0f
                    )
                }
            } else {
                for (i in mediaPlayers.indices) {
                    mediaPlayers[i].setVolume(volumeSliders[i].value, volumeSliders[i].value)
                }
            }
        }

        trackListContainer.addView(trackView)
        volumeSliders.add(volumeSlider)
        trackNames.add(trackName)
    }

    private fun playAllTracks() {
        for (player in mediaPlayers) {
            if (!player.isPlaying) {
                player.start()
            }
        }
        isPlaying = true
    }

    private fun pauseAllTracks() {
        for (player in mediaPlayers) {
            if (player.isPlaying) {
                player.pause()
            }
        }
        isPlaying = false
    }

    override fun onDestroy() {
        super.onDestroy()
        for (player in mediaPlayers) {
            player.release()
        }
    }
}
