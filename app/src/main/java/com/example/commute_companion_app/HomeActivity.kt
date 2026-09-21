package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LanguageManager.applyLanguage(newBase)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_home)

        setupNavigation()

        BottomNavigationHelper.setSelectedTab(
            findViewById(android.R.id.content),
            BottomNavigationHelper.Tab.HOME
        )

        loadHomeContent()
    }

    override fun onResume() {
        super.onResume()

        BottomNavigationHelper.setSelectedTab(
            findViewById(android.R.id.content),
            BottomNavigationHelper.Tab.HOME
        )

        loadHomeContent()
    }

    private fun loadHomeContent() {

        val prefs = AppPreferences(this)

        val userName = prefs.userName.trim()

        val greeting = findViewById<TextView>(
            R.id.tvGreeting
        )

        greeting.text =
            if (userName.isNotEmpty()) {
                getString(
                    R.string.home_welcome_user,
                    userName
                )
            } else {
                getString(
                    R.string.home_welcome
                )
            }

        val location = prefs.savedLocationAddress
            .trim()

        findViewById<TextView>(
            R.id.tvLocationSubtitle
        ).text =
            if (location.isNotEmpty()) {
                location
            } else {
                getString(
                    R.string.home_location_not_set
                )
            }

        val routeName =
            prefs.savedRouteName.trim()

        val routeDestination =
            prefs.savedRouteDestination.trim()

        findViewById<TextView>(
            R.id.tvHomeRoute
        ).text =
            when {
                routeName.isNotEmpty() &&
                        routeDestination.isNotEmpty() ->
                    "$routeName • $routeDestination"

                routeDestination.isNotEmpty() ->
                    routeDestination

                else ->
                    getString(
                        R.string.home_no_route
                    )
            }

        Log.d(
            "CommuteCompanion",
            "Home hub loaded — " +
                    "userName='${prefs.userName}', " +
                    "savedLocationAddress='${prefs.savedLocationAddress}', " +
                    "savedRouteName='${prefs.savedRouteName}', " +
                    "savedRouteDestination='${prefs.savedRouteDestination}'"
        )
    }

    private fun setupNavigation() {

        // Live Dashboard
        findViewById<LinearLayout>(
            R.id.cardLiveDashboard
        ).setOnClickListener {

            Log.d(
                "CommuteCompanion",
                "Opening Live Dashboard from Home hub."
            )

            startActivity(
                Intent(
                    this,
                    LiveDashboardActivity::class.java
                )
            )
        }

        // Alerts
        findViewById<LinearLayout>(
            R.id.cardAlerts
        ).setOnClickListener {

            Log.d(
                "CommuteCompanion",
                "Opening Alerts from Home hub."
            )

            startActivity(
                Intent(
                    this,
                    AlertsActivity::class.java
                )
            )
        }

        // Set Location
        findViewById<LinearLayout>(
            R.id.cardLocation
        ).setOnClickListener {

            Log.d(
                "CommuteCompanion",
                "Opening Set Location from Home hub."
            )

            startActivity(
                Intent(
                    this,
                    SetLocationActivity::class.java
                )
            )
        }

        // Routes
        findViewById<LinearLayout>(
            R.id.cardRoutes
        ).setOnClickListener {

            Log.d(
                "CommuteCompanion",
                "Opening Set Route from Home hub."
            )

            startActivity(
                Intent(
                    this,
                    SetRouteActivity::class.java
                )
            )
        }

        // Profile
        findViewById<LinearLayout>(
            R.id.cardProfile
        ).setOnClickListener {

            Log.d(
                "CommuteCompanion",
                "Opening Profile from Home hub."
            )

            startActivity(
                Intent(
                    this,
                    ProfileActivity::class.java
                )
            )

        }

        // Bottom navigation
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