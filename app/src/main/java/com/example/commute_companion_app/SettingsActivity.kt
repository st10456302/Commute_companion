package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = AppPreferences(this)

        val switchNotifications =
            findViewById<Switch>(R.id.switchNotifications)

        val switchBiometric =
            findViewById<Switch>(R.id.switchBiometric)

        val tvLanguage =
            findViewById<TextView>(R.id.tvSelectedLanguage)

        // Load the user's saved preferences.
        switchNotifications.isChecked = prefs.notificationsEnabled
        switchBiometric.isChecked = prefs.biometricEnabled
        tvLanguage.text = prefs.selectedLanguage

        // Notification preference.
        switchNotifications.setOnCheckedChangeListener { _, enabled ->

            prefs.notificationsEnabled = enabled

            Log.d(
                "CommuteCompanion",
                "Notifications setting changed: $enabled"
            )

            Toast.makeText(
                this,
                if (enabled) {
                    getString(R.string.notifications_enabled_message)
                } else {
                    getString(R.string.notifications_disabled_message)
                },
                Toast.LENGTH_SHORT
            ).show()
        }

        // Biometric preference.
        switchBiometric.setOnCheckedChangeListener { _, enabled ->

            prefs.biometricEnabled = enabled

            Log.d(
                "CommuteCompanion",
                "Biometric setting changed: $enabled"
            )
        }

        // Language selection.
        findViewById<Button>(R.id.btnLanguage).setOnClickListener {

            val intent = Intent(
                this,
                LanguageSelectionActivity::class.java
            )

            intent.putExtra("opened_from_settings", true)

            startActivity(intent)
        }

        // Back button.
        findViewById<Button>(R.id.btnSettingsBack).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()

        // Refresh language text when returning from
        // LanguageSelectionActivity.
        val tvLanguage =
            findViewById<TextView>(R.id.tvSelectedLanguage)

        tvLanguage?.text = AppPreferences(this).selectedLanguage
    }
}