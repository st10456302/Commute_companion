package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.commute_companion_app.api.CommuteRepository
import com.example.commute_companion_app.api.DashboardResponse
import kotlinx.coroutines.launch

class AlertsActivity : AppCompatActivity() {

    private lateinit var repository: CommuteRepository

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LanguageManager.applyLanguage(newBase)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_alerts)

        repository = CommuteRepository(
            AppPreferences(this)
        )

        setupBottomNavigation()

        BottomNavigationHelper.setSelectedTab(
            findViewById(android.R.id.content),
            BottomNavigationHelper.Tab.ALERTS
        )
    }

    override fun onResume() {
        super.onResume()

        if (::repository.isInitialized) {

            BottomNavigationHelper.setSelectedTab(
                findViewById(android.R.id.content),
                BottomNavigationHelper.Tab.ALERTS
            )

            loadAlerts()
        }
    }

    private fun setupBottomNavigation() {

        findViewById<LinearLayout>(
            R.id.navHome
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    HomeActivity::class.java
                ).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            )
        }

        findViewById<LinearLayout>(
            R.id.navAlerts
        ).setOnClickListener {

            // Already on Alerts.
        }

        findViewById<LinearLayout>(
            R.id.navLocations
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    SetLocationActivity::class.java
                )
            )
        }

        findViewById<LinearLayout>(
            R.id.navProfile
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    ProfileActivity::class.java
                )
            )
        }
    }

    private fun loadAlerts() {

        val prefs = AppPreferences(this)

        lifecycleScope.launch {

            Log.d(
                "CommuteCompanionAPI",
                "Loading notification preferences for Alerts..."
            )

            val preferencesResult =
                repository.getNotificationPreferences()

            preferencesResult.onSuccess { preferences ->

                Log.d(
                    "CommuteCompanionAPI",
                    "Alert preferences loaded — " +
                            "notificationsEnabled=" +
                            "${preferences.notificationsEnabled}, " +
                            "trafficAlerts=" +
                            "${preferences.trafficAlerts}, " +
                            "weatherAlerts=" +
                            "${preferences.weatherAlerts}, " +
                            "loadSheddingAlerts=" +
                            "${preferences.loadSheddingAlerts}"
                )

                if (!preferences.notificationsEnabled) {

                    Log.d(
                        "CommuteCompanionAPI",
                        "Notifications are disabled. " +
                                "Hiding all alert categories."
                    )

                    hideAllAlerts()

                    return@onSuccess
                }

                loadSavedLocation(
                    prefs,
                    preferences.trafficAlerts,
                    preferences.weatherAlerts,
                    preferences.loadSheddingAlerts
                )
            }

            preferencesResult.onFailure { exception ->

                Log.e(
                    "CommuteCompanionAPI",
                    "Failed to load notification preferences.",
                    exception
                )

                Toast.makeText(
                    this@AlertsActivity,
                    "Unable to load notification settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadSavedLocation(
        prefs: AppPreferences,
        trafficAlertsEnabled: Boolean,
        weatherAlertsEnabled: Boolean,
        loadSheddingAlertsEnabled: Boolean
    ) {

        lifecycleScope.launch {

            Log.d(
                "CommuteCompanionAPI",
                "Loading saved locations for Alerts..."
            )

            val locationsResult =
                repository.getSavedLocations()

            locationsResult.onSuccess { locations ->

                if (locations.isEmpty()) {

                    Log.d(
                        "CommuteCompanionAPI",
                        "No saved locations found for Alerts."
                    )

                    Toast.makeText(
                        this@AlertsActivity,
                        "Please save a location first.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@onSuccess
                }

                val selectedLocation =
                    locations.firstOrNull {
                        it.label == prefs.savedLocationLabel
                    } ?: locations.first()

                Log.d(
                    "CommuteCompanionAPI",
                    "Alerts location selected — " +
                            "id='${selectedLocation.id}', " +
                            "label='${selectedLocation.label}'"
                )

                loadDashboardAlerts(
                    selectedLocation.id,
                    trafficAlertsEnabled,
                    weatherAlertsEnabled,
                    loadSheddingAlertsEnabled
                )
            }

            locationsResult.onFailure { exception ->

                Log.e(
                    "CommuteCompanionAPI",
                    "Failed to load saved locations for Alerts.",
                    exception
                )

                Toast.makeText(
                    this@AlertsActivity,
                    "Unable to load your saved location.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadDashboardAlerts(
        locationId: Int,
        trafficAlertsEnabled: Boolean,
        weatherAlertsEnabled: Boolean,
        loadSheddingAlertsEnabled: Boolean
    ) {

        lifecycleScope.launch {

            Log.d(
                "CommuteCompanionAPI",
                "Requesting dashboard data for Alerts — " +
                        "locationId='$locationId'"
            )

            val result =
                repository.getDashboard(locationId)

            result.onSuccess { dashboard ->

                Log.d(
                    "CommuteCompanionAPI",
                    "Dashboard data loaded for Alerts — " +
                            "traffic='${dashboard.traffic.status}', " +
                            "incidents=${dashboard.traffic.incidentCount}, " +
                            "loadSheddingStage=" +
                            "${dashboard.loadShedding.stage}, " +
                            "wetRoads=${dashboard.weather.wetRoads}"
                )

                updateAlertsUi(
                    dashboard,
                    trafficAlertsEnabled,
                    weatherAlertsEnabled,
                    loadSheddingAlertsEnabled
                )
            }

            result.onFailure { exception ->

                Log.e(
                    "CommuteCompanionAPI",
                    "Failed to load dashboard data for Alerts.",
                    exception
                )

                Toast.makeText(
                    this@AlertsActivity,
                    "Unable to load live alerts.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateAlertsUi(
        dashboard: DashboardResponse,
        trafficAlertsEnabled: Boolean,
        weatherAlertsEnabled: Boolean,
        loadSheddingAlertsEnabled: Boolean
    ) {

        val trafficCard =
            findViewById<LinearLayout>(
                R.id.cardTrafficAlert
            )

        val loadSheddingCard =
            findViewById<LinearLayout>(
                R.id.cardLoadSheddingAlert
            )

        val weatherCard =
            findViewById<LinearLayout>(
                R.id.cardWeatherAlert
            )

        if (trafficAlertsEnabled) {

            trafficCard.visibility = View.VISIBLE

            findViewById<TextView>(
                R.id.tvTrafficAlertStatus
            ).text =
                dashboard.traffic.status

            val trafficMessage =
                when {
                    dashboard.traffic.incidentCount > 0 &&
                            dashboard.traffic.delayMinutes > 0 -> {
                        "${dashboard.traffic.incidentCount} traffic " +
                                "incident(s) detected. " +
                                "${dashboard.traffic.delayMinutes} min delay."
                    }

                    dashboard.traffic.incidentCount > 0 -> {
                        "${dashboard.traffic.incidentCount} traffic " +
                                "incident(s) detected near your route."
                    }

                    dashboard.traffic.delayMinutes > 0 -> {
                        "${dashboard.traffic.delayMinutes} min traffic delay " +
                                "reported on your route."
                    }

                    else -> {
                        "Traffic is currently ${dashboard.traffic.status.lowercase()} " +
                                "with no reported incidents."
                    }
                }

            findViewById<TextView>(
                R.id.tvTrafficAlertMessage
            ).text = trafficMessage

        } else {

            trafficCard.visibility = View.GONE

            Log.d(
                "CommuteCompanionAPI",
                "Traffic Alerts disabled — hiding traffic card."
            )
        }

        if (loadSheddingAlertsEnabled) {

            loadSheddingCard.visibility = View.VISIBLE

            findViewById<TextView>(
                R.id.tvLoadSheddingAlertStatus
            ).text =
                "Stage ${dashboard.loadShedding.stage}"

            val loadSheddingMessage =
                if (dashboard.loadShedding.stage > 0) {

                    "${dashboard.loadShedding.changeIn}. " +
                            dashboard.loadShedding.nextSlot

                } else {

                    if (
                        dashboard.loadShedding.nextSlot
                            .equals(
                                "No upcoming load-shedding event",
                                ignoreCase = true
                            )
                    ) {
                        "Power is currently available. " +
                                "No upcoming load-shedding event."
                    } else {
                        dashboard.loadShedding.nextSlot
                    }
                }

            findViewById<TextView>(
                R.id.tvLoadSheddingAlertMessage
            ).text = loadSheddingMessage

        } else {

            loadSheddingCard.visibility = View.GONE

            Log.d(
                "CommuteCompanionAPI",
                "Load-shedding Alerts disabled — " +
                        "hiding load-shedding card."
            )
        }

        if (weatherAlertsEnabled) {

            weatherCard.visibility = View.VISIBLE

            findViewById<TextView>(
                R.id.tvWeatherAlertStatus
            ).text =
                if (dashboard.weather.wetRoads) {
                    "Alert"
                } else {
                    "Info"
                }

            val weatherMessage =
                if (dashboard.weather.wetRoads) {

                    "Wet road conditions reported. " +
                            "Current temperature: " +
                            "${dashboard.weather.temperatureCelsius.toInt()}°C."

                } else {

                    "Current temperature: " +
                            "${dashboard.weather.temperatureCelsius.toInt()}°C. " +
                            dashboard.weather.condition
                }

            findViewById<TextView>(
                R.id.tvWeatherAlertMessage
            ).text = weatherMessage

        } else {

            weatherCard.visibility = View.GONE

            Log.d(
                "CommuteCompanionAPI",
                "Weather Alerts disabled — hiding weather card."
            )
        }

        var alertCount = 0

        if (
            trafficAlertsEnabled &&
            (
                    dashboard.traffic.incidentCount > 0 ||
                            dashboard.traffic.delayMinutes > 0
                    )
        ) {
            alertCount++
        }

        if (
            loadSheddingAlertsEnabled &&
            dashboard.loadShedding.stage > 0
        ) {
            alertCount++
        }

        if (
            weatherAlertsEnabled &&
            dashboard.weather.wetRoads
        ) {
            alertCount++
        }

        findViewById<TextView>(
            R.id.tvAlertsCount
        ).text =
            alertCount.toString()

        Log.d(
            "CommuteCompanionAPI",
            "Alerts UI updated successfully — " +
                    "alertCount=$alertCount"
        )
    }

    private fun hideAllAlerts() {

        findViewById<LinearLayout>(
            R.id.cardTrafficAlert
        ).visibility = View.GONE

        findViewById<LinearLayout>(
            R.id.cardLoadSheddingAlert
        ).visibility = View.GONE

        findViewById<LinearLayout>(
            R.id.cardWeatherAlert
        ).visibility = View.GONE

        findViewById<TextView>(
            R.id.tvAlertsCount
        ).text = "0"

        Log.d(
            "CommuteCompanionAPI",
            "All alert cards hidden and alert count reset to 0."
        )
    }
}