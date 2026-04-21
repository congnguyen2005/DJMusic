package com.example.djmussic.ui

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.djmussic.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.FileOutputStream

class DrumPadActivity : AppCompatActivity() {

    private lateinit var soundPool: SoundPool
    private val soundMap = mutableMapOf<Int, Int>()
    private val soundNames = mapOf(
        R.id.pad_kick to "Kick",
        R.id.pad_snare to "Snare",
        R.id.pad_hihat to "Hi-Hat",
        R.id.pad_crash to "Crash",
        R.id.pad_tom1 to "Tom Low",
        R.id.pad_tom2 to "Tom High",
        R.id.pad_ride to "Ride",
        R.id.pad_clap to "Clap"
    )

    private val recordedPatterns = mutableListOf<RecordedNote>()
    private var isRecording = false
    private var recordingStartTime = 0L
    private val savedPatterns = mutableListOf<SavedPattern>()
    private var isPlayingPattern = false
    private val playbackHandler = Handler(Looper.getMainLooper())

    private lateinit var volumeSeekBar: SeekBar
    private lateinit var volumeText: TextView
    private lateinit var recordBtn: FloatingActionButton
    private lateinit var playBtn: FloatingActionButton
    private lateinit var stopBtn: FloatingActionButton
    private lateinit var clearBtn: Button
    private lateinit var saveBtn: Button
    private lateinit var patternsContainer: LinearLayout
    private lateinit var vibrator: Vibrator

    private var currentVolume = 0.8f

    data class RecordedNote(val padId: Int, val timestamp: Long, val soundName: String)
    data class SavedPattern(val id: String, val name: String, val notes: List<RecordedNote>)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_drum_pad)

        setupToolbar()
        initViews()
        setupSoundPool()
        setupDrumPads()
        setupControls()
        setupVibrator()

        Toast.makeText(this, "Tap pads to play! Press REC to record your beat.", Toast.LENGTH_LONG).show()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Drum Pad"
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun initViews() {
        volumeSeekBar = findViewById(R.id.volume_seekbar)
        volumeText = findViewById(R.id.volume_text)
        recordBtn = findViewById(R.id.record_btn)
        playBtn = findViewById(R.id.play_btn)
        stopBtn = findViewById(R.id.stop_btn)
        clearBtn = findViewById(R.id.clear_btn)
        saveBtn = findViewById(R.id.save_btn)
        patternsContainer = findViewById(R.id.patterns_container)
    }

    private fun setupVibrator() {
        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
    }

    private fun setupSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(10)
            .setAudioAttributes(audioAttributes)
            .build()

        val sampleSound = R.raw.silent
        soundMap[R.id.pad_kick] = soundPool.load(this, sampleSound, 1)
        soundMap[R.id.pad_snare] = soundPool.load(this, sampleSound, 1)
        soundMap[R.id.pad_hihat] = soundPool.load(this, sampleSound, 1)
        soundMap[R.id.pad_crash] = soundPool.load(this, sampleSound, 1)
        soundMap[R.id.pad_tom1] = soundPool.load(this, sampleSound, 1)
        soundMap[R.id.pad_tom2] = soundPool.load(this, sampleSound, 1)
        soundMap[R.id.pad_ride] = soundPool.load(this, sampleSound, 1)
        soundMap[R.id.pad_clap] = soundPool.load(this, sampleSound, 1)
    }

    private fun setupDrumPads() {
        soundNames.keys.forEach { padId ->
            val pad = findViewById<ImageButton>(padId)
            pad.setOnClickListener {
                playSound(padId)
                animatePad(pad)
                if (isRecording) {
                    recordNote(padId)
                }
            }
        }
    }

    private fun playSound(padId: Int) {
        soundMap[padId]?.let { soundId ->
            soundPool.play(soundId, currentVolume, currentVolume, 1, 0, 1f)
            vibrate()
        }
    }

    private fun animatePad(pad: ImageButton) {
        try {
            val scaleDown = AnimationUtils.loadAnimation(this, R.anim.scale_down)
            pad.startAnimation(scaleDown)
        } catch (e: Exception) { }
    }

    private fun vibrate() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                vibrator.vibrate(30)
            }
        } catch (e: Exception) { }
    }

    private fun setupControls() {
        volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentVolume = progress / 100f
                volumeText.text = "${(currentVolume * 100).toInt()}%"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        recordBtn.setOnClickListener {
            if (isRecording) stopRecording() else startRecording()
        }

        playBtn.setOnClickListener {
            if (recordedPatterns.isNotEmpty()) playPattern()
        }

        stopBtn.setOnClickListener {
            stopPlayback()
        }

        clearBtn.setOnClickListener {
            clearRecording()
        }

        saveBtn.setOnClickListener {
            savePattern()
        }
    }

    private fun startRecording() {
        isRecording = true
        recordedPatterns.clear()
        recordingStartTime = System.currentTimeMillis()
        recordBtn.setImageResource(android.R.drawable.ic_media_pause)
        Toast.makeText(this, "Recording... Tap pads!", Toast.LENGTH_SHORT).show()
    }

    private fun stopRecording() {
        isRecording = false
        recordBtn.setImageResource(R.drawable.ic_mic)
        Toast.makeText(this, "Recorded ${recordedPatterns.size} notes", Toast.LENGTH_SHORT).show()
        playBtn.isEnabled = recordedPatterns.isNotEmpty()
        saveBtn.isEnabled = recordedPatterns.isNotEmpty()
    }

    private fun recordNote(padId: Int) {
        val timestamp = System.currentTimeMillis() - recordingStartTime
        recordedPatterns.add(RecordedNote(padId, timestamp, soundNames[padId] ?: "Unknown"))
    }

    private fun playPattern() {
        if (recordedPatterns.isEmpty()) {
            Toast.makeText(this, "No pattern to play!", Toast.LENGTH_SHORT).show()
            return
        }
        isPlayingPattern = true
        playBtn.setImageResource(android.R.drawable.ic_media_pause)
        playNextNote(0)
    }

    private fun playNextNote(index: Int) {
        if (!isPlayingPattern || index >= recordedPatterns.size) {
            stopPlayback()
            return
        }

        val note = recordedPatterns[index]
        val delay = if (index == 0) note.timestamp else
            note.timestamp - recordedPatterns[index - 1].timestamp

        playbackHandler.postDelayed({
            playSound(note.padId)
            animatePad(findViewById(note.padId))
            playNextNote(index + 1)
        }, delay)
    }

    private fun stopPlayback() {
        isPlayingPattern = false
        playbackHandler.removeCallbacksAndMessages(null)
        playBtn.setImageResource(R.drawable.ic_play)
    }

    private fun clearRecording() {
        recordedPatterns.clear()
        stopPlayback()
        if (isRecording) stopRecording()
        playBtn.isEnabled = false
        saveBtn.isEnabled = false
        Toast.makeText(this, "Pattern cleared!", Toast.LENGTH_SHORT).show()
    }

    private fun savePattern() {
        if (recordedPatterns.isEmpty()) {
            Toast.makeText(this, "Nothing to save!", Toast.LENGTH_SHORT).show()
            return
        }

        val patternName = "Pattern ${savedPatterns.size + 1}"
        val pattern = SavedPattern(System.currentTimeMillis().toString(), patternName, recordedPatterns.toList())
        savedPatterns.add(pattern)

        addPatternToUI(pattern)
        Toast.makeText(this, "Pattern saved!", Toast.LENGTH_SHORT).show()
        clearRecording()
    }

    private fun addPatternToUI(pattern: SavedPattern) {
        val patternCard = layoutInflater.inflate(R.layout.item_pattern, patternsContainer, false)
        val nameView = patternCard.findViewById<TextView>(R.id.pattern_name)
        val infoView = patternCard.findViewById<TextView>(R.id.pattern_notes)
        val playPatternBtn = patternCard.findViewById<ImageButton>(R.id.play_pattern_btn)
        val deletePatternBtn = patternCard.findViewById<ImageButton>(R.id.delete_pattern_btn)

        nameView.text = pattern.name
        infoView.text = "${pattern.notes.size} notes"

        playPatternBtn.setOnClickListener {
            playSavedPattern(pattern.notes)
        }

        deletePatternBtn.setOnClickListener {
            savedPatterns.remove(pattern)
            patternsContainer.removeView(patternCard)
            Toast.makeText(this, "Pattern deleted", Toast.LENGTH_SHORT).show()
        }

        patternsContainer.addView(patternCard)
    }

    private fun playSavedPattern(notes: List<RecordedNote>) {
        var index = 0
        fun playNext() {
            if (index >= notes.size) return
            val note = notes[index]
            val delay = if (index == 0) note.timestamp else
                note.timestamp - notes[index - 1].timestamp
            playbackHandler.postDelayed({
                playSound(note.padId)
                animatePad(findViewById(note.padId))
                index++
                playNext()
            }, delay)
        }
        playNext()
    }

    override fun onDestroy() {
        soundPool.release()
        playbackHandler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}