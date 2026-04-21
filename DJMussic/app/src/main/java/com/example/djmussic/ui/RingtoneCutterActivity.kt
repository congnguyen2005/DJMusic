package com.example.djmussic.ui

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.djmussic.R
import java.io.File
import java.io.FileOutputStream

class RingtoneCutterActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var startTimeText: TextView
    private lateinit var endTimeText: TextView
    private lateinit var currentTimeText: TextView
    private lateinit var selectFileBtn: Button
    private lateinit var setStartBtn: Button
    private lateinit var setEndBtn: Button
    private lateinit var cutBtn: Button
    private lateinit var playBtn: Button
    private lateinit var pauseBtn: Button

    private var currentFileUri: Uri? = null
    private var startPosition = 0
    private var endPosition = 0
    private var isPlaying = false
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    private val selectFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { loadAudioFile(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ringtone_cutter)

        setupToolbar()
        initViews()
        setupListeners()
        checkPermission()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Ringtone Cutter"
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun initViews() {
        seekBar = findViewById(R.id.seek_bar)
        startTimeText = findViewById(R.id.start_time_text)
        endTimeText = findViewById(R.id.end_time_text)
        currentTimeText = findViewById(R.id.current_time_text)
        selectFileBtn = findViewById(R.id.select_file_btn)
        setStartBtn = findViewById(R.id.set_start_btn)
        setEndBtn = findViewById(R.id.set_end_btn)
        cutBtn = findViewById(R.id.cut_btn)
        playBtn = findViewById(R.id.play_btn)
        pauseBtn = findViewById(R.id.pause_btn)

        setStartBtn.isEnabled = false
        setEndBtn.isEnabled = false
        cutBtn.isEnabled = false
        pauseBtn.isEnabled = false
    }

    private fun setupListeners() {
        selectFileBtn.setOnClickListener {
            selectFileLauncher.launch("audio/*")
        }

        setStartBtn.setOnClickListener {
            startPosition = mediaPlayer.currentPosition
            startTimeText.text = formatTime(startPosition)
            Toast.makeText(this, "Start position set", Toast.LENGTH_SHORT).show()
        }

        setEndBtn.setOnClickListener {
            endPosition = mediaPlayer.currentPosition
            endTimeText.text = formatTime(endPosition)
            cutBtn.isEnabled = true
            Toast.makeText(this, "End position set", Toast.LENGTH_SHORT).show()
        }

        cutBtn.setOnClickListener {
            if (startPosition < endPosition) {
                cutAudio()
            } else {
                Toast.makeText(this, "End position must be after start!", Toast.LENGTH_SHORT).show()
            }
        }

        playBtn.setOnClickListener {
            if (currentFileUri != null && !isPlaying) {
                mediaPlayer.start()
                isPlaying = true
                startUpdatingSeekBar()
            }
        }

        pauseBtn.setOnClickListener {
            if (isPlaying) {
                mediaPlayer.pause()
                isPlaying = false
                handler.removeCallbacks(updateSeekBarRunnable)
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                    currentTimeText.text = formatTime(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun loadAudioFile(uri: Uri) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(this@RingtoneCutterActivity, uri)
                prepare()
                isLooping = false
            }

            currentFileUri = uri
            val duration = mediaPlayer.duration
            seekBar.max = duration
            endPosition = duration
            endTimeText.text = formatTime(duration)
            startTimeText.text = formatTime(0)

            setStartBtn.isEnabled = true
            setEndBtn.isEnabled = true
            playBtn.isEnabled = true
            pauseBtn.isEnabled = true

            Toast.makeText(this, "File loaded: ${formatTime(duration)}", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun cutAudio() {
        val duration = endPosition - startPosition
        if (duration <= 0) {
            Toast.makeText(this, "Invalid selection!", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val outputFileName = "ringtone_${System.currentTimeMillis()}.mp3"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, outputFileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "audio/mpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_MUSIC}/DJAudioTools")
                }

                val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { outputStream ->
                        // Copy selected segment
                        val inputStream = contentResolver.openInputStream(currentFileUri!!)
                        inputStream?.skip(startPosition.toLong())
                        val buffer = ByteArray(8192)
                        var totalRead = 0
                        while (totalRead < duration) {
                            val bytesToRead = minOf(buffer.size, duration - totalRead)
                            val bytesRead = inputStream?.read(buffer, 0, bytesToRead) ?: -1
                            if (bytesRead == -1) break
                            outputStream.write(buffer, 0, bytesRead)
                            totalRead += bytesRead
                        }
                        inputStream?.close()
                    }
                    Toast.makeText(this, "Ringtone saved to Music/DJAudioTools/", Toast.LENGTH_LONG).show()
                }
            } else {
                val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                val appDir = File(musicDir, "DJAudioTools")
                if (!appDir.exists()) appDir.mkdirs()

                val outputFile = File(appDir, outputFileName)
                FileOutputStream(outputFile).use { outputStream ->
                    val inputStream = contentResolver.openInputStream(currentFileUri!!)
                    inputStream?.skip(startPosition.toLong())
                    val buffer = ByteArray(8192)
                    var totalRead = 0
                    while (totalRead < duration) {
                        val bytesToRead = minOf(buffer.size, duration - totalRead)
                        val bytesRead = inputStream?.read(buffer, 0, bytesToRead) ?: -1
                        if (bytesRead == -1) break
                        outputStream.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                    }
                    inputStream?.close()
                }
                Toast.makeText(this, "Ringtone saved to ${outputFile.absolutePath}", Toast.LENGTH_LONG).show()
            }

            AlertDialog.Builder(this)
                .setTitle("Success!")
                .setMessage("Ringtone saved successfully!")
                .setPositiveButton("OK", null)
                .show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error cutting audio: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private val updateSeekBarRunnable = object : Runnable {
        override fun run() {
            if (isPlaying && ::mediaPlayer.isInitialized) {
                val currentPosition = mediaPlayer.currentPosition
                seekBar.progress = currentPosition
                currentTimeText.text = formatTime(currentPosition)
                handler.postDelayed(this, 500)
            }
        }
    }

    private fun startUpdatingSeekBar() {
        handler.post(updateSeekBarRunnable)
    }

    private fun formatTime(millis: Int): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_MEDIA_AUDIO), 100)
            }
        } else if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        handler.removeCallbacks(updateSeekBarRunnable)
    }
}