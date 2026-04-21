package com.example.djmussic.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import com.example.djmussic.R

class SettingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        setupToolbar()
        setupGeneralSettings()
        setupActions()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"
        toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
    }

    private fun setupGeneralSettings() {
        // Scan Library
        findViewById<CardView>(R.id.scan_library).setOnClickListener {
            Toast.makeText(this, "Scanning library...", Toast.LENGTH_SHORT).show()
        }

        // View Recording After Completed
        val viewRecordingSwitch = findViewById<SwitchCompat>(R.id.view_recording_switch)
        viewRecordingSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Save preference
            Toast.makeText(this, "View recording: $isChecked", Toast.LENGTH_SHORT).show()
        }

        // Record Format
        findViewById<CardView>(R.id.record_format).setOnClickListener {
            showFormatDialog()
        }

        // Record Type
        findViewById<CardView>(R.id.record_type).setOnClickListener {
            showRecordTypeDialog()
        }

        // Record Path
        findViewById<CardView>(R.id.record_path).setOnClickListener {
            showPathDialog()
        }

        // Use Notification Bar
        val notificationSwitch = findViewById<SwitchCompat>(R.id.notification_switch)
        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Notification: $isChecked", Toast.LENGTH_SHORT).show()
        }

        // Hide Update Reminder
        val hideUpdateSwitch = findViewById<SwitchCompat>(R.id.hide_update_switch)
        hideUpdateSwitch.setOnCheckedChangeListener { _, isChecked ->
            Toast.makeText(this, "Hide update: $isChecked", Toast.LENGTH_SHORT).show()
        }

        // Dark Mode
        val darkModeSwitch = findViewById<SwitchCompat>(R.id.dark_mode_switch)
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
    }

    private fun setupActions() {
        // Check for Update
        findViewById<CardView>(R.id.check_update).setOnClickListener {
            Toast.makeText(this, "Checking for updates...", Toast.LENGTH_SHORT).show()
        }

        // Feedback
        findViewById<CardView>(R.id.feedback).setOnClickListener {
            sendEmail()
        }

        // Rate Us
        findViewById<CardView>(R.id.rate_us).setOnClickListener {
            openPlayStore()
        }

        // Share App
        findViewById<CardView>(R.id.share_app).setOnClickListener {
            shareApp()
        }

        // Terms of Service
        findViewById<CardView>(R.id.terms).setOnClickListener {
            showDialog("Terms of Service", "This is a demo app. Terms and conditions apply.")
        }

        // Privacy Policy
        findViewById<CardView>(R.id.privacy).setOnClickListener {
            showDialog("Privacy Policy", "We respect your privacy. No data is collected.")
        }
    }

    private fun showFormatDialog() {
        val formats = arrayOf("MP3", "WAV", "AAC", "FLAC")
        AlertDialog.Builder(this)
            .setTitle("Select Record Format")
            .setItems(formats) { _, which ->
                val formatTv = findViewById<TextView>(R.id.format_value)
                formatTv.text = formats[which]
                Toast.makeText(this, "Format set to ${formats[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showRecordTypeDialog() {
        val types = arrayOf("Internal Audio", "Microphone", "Both")
        AlertDialog.Builder(this)
            .setTitle("Select Record Type")
            .setItems(types) { _, which ->
                val typeTv = findViewById<TextView>(R.id.type_value)
                typeTv.text = types[which]
                Toast.makeText(this, "Type set to ${types[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun showPathDialog() {
        val editText = EditText(this)
        editText.setText("/storage/emulated/0/Music/DJAudioTools")
        AlertDialog.Builder(this)
            .setTitle("Record Path")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val pathTv = findViewById<TextView>(R.id.path_value)
                pathTv.text = editText.text.toString()
                Toast.makeText(this, "Path saved", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@djaudiotools.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Feedback for DJ Audio Tools")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPlayStore() {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
        } catch (e: Exception) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out DJ Audio Tools app!")
        }
        startActivity(Intent.createChooser(shareIntent, "Share app via"))
    }

    private fun showDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }
}