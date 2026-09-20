package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val prefs = AppPreferences(this)

        // --- Populate real onboarding data, with safe fallbacks ---

        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)
        val userName = prefs.userName.trim()
        tvGreeting.text = if (userName.isNotEmpty()) {
            "Good Morning, $userName."
        } else {
            getString(R.string.good_morning)
        }

        val locationDisplay = prefs.savedLocationAddress.trim().ifEmpty {
            getString(R.string.sandton_central)
        }

        val tvLocationSubtitle = findViewById<TextView>(R.id.tvLocationSubtitle)
        // Temperature remains mock data — only the location portion is real.
        tvLocationSubtitle.text = "$locationDisplay • 18°C"

        val tvWeatherLocationTag = findViewById<TextView>(R.id.tvWeatherLocationTag)
        tvWeatherLocationTag.text = locationDisplay

        val routeDestination = prefs.savedRouteDestination.trim()
        val tvTrafficHeading = findViewById<TextView>(R.id.tvTrafficHeading)
        tvTrafficHeading.text = if (routeDestination.isNotEmpty()) {
            "Traffic to $routeDestination"
        } else {
            getString(R.string.traffic_to_sandton)
        }

        Log.d(
            "CommuteCompanion",
            "Home Dashboard loaded — userName='${prefs.userName}', " +
                    "savedLocationAddress='${prefs.savedLocationAddress}', " +
                    "savedLocationLabel='${prefs.savedLocationLabel}', " +
                    "savedRouteName='${prefs.savedRouteName}', " +
                    "savedRouteDestination='${prefs.savedRouteDestination}'"
        )

        // --- Bottom navigation ---

        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            // Already on Home — no action needed.
        }

        findViewById<LinearLayout>(R.id.navAlerts).setOnClickListener {
            startActivity(Intent(this, AlertsActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navLocations).setOnClickListener {
            startActivity(Intent(this, SetLocationActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }
}