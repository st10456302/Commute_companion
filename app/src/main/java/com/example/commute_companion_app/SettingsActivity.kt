
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
    private lateinit var switchBiometric: Switch

    // Prevents the notification listener from running while
    // the switch is being updated from API data.
    private var isLoadingNotificationPreference = false

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = AppPreferences(this)

        switchNotifications =
            findViewById(R.id.switchNotifications)

        switchBiometric =
            findViewById(R.id.switchBiometric)

        val tvLanguage =
            findViewById<TextView>(R.id.tvSelectedLanguage)

        // Load the existing local preferences immediately
        // so the screen remains responsive.
        switchNotifications.isChecked =
            prefs.notificationsEnabled

        switchBiometric.isChecked =
            prefs.biometricEnabled

        tvLanguage.text =
            prefs.selectedLanguage

        // Load notification preferences from the REST API.
        loadNotificationPreferences()

        // Notification preference.
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

            updateNotificationPreference(enabled)
        }

        // Biometric preference remains local for now.
        switchBiometric.setOnCheckedChangeListener { _, enabled ->

            prefs.biometricEnabled = enabled

            Log.d(
                "CommuteCompanion",
                "Biometric setting changed: $enabled"
            )
        }

        // Language selection.
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

        // Back button.
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

                isLoadingNotificationPreference = false

                // Keep the local preference synchronized
                // with the backend.
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

    private fun updateNotificationPreference(
        enabled: Boolean
    ) {

        lifecycleScope.launch {

            val repository =
                CommuteRepository(
                    AppPreferences(this@SettingsActivity)
                )

            Log.d(
                "CommuteCompanionAPI",
                "Updating notification preference — " +
                        "notificationsEnabled=$enabled"
            )

            // The current backend stores the three alert
            // categories separately. Until their individual
            // UI controls are added, keep them enabled.
            val result =
                repository.updateNotificationPreferences(
                    notificationsEnabled = enabled,
                    trafficAlerts = true,
                    weatherAlerts = true,
                    loadSheddingAlerts = true
                )

            result.onSuccess { preferences ->

                Log.d(
                    "CommuteCompanionAPI",
                    "Notification preferences updated successfully — " +
                            "databaseId='${preferences.id}', " +
                            "notificationsEnabled=" +
                            "${preferences.notificationsEnabled}"
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

        // Refresh language text when returning from
        // LanguageSelectionActivity.
        val tvLanguage =
            findViewById<TextView>(R.id.tvSelectedLanguage)

        tvLanguage?.text =
            AppPreferences(this).selectedLanguage
    }
}
