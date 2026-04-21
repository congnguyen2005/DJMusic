package com.example.djmussic.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.djmussic.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider

class DjMixerActivity : AppCompatActivity() {

    private lateinit var deck1Volume: Slider
    private lateinit var deck2Volume: Slider
    private lateinit var crossfader: Slider
    private lateinit var playBtn1: MaterialButton
    private lateinit var playBtn2: MaterialButton
    private lateinit var time1Tv: TextView
    private lateinit var time2Tv: TextView

    private var mediaPlayer1: MediaPlayer? = null
    private var mediaPlayer2: MediaPlayer? = null
    private var isPlaying1 = false
    private var isPlaying2 = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dj_mixer)

        setupToolbar()
        initViews()
        setupControls()
        setupMediaPlayers()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "DJ Mixer"
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun initViews() {
        deck1Volume = findViewById(R.id.deck1_volume)
        deck2Volume = findViewById(R.id.deck2_volume)
        crossfader = findViewById(R.id.crossfader)
        playBtn1 = findViewById(R.id.play_btn_1)
        playBtn2 = findViewById(R.id.play_btn_2)
        time1Tv = findViewById(R.id.time_1)
        time2Tv = findViewById(R.id.time_2)
    }

    private fun setupControls() {
        crossfader.addOnChangeListener { _, value, _ ->
            updateCrossfaderVolumes(value)
        }

        deck1Volume.addOnChangeListener { _, value, _ ->
            if (isPlaying1 && mediaPlayer1 != null) {
                val crossValue = crossfader.value
                val factor = if (crossValue <= 0.5f) 1f else (1 - (crossValue - 0.5f) * 2)
                mediaPlayer1?.setVolume(value * factor, value * factor)
            }
        }

        deck2Volume.addOnChangeListener { _, value, _ ->
            if (isPlaying2 && mediaPlayer2 != null) {
                val crossValue = crossfader.value
                val factor = if (crossValue >= 0.5f) 1f else (crossValue * 2)
                mediaPlayer2?.setVolume(value * factor, value * factor)
            }
        }

        playBtn1.setOnClickListener {
            if (mediaPlayer1 == null) {
                Toast.makeText(this, "Loading audio...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isPlaying1) {
                mediaPlayer1?.pause()
                playBtn1.icon = getDrawable(R.drawable.ic_play)
                isPlaying1 = false
                handler.removeCallbacks(updateRunnable1)
            } else {
                mediaPlayer1?.start()
                playBtn1.icon = getDrawable(R.drawable.ic_pause)
                isPlaying1 = true
                updateTime1()
            }
        }

        playBtn2.setOnClickListener {
            if (mediaPlayer2 == null) {
                Toast.makeText(this, "Loading audio...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (isPlaying2) {
                mediaPlayer2?.pause()
                playBtn2.icon = getDrawable(R.drawable.ic_play)
                isPlaying2 = false
                handler.removeCallbacks(updateRunnable2)
            } else {
                mediaPlayer2?.start()
                playBtn2.icon = getDrawable(R.drawable.ic_pause)
                isPlaying2 = true
                updateTime2()
            }
        }
    }

    private fun updateCrossfaderVolumes(value: Float) {
        val leftVol = if (value <= 0.5f) 1f else (1 - (value - 0.5f) * 2)
        val rightVol = if (value >= 0.5f) 1f else (value * 2)

        if (isPlaying1 && mediaPlayer1 != null) {
            mediaPlayer1?.setVolume(deck1Volume.value * leftVol, deck1Volume.value * leftVol)
        }
        if (isPlaying2 && mediaPlayer2 != null) {
            mediaPlayer2?.setVolume(deck2Volume.value * rightVol, deck2Volume.value * rightVol)
        }
    }

    private fun setupMediaPlayers() {
        try {
            // Sử dụng file demo_track.mp3 từ thư mục raw
            val mediaPlayer = MediaPlayer.create(this, R.raw.demo_track)

            if (mediaPlayer != null) {
                mediaPlayer1 = mediaPlayer.apply {
                    isLooping = true
                    setVolume(0.5f, 0.5f)
                }

                // Tạo mediaPlayer2 từ cùng file (hoặc có thể dùng file khác)
                mediaPlayer2 = MediaPlayer.create(this, R.raw.demo_track)?.apply {
                    isLooping = true
                    setVolume(0f, 0f)
                }

                Toast.makeText(this, "Audio loaded successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Cannot load demo_track.mp3", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private val updateRunnable1 = object : Runnable {
        override fun run() {
            mediaPlayer1?.let {
                val current = it.currentPosition / 1000
                val minutes = current / 60
                val seconds = current % 60
                time1Tv.text = String.format("%02d:%02d", minutes, seconds)
                handler.postDelayed(this, 1000)
            }
        }
    }

    private val updateRunnable2 = object : Runnable {
        override fun run() {
            mediaPlayer2?.let {
                val current = it.currentPosition / 1000
                val minutes = current / 60
                val seconds = current % 60
                time2Tv.text = String.format("%02d:%02d", minutes, seconds)
                handler.postDelayed(this, 1000)
            }
        }
    }

    private fun updateTime1() {
        handler.post(updateRunnable1)
    }

    private fun updateTime2() {
        handler.post(updateRunnable2)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer1?.release()
        mediaPlayer2?.release()
        handler.removeCallbacks(updateRunnable1)
        handler.removeCallbacks(updateRunnable2)
    }
}