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
import androidx.lifecycle.lifecycleScope
import com.example.commute_companion_app.api.CommuteRepository
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences

    private lateinit var switchNotifications: Switch
    private lateinit var switchTrafficAlerts: Switch
    private lateinit var switchWeatherAlerts: Switch
    private lateinit var switchLoadSheddingAlerts: Switch
    private lateinit var switchBiometric: Switch

    // Prevents listeners from running while
    // preferences are being loaded from the API.
    private var isLoadingNotificationPreference = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LanguageManager.applyLanguage(newBase)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_settings)

        prefs = AppPreferences(this)

        switchNotifications =
            findViewById(R.id.switchNotifications)

        switchTrafficAlerts =
            findViewById(R.id.switchTrafficAlerts)

        switchWeatherAlerts =
            findViewById(R.id.switchWeatherAlerts)

        switchLoadSheddingAlerts =
            findViewById(R.id.switchLoadSheddingAlerts)

        switchBiometric =
            findViewById(R.id.switchBiometric)

        val tvLanguage =
            findViewById<TextView>(R.id.tvSelectedLanguage)

        // Load local preferences immediately.
        switchNotifications.isChecked =
            prefs.notificationsEnabled

        switchBiometric.isChecked =
            prefs.biometricEnabled

        tvLanguage.text =
            prefs.selectedLanguage

        // The category switches default to enabled locally.
        // They will be replaced with the backend values
        // when loadNotificationPreferences() completes.
        switchTrafficAlerts.isChecked = true
        switchWeatherAlerts.isChecked = true
        switchLoadSheddingAlerts.isChecked = true

        // Load notification preferences from the REST API.
        loadNotificationPreferences()

        // -------------------------------------------------
        // MASTER NOTIFICATIONS SWITCH
        // -------------------------------------------------

        switchNotifications.setOnCheckedChangeListener { _, enabled ->

            if (isLoadingNotificationPreference) {
                return@setOnCheckedChangeListener
            }

            prefs.notificationsEnabled = enabled

            Log.d(
                "CommuteCompanion",
                "Notifications setting changed: $enabled"
            )

            Toast.makeText(
                this,
                if (enabled) {
                    getString(
                        R.string.notifications_enabled_message
                    )
                } else {
                    getString(
                        R.string.notifications_disabled_message
                    )
                },
                Toast.LENGTH_SHORT
            ).show()

            updateNotificationPreferences()
        }

        // -------------------------------------------------
        // TRAFFIC ALERT SWITCH
        // -------------------------------------------------

        switchTrafficAlerts.setOnCheckedChangeListener { _, enabled ->

            if (isLoadingNotificationPreference) {
                return@setOnCheckedChangeListener
            }

            Log.d(
                "CommuteCompanion",
                "Traffic alerts setting changed: $enabled"
            )

            updateNotificationPreferences()
        }

        // -------------------------------------------------
        // WEATHER ALERT SWITCH
        // -------------------------------------------------

        switchWeatherAlerts.setOnCheckedChangeListener { _, enabled ->

            if (isLoadingNotificationPreference) {
                return@setOnCheckedChangeListener
            }

            Log.d(
                "CommuteCompanion",
                "Weather alerts setting changed: $enabled"
            )

            updateNotificationPreferences()
        }

        // -------------------------------------------------
        // LOAD-SHEDDING ALERT SWITCH
        // -------------------------------------------------

        switchLoadSheddingAlerts.setOnCheckedChangeListener { _, enabled ->

            if (isLoadingNotificationPreference) {
                return@setOnCheckedChangeListener
            }

            Log.d(
                "CommuteCompanion",
                "Load-shedding alerts setting changed: $enabled"
            )

            updateNotificationPreferences()
        }

        // -------------------------------------------------
        // BIOMETRIC
        // -------------------------------------------------

        switchBiometric.setOnCheckedChangeListener { _, enabled ->

            prefs.biometricEnabled = enabled

            Log.d(
                "CommuteCompanion",
                "Biometric setting changed: $enabled"
            )
        }

        // -------------------------------------------------
        // LANGUAGE
        // -------------------------------------------------

        findViewById<Button>(R.id.btnLanguage)
            .setOnClickListener {

                val intent = Intent(
                    this,
                    LanguageSelectionActivity::class.java
                )

                intent.putExtra(
                    "opened_from_settings",
                    true
                )

                startActivity(intent)
            }

        // -------------------------------------------------
        // BACK
        // -------------------------------------------------

        findViewById<Button>(R.id.btnSettingsBack)
            .setOnClickListener {
                finish()
            }
    }

    private fun loadNotificationPreferences() {

        lifecycleScope.launch {

            val repository =
                CommuteRepository(
                    AppPreferences(this@SettingsActivity)
                )

            Log.d(
                "CommuteCompanionAPI",
                "Loading notification preferences..."
            )

            val result =
                repository.getNotificationPreferences()

            result.onSuccess { preferences ->

                isLoadingNotificationPreference = true

                switchNotifications.isChecked =
                    preferences.notificationsEnabled

                switchTrafficAlerts.isChecked =
                    preferences.trafficAlerts

                switchWeatherAlerts.isChecked =
                    preferences.weatherAlerts

                switchLoadSheddingAlerts.isChecked =
                    preferences.loadSheddingAlerts

                isLoadingNotificationPreference = false

                // Keep the master setting synchronized locally.
                prefs.notificationsEnabled =
                    preferences.notificationsEnabled

                Log.d(
                    "CommuteCompanionAPI",
                    "Notification preferences loaded — " +
                            "notificationsEnabled=" +
                            "${preferences.notificationsEnabled}, " +
                            "trafficAlerts=" +
                            "${preferences.trafficAlerts}, " +
                            "weatherAlerts=" +
                            "${preferences.weatherAlerts}, " +
                            "loadSheddingAlerts=" +
                            "${preferences.loadSheddingAlerts}"
                )
            }

            result.onFailure { exception ->

                Log.d(
                    "CommuteCompanionAPI",
                    "Notification preferences were not found. " +
                            "Creating default preferences..."
                )

                createDefaultNotificationPreferences()
            }
        }
    }

    private fun createDefaultNotificationPreferences() {

        lifecycleScope.launch {

            val repository =
                CommuteRepository(
                    AppPreferences(this@SettingsActivity)
                )

            val result =
                repository.createNotificationPreferences(
                    notificationsEnabled =
                        prefs.notificationsEnabled,
                    trafficAlerts = true,
                    weatherAlerts = true,
                    loadSheddingAlerts = true
                )

            result.onSuccess { preferences ->

                isLoadingNotificationPreference = true

                switchNotifications.isChecked =
                    preferences.notificationsEnabled

                switchTrafficAlerts.isChecked =
                    preferences.trafficAlerts

                switchWeatherAlerts.isChecked =
                    preferences.weatherAlerts

                switchLoadSheddingAlerts.isChecked =
                    preferences.loadSheddingAlerts

                isLoadingNotificationPreference = false

                Log.d(
                    "CommuteCompanionAPI",
                    "Default notification preferences created — " +
                            "databaseId='${preferences.id}'"
                )
            }

            result.onFailure { exception ->

                Log.e(
                    "CommuteCompanionAPI",
                    "Failed to create notification preferences",
                    exception
                )
            }
        }
    }

    private fun updateNotificationPreferences() {

        lifecycleScope.launch {

            val repository =
                CommuteRepository(
                    AppPreferences(this@SettingsActivity)
                )

            val notificationsEnabled =
                switchNotifications.isChecked

            val trafficAlerts =
                switchTrafficAlerts.isChecked

            val weatherAlerts =
                switchWeatherAlerts.isChecked

            val loadSheddingAlerts =
                switchLoadSheddingAlerts.isChecked

            Log.d(
                "CommuteCompanionAPI",
                "Updating notification preferences — " +
                        "notificationsEnabled=$notificationsEnabled, " +
                        "trafficAlerts=$trafficAlerts, " +
                        "weatherAlerts=$weatherAlerts, " +
                        "loadSheddingAlerts=$loadSheddingAlerts"
            )

            val result =
                repository.updateNotificationPreferences(
                    notificationsEnabled =
                        notificationsEnabled,
                    trafficAlerts =
                        trafficAlerts,
                    weatherAlerts =
                        weatherAlerts,
                    loadSheddingAlerts =
                        loadSheddingAlerts
                )

            result.onSuccess { preferences ->

                Log.d(
                    "CommuteCompanionAPI",
                    "Notification preferences updated successfully — " +
                            "databaseId='${preferences.id}', " +
                            "notificationsEnabled=" +
                            "${preferences.notificationsEnabled}, " +
                            "trafficAlerts=" +
                            "${preferences.trafficAlerts}, " +
                            "weatherAlerts=" +
                            "${preferences.weatherAlerts}, " +
                            "loadSheddingAlerts=" +
                            "${preferences.loadSheddingAlerts}"
                )
            }

            result.onFailure { exception ->

                Log.e(
                    "CommuteCompanionAPI",
                    "Failed to update notification preferences",
                    exception
                )

                Toast.makeText(
                    this@SettingsActivity,
                    "Unable to sync notification settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        val tvLanguage =
            findViewById<TextView>(R.id.tvSelectedLanguage)

        tvLanguage.text =
            AppPreferences(this).selectedLanguage
    }
}