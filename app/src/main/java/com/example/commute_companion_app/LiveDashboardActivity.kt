package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.commute_companion_app.api.CommuteRepository
import kotlinx.coroutines.launch

class LiveDashboardActivity : AppCompatActivity() {

    private lateinit var repository: CommuteRepository

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_dashboard)

        repository = CommuteRepository(
            AppPreferences(this)
        )

        findViewById<LinearLayout>(
            R.id.locationSelector
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SetLocationActivity::class.java
                )
            )
        }

        loadDashboard()
    }

    private fun loadDashboard() {
        val prefs = AppPreferences(this)

        lifecycleScope.launch {

            Log.d(
                "CommuteCompanionAPI",
                "Loading live dashboard..."
            )

            val locationsResult =
                repository.getSavedLocations()

            locationsResult.onFailure { exception ->

                Log.e(
                    "CommuteCompanionAPI",
                    "Failed to load saved locations",
                    exception
                )

                Toast.makeText(
                    this@LiveDashboardActivity,
                    "Unable to load your saved location.",
                    Toast.LENGTH_LONG
                ).show()
            }

            locationsResult.onSuccess { locations ->

                if (locations.isEmpty()) {

                    Log.d(
                        "CommuteCompanionAPI",
                        "No saved locations were found."
                    )

                    Toast.makeText(
                        this@LiveDashboardActivity,
                        "Please save a location first.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@onSuccess
                }

                val preferredLocation =
                    locations.firstOrNull {
                        it.label == prefs.savedLocationLabel
                    } ?: locations.first()

                Log.d(
                    "CommuteCompanionAPI",
                    "Selected dashboard location — " +
                            "id='${preferredLocation.id}', " +
                            "label='${preferredLocation.label}', " +
                            "address='${preferredLocation.address}'"
                )

                loadDashboardData(
                    preferredLocation.id
                )
            }
        }
    }

    private fun loadDashboardData(
        locationId: Int
    ) {
        lifecycleScope.launch {

            Log.d(
                "CommuteCompanionAPI",
                "Requesting dashboard data — " +
                        "locationId='$locationId'"
            )

            val result =
                repository.getDashboard(locationId)

            result.onSuccess { dashboard ->

                Log.d(
                    "CommuteCompanionAPI",
                    "Dashboard loaded successfully — " +
                            "location='${dashboard.locationLabel}', " +
                            "traffic='${dashboard.traffic.status}', " +
                            "commuteMinutes=" +
                            "${dashboard.traffic.commuteMinutes}, " +
                            "loadSheddingStage=" +
                            "${dashboard.loadShedding.stage}, " +
                            "temperature=" +
                            "${dashboard.weather.temperatureCelsius}"
                )

                updateDashboardUi(dashboard)
            }

            result.onFailure { exception ->

                Log.e(
                    "CommuteCompanionAPI",
                    "Failed to load dashboard data",
                    exception
                )

                Toast.makeText(
                    this@LiveDashboardActivity,
                    "Unable to load live dashboard data.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateDashboardUi(
        dashboard: com.example.commute_companion_app.api.DashboardResponse
    ) {

        findViewById<TextView>(
            R.id.tvTrafficStatus
        ).text = dashboard.traffic.status

        findViewById<TextView>(
            R.id.tvCommuteMinutes
        ).text =
            dashboard.traffic.commuteMinutes.toString()

        findViewById<TextView>(
            R.id.tvTrafficDelay
        ).text =
            "↑ ${dashboard.traffic.delayMinutes} delay"

        findViewById<TextView>(
            R.id.tvTrafficIncidents
        ).text =
            "${dashboard.traffic.incidentCount} incidents"

        findViewById<TextView>(
            R.id.tvLoadSheddingStage
        ).text =
            "Stage ${dashboard.loadShedding.stage}"

        findViewById<TextView>(
            R.id.tvPowerStatus
        ).text =
            if (dashboard.loadShedding.powerAvailable) {
                "Power available"
            } else {
                "Power unavailable"
            }

        findViewById<TextView>(
            R.id.tvLoadSheddingChange
        ).text =
            "Change in ${dashboard.loadShedding.changeIn}"

        findViewById<TextView>(
            R.id.tvNextLoadSheddingSlot
        ).text =
            dashboard.loadShedding.nextSlot

        findViewById<TextView>(
            R.id.tvWeather
        ).text =
            "${dashboard.weather.temperatureCelsius.toInt()}°C " +
                    dashboard.weather.condition

        findViewById<TextView>(
            R.id.tvWeatherAlert
        ).text =
            if (dashboard.weather.wetRoads) {
                "Wet roads"
            } else {
                dashboard.weather.alertMessage
            }

        Log.d(
            "CommuteCompanion",
            "Live dashboard UI updated successfully."
        )
    }
}