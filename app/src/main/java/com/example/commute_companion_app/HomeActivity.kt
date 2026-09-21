package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        prefs = AppPreferences(this)

        findViewById<LinearLayout>(
            R.id.btnLiveDashboard
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    LiveDashboardActivity::class.java
                )
            )
        }

        findViewById<android.widget.ImageView>(
            R.id.btnHeaderNotifications
        ).setOnClickListener {

            openAlerts()
        }

        findViewById<LinearLayout>(
            R.id.navHome
        ).setOnClickListener {
            // Already on Home
        }

        findViewById<LinearLayout>(
            R.id.navAlerts
        ).setOnClickListener {

            openAlerts()
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
                    SettingsActivity::class.java
                )
            )
        }
    }

    override fun onResume() {
        super.onResume()
        loadDashboardData()
    }

    private fun loadDashboardData() {

        val name =
            prefs.userName.trim()

        findViewById<TextView>(
            R.id.tvGreeting
        ).text =
            if (name.isNotEmpty()) {
                "Good Morning, $name."
            } else {
                getString(R.string.good_morning)
            }

        val location =
            prefs.savedLocationAddress.trim()
                .ifEmpty {
                    getString(
                        R.string.sandton_central
                    )
                }

        findViewById<TextView>(
            R.id.tvLocationSubtitle
        ).text =
            "$location • 18°C"

        findViewById<TextView>(
            R.id.tvWeatherLocationTag
        ).text =
            location

        val destination =
            prefs.savedRouteDestination.trim()

        findViewById<TextView>(
            R.id.tvTrafficHeading
        ).text =
            if (destination.isNotEmpty()) {
                "Traffic to $destination"
            } else {
                "No commute route saved"
            }

        findViewById<TextView>(
            R.id.tvRouteStatus
        ).text =
            if (destination.isNotEmpty()) {
                "View your current commute information and alerts."
            } else {
                "Save a route to personalise your commute dashboard."
            }
    }

    private fun openAlerts() {

        startActivity(
            Intent(
                this,
                AlertsActivity::class.java
            )
        )
    }
}