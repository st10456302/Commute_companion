package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AlertsActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alerts)

        prefs = AppPreferences(this)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.alertTraffic).setOnClickListener {
            Toast.makeText(
                this,
                "Traffic alert details opened.",
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<LinearLayout>(R.id.alertPower).setOnClickListener {
            Toast.makeText(
                this,
                "Load-shedding alert details opened.",
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<LinearLayout>(R.id.alertWeather).setOnClickListener {
            Toast.makeText(
                this,
                "Weather alert details opened.",
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            openHome()
        }

        findViewById<LinearLayout>(R.id.navAlerts).setOnClickListener {
            // Already on Alerts
        }

        findViewById<LinearLayout>(R.id.navLocations).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SetLocationActivity::class.java
                )
            )
        }

        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
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
        loadAlertData()
    }

    private fun loadAlertData() {

        val routeDestination =
            prefs.savedRouteDestination.trim()

        val location =
            prefs.savedLocationAddress.trim()

        findViewById<TextView>(
            R.id.tvTrafficAlert
        ).text =
            if (routeDestination.isNotEmpty()) {
                "Moderate traffic reported towards $routeDestination."
            } else {
                "Save a route to receive personalised traffic alerts."
            }

        findViewById<TextView>(
            R.id.tvWeatherAlert
        ).text =
            if (location.isNotEmpty()) {
                "Weather conditions may affect travel around $location."
            } else {
                "Save a location to receive local weather alerts."
            }

        findViewById<TextView>(
            R.id.tvNotificationStatus
        ).text =
            if (prefs.notificationsEnabled) {
                "Notifications are enabled"
            } else {
                "Notifications are currently disabled"
            }
    }

    private fun openHome() {

        val intent =
            Intent(
                this,
                HomeActivity::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_CLEAR_TOP

        startActivity(intent)
        finish()
    }
}