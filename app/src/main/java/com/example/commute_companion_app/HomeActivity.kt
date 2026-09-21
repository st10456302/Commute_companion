package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val prefs = AppPreferences(this)

        val tvGreeting =
            findViewById<TextView>(R.id.tvGreeting)

        val userName = prefs.userName.trim()

        tvGreeting.text =
            if (userName.isNotEmpty()) {
                "Good Morning, $userName."
            } else {
                getString(R.string.good_morning)
            }

        val locationDisplay =
            prefs.savedLocationAddress.trim().ifEmpty {
                getString(R.string.sandton_central)
            }

        findViewById<TextView>(
            R.id.tvLocationSubtitle
        ).text = "$locationDisplay • 18°C"

        findViewById<TextView>(
            R.id.tvWeatherLocationTag
        ).text = locationDisplay

        val routeDestination =
            prefs.savedRouteDestination.trim()

        findViewById<TextView>(
            R.id.tvTrafficHeading
        ).text =
            if (routeDestination.isNotEmpty()) {
                "Traffic to $routeDestination"
            } else {
                getString(R.string.traffic_to_sandton)
            }

        Log.d(
            "CommuteCompanion",
            "Home Dashboard loaded"
        )

        findViewById<LinearLayout>(
            R.id.navHome
        ).setOnClickListener {
            // Already on Home
        }

        findViewById<LinearLayout>(
            R.id.navAlerts
        ).setOnClickListener {
            Toast.makeText(
                this,
                "Alerts — coming soon",
                Toast.LENGTH_SHORT
            ).show()
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
}