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

class HomeActivity : AppCompatActivity() {

    private lateinit var repository: CommuteRepository

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LanguageManager.applyLanguage(newBase)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        repository =
            CommuteRepository(
                AppPreferences(this)
            )

        setupNavigation()

        BottomNavigationHelper.setSelectedTab(
            findViewById(android.R.id.content),
            BottomNavigationHelper.Tab.HOME
        )
    }

    override fun onResume() {
        super.onResume()

        if (::repository.isInitialized) {
            BottomNavigationHelper.setSelectedTab(
                findViewById(android.R.id.content),
                BottomNavigationHelper.Tab.HOME
            )

            loadHomeData()
        }
    }

    private fun loadHomeData() {

        val prefs =
            AppPreferences(this)

        val tvGreeting =
            findViewById<TextView>(
                R.id.tvGreeting
            )

        val userName =
            prefs.userName.trim()

        tvGreeting.text =
            if (userName.isNotEmpty()) {
                "Good Morning, $userName."
            } else {
                getString(
                    R.string.good_morning
                )
            }

        val locationDisplay =
            prefs.savedLocationAddress
                .trim()
                .ifEmpty {
                    getString(
                        R.string.sandton_central
                    )
                }

        findViewById<TextView>(
            R.id.tvLocationSubtitle
        ).text =
            "$locationDisplay • 18°C"

        findViewById<TextView>(
            R.id.tvWeatherLocationTag
        ).text =
            locationDisplay

        val routeDestination =
            prefs.savedRouteDestination.trim()

        findViewById<TextView>(
            R.id.tvTrafficHeading
        ).text =
            if (routeDestination.isNotEmpty()) {
                "Traffic to $routeDestination"
            } else {
                getString(
                    R.string.traffic_to_sandton
                )
            }

        Log.d(
            "CommuteCompanion",
            "Home Dashboard loaded — " +
                    "userName='${prefs.userName}', " +
                    "savedLocationAddress='${prefs.savedLocationAddress}', " +
                    "savedLocationLabel='${prefs.savedLocationLabel}', " +
                    "savedRouteName='${prefs.savedRouteName}', " +
                    "savedRouteDestination='${prefs.savedRouteDestination}'"
        )

        loadCommuteScore()
    }

    private fun loadCommuteScore() {

        val prefs =
            AppPreferences(this)

        lifecycleScope.launch {

            Log.d(
                "CommuteCompanionAPI",
                "Loading saved locations for Commute Score..."
            )

            val locationsResult =
                repository.getSavedLocations()

            locationsResult.onSuccess { locations ->

                if (locations.isEmpty()) {

                    Log.d(
                        "CommuteCompanionAPI",
                        "No saved locations found for Commute Score."
                    )

                    showCommuteScoreUnavailable()

                    return@onSuccess
                }

                val selectedLocation =
                    locations.firstOrNull {
                        it.label ==
                                prefs.savedLocationLabel
                    } ?: locations.first()

                Log.d(
                    "CommuteCompanionAPI",
                    "Commute Score location selected — " +
                            "id='${selectedLocation.id}', " +
                            "label='${selectedLocation.label}'"
                )

                loadCommuteScoreDashboard(
                    selectedLocation.id
                )
            }

            locationsResult.onFailure { exception ->

                Log.e(
                    "CommuteCompanionAPI",
                    "Failed to load saved locations for Commute Score.",
                    exception
                )

                showCommuteScoreUnavailable()
            }
        }
    }

    private fun loadCommuteScoreDashboard(
        locationId: Int
    ) {

        lifecycleScope.launch {

            Log.d(
                "CommuteCompanionAPI",
                "Requesting dashboard data for Commute Score — " +
                        "locationId='$locationId'"
            )

            val result =
                repository.getDashboard(
                    locationId
                )

            result.onSuccess { dashboard ->

                val score =
                    CommuteScoreCalculator.calculate(
                        dashboard
                    )

                updateCommuteScore(score)

                Log.d(
                    "CommuteCompanionAPI",
                    "Commute Score calculated — " +
                            "score=$score, " +
                            "traffic='${dashboard.traffic.status}', " +
                            "delay=${dashboard.traffic.delayMinutes}, " +
                            "incidents=${dashboard.traffic.incidentCount}, " +
                            "loadSheddingStage=${dashboard.loadShedding.stage}, " +
                            "wetRoads=${dashboard.weather.wetRoads}"
                )
            }

            result.onFailure { exception ->

                Log.e(
                    "CommuteCompanionAPI",
                    "Failed to load dashboard data for Commute Score.",
                    exception
                )

                showCommuteScoreUnavailable()
            }
        }
    }

    private fun updateCommuteScore(
        score: Int
    ) {

        findViewById<TextView>(
            R.id.tvCommuteScore
        ).text =
            "$score/100"

        findViewById<TextView>(
            R.id.tvCommuteScoreStatus
        ).text =
            when {
                score >= 80 ->
                    getString(
                        R.string.commute_score_excellent
                    )

                score >= 60 ->
                    getString(
                        R.string.commute_score_good
                    )

                score >= 40 ->
                    getString(
                        R.string.commute_score_moderate
                    )

                else ->
                    getString(
                        R.string.commute_score_difficult
                    )
            }

        Log.d(
            "CommuteCompanion",
            "Commute Score UI updated — score=$score"
        )
    }

    private fun showCommuteScoreUnavailable() {

        findViewById<TextView>(
            R.id.tvCommuteScore
        ).text =
            "--/100"

        findViewById<TextView>(
            R.id.tvCommuteScoreStatus
        ).text =
            getString(
                R.string.commute_score_loading
            )

        Log.d(
            "CommuteCompanion",
            "Commute Score unavailable."
        )
    }

    private fun setupNavigation() {

        findViewById<LinearLayout>(
            R.id.cardTraffic
        ).setOnClickListener {

            Log.d(
                "CommuteCompanion",
                "Opening Live Dashboard from Home."
            )

            startActivity(
                Intent(
                    this,
                    LiveDashboardActivity::class.java
                )
            )
        }

        findViewById<LinearLayout>(
            R.id.navHome
        ).setOnClickListener {

            // Already on Home.
        }

        findViewById<LinearLayout>(
            R.id.navAlerts
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    AlertsActivity::class.java
                )
            )
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
}