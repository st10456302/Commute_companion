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
import com.example.commute_companion_app.api.DashboardResponse
import kotlinx.coroutines.launch

class LiveDashboardActivity : AppCompatActivity() {

    private lateinit var repository: CommuteRepository

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LanguageManager.applyLanguage(newBase)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_live_dashboard)

        repository = CommuteRepository(
            AppPreferences(this)
        )

        // Open saved-location management.
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
    }

    override fun onResume() {
        super.onResume()

        if (::repository.isInitialized) {
            loadDashboard()
        }
    }

    private fun loadDashboard() {

        val prefs = AppPreferences(this)

        lifecycleScope.launch {

            Log.d(
                "CommuteCompanionAPI",
                "Loading saved locations for Live Dashboard..."
            )

            val locationsResult =
                repository.getSavedLocations()

            locationsResult.onSuccess { locations ->

                if (locations.isEmpty()) {

                    Log.d(
                        "CommuteCompanionAPI",
                        "No saved locations found."
                    )

                    Toast.makeText(
                        this@LiveDashboardActivity,
                        "Please save a location first.",
                        Toast.LENGTH_LONG
                    ).show()

                    return@onSuccess
                }

                // Try to use the location selected during setup.
                // If it cannot be found, use the first saved location.
                val selectedLocation =
                    locations.firstOrNull {
                        it.label == prefs.savedLocationLabel
                    } ?: locations.first()

                Log.d(
                    "CommuteCompanionAPI",
                    "Dashboard location selected — " +
                            "id='${selectedLocation.id}', " +
                            "label='${selectedLocation.label}', " +
                            "address='${selectedLocation.address}'"
                )

                updateLocationDisplay(
                    selectedLocation.label,
                    selectedLocation.address
                )

                loadDashboardData(
                    selectedLocation.id
                )
            }

            locationsResult.onFailure { exception ->

                Log.e(
                    "CommuteCompanionAPI",
                    "Failed to load saved locations.",
                    exception
                )

                Toast.makeText(
                    this@LiveDashboardActivity,
                    "Unable to load your saved location.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateLocationDisplay(
        label: String,
        address: String
    ) {

        findViewById<TextView>(
            R.id.tvDashboardLocation
        ).text = label

        findViewById<TextView>(
            R.id.tvDashboardAddress
        ).text = address

        Log.d(
            "CommuteCompanionAPI",
            "Dashboard location UI updated — " +
                    "label='$label', " +
                    "address='$address'"
        )
    }

    private fun loadDashboardData(
        locationId: Int
    ) {

        lifecycleScope.launch {

            Log.d(
                "CommuteCompanionAPI",
                "Requesting dashboard API — " +
                        "locationId='$locationId'"
            )

            val result =
                repository.getDashboard(locationId)

            result.onSuccess { dashboard ->

                Log.d(
                    "CommuteCompanionAPI",
                    "Dashboard API request successful — " +
                            "location='${dashboard.locationLabel}', " +
                            "traffic='${dashboard.traffic.status}', " +
                            "commuteMinutes=" +
                            "${dashboard.traffic.commuteMinutes}, " +
                            "delayMinutes=" +
                            "${dashboard.traffic.delayMinutes}, " +
                            "incidents=" +
                            "${dashboard.traffic.incidentCount}, " +
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
                    "Dashboard API request failed.",
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
        dashboard: DashboardResponse
    ) {

        // -------------------------
        // Traffic
        // -------------------------

        findViewById<TextView>(
            R.id.tvTrafficStatus
        ).text =
            dashboard.traffic.status

        findViewById<TextView>(
            R.id.tvCommuteMinutes
        ).text =
            dashboard.traffic.commuteMinutes.toString()

        findViewById<TextView>(
            R.id.tvTrafficDelay
        ).text =
            if (dashboard.traffic.delayMinutes > 0) {
                "↑ ${dashboard.traffic.delayMinutes} min delay"
            } else {
                "No delay"
            }

        findViewById<TextView>(
            R.id.tvTrafficIncidents
        ).text =
            if (dashboard.traffic.incidentCount == 1) {
                "1 incident"
            } else {
                "${dashboard.traffic.incidentCount} incidents"
            }


        // -------------------------
        // Load shedding
        // -------------------------

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
            dashboard.loadShedding.changeIn

        findViewById<TextView>(
            R.id.tvNextLoadSheddingSlot
        ).text =
            dashboard.loadShedding.nextSlot


        // -------------------------
        // Weather
        // -------------------------

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
            "Live Dashboard UI updated successfully."
        )
    }
}