package com.example.djmussic.ui

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.djmussic.R
import java.io.File
import java.io.FileOutputStream

class SoundMergerActivity : AppCompatActivity() {

    private val audioFiles = mutableListOf<Uri>()
    private lateinit var filesContainer: LinearLayout
    private lateinit var addFileBtn: Button
    private lateinit var mergeBtn: Button
    private lateinit var clearBtn: Button

    private val selectFileLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        for (uri in uris) {
            addAudioFile(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sound_merger)

        setupToolbar()
        initViews()
        setupListeners()
        checkPermission()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Sound Merger"
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun initViews() {
        filesContainer = findViewById(R.id.files_container)
        addFileBtn = findViewById(R.id.add_file_btn)
        mergeBtn = findViewById(R.id.merge_btn)
        clearBtn = findViewById(R.id.clear_btn)
    }

    private fun setupListeners() {
        addFileBtn.setOnClickListener {
            selectFileLauncher.launch("audio/*")
        }

        mergeBtn.setOnClickListener {
            if (audioFiles.size >= 2) {
                mergeAudioFiles()
            } else {
                Toast.makeText(this, "Select at least 2 audio files to merge!", Toast.LENGTH_SHORT).show()
            }
        }

        clearBtn.setOnClickListener {
            clearAllFiles()
        }
    }

    private fun addAudioFile(uri: Uri) {
        audioFiles.add(uri)

        val fileName = getFileName(uri)

        val fileView = layoutInflater.inflate(R.layout.item_audio_file, filesContainer, false)
        val nameText = fileView.findViewById<TextView>(R.id.file_name)
        val removeBtn = fileView.findViewById<Button>(R.id.remove_file_btn)

        nameText.text = fileName
        removeBtn.setOnClickListener {
            val index = filesContainer.indexOfChild(fileView)
            if (index >= 0 && index < audioFiles.size) {
                audioFiles.removeAt(index)
                filesContainer.removeView(fileView)
                updateMergeButtonState()
            }
        }

        filesContainer.addView(fileView)
        updateMergeButtonState()

        Toast.makeText(this, "Added: $fileName", Toast.LENGTH_SHORT).show()
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "Unknown"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    fileName = it.getString(nameIndex) ?: "Unknown"
                }
            }
        }
        return fileName
    }

    private fun mergeAudioFiles() {
        try {
            val outputFileName = "merged_audio_${System.currentTimeMillis()}.mp3"

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
                        mergeAudioStreams(  outputStream)
                    }
                    Toast.makeText(this, "Merged file saved to Music/DJAudioTools/", Toast.LENGTH_LONG).show()
                }
            } else {
                val musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
                val appDir = File(musicDir, "DJAudioTools")
                if (!appDir.exists()) appDir.mkdirs()

                val outputFile = File(appDir, outputFileName)
                FileOutputStream(outputFile).use { outputStream ->
                    mergeAudioStreams(outputStream)
                }
                Toast.makeText(this, "Merged file saved to ${outputFile.absolutePath}", Toast.LENGTH_LONG).show()
            }

            Toast.makeText(this, "Merge completed successfully!", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Error merging: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    private fun mergeAudioStreams(outputStream: java.io.OutputStream) {
        val buffer = ByteArray(8192)

        for (uri in audioFiles) {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                    outputStream.write(buffer, 0, bytesRead)
                }
            }
        }
    }

    private fun clearAllFiles() {
        audioFiles.clear()
        filesContainer.removeAllViews()
        updateMergeButtonState()
        Toast.makeText(this, "All files cleared", Toast.LENGTH_SHORT).show()
    }

    private fun updateMergeButtonState() {
        mergeBtn.isEnabled = audioFiles.size >= 2
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
}