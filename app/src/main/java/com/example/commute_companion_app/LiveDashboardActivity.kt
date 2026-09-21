package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class LiveDashboardActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_dashboard)

        prefs = AppPreferences(this)

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

        findViewById<ImageView>(
            R.id.btnDashboardNotifications
        ).setOnClickListener {

            openAlerts()
        }

        findViewById<LinearLayout>(
            R.id.btnStartCommute
        ).setOnClickListener {

            startCommute()
        }

        findViewById<TextView>(
            R.id.btnViewTraffic
        ).setOnClickListener {

            showTrafficDetails()
        }

        findViewById<LinearLayout>(
            R.id.btnViewSchedule
        ).setOnClickListener {

            showLoadSheddingSchedule()
        }

        findViewById<LinearLayout>(
            R.id.navHome
        ).setOnClickListener {

            finish()
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
        loadDashboard()
    }

    private fun loadDashboard() {

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
            R.id.tvLiveLocation
        ).text =
            location

        val routeName =
            prefs.savedRouteName.trim()
                .ifEmpty {
                    getString(
                        R.string.morning_commute
                    )
                }

        findViewById<TextView>(
            R.id.tvLiveRouteName
        ).text =
            routeName

        val destination =
            prefs.savedRouteDestination.trim()

        findViewById<TextView>(
            R.id.tvLiveDestination
        ).text =
            if (destination.isNotEmpty()) {
                "Destination: $destination"
            } else {
                "No destination saved"
            }
    }

    private fun startCommute() {

        val destination =
            prefs.savedRouteDestination.trim()

        if (destination.isEmpty()) {

            Toast.makeText(
                this,
                "Save a route before starting your commute.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        Toast.makeText(
            this,
            "Commute started to $destination",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun showTrafficDetails() {

        val destination =
            prefs.savedRouteDestination.trim()

        val message =
            if (destination.isNotEmpty()) {
                "Moderate traffic towards $destination. Estimated commute: 25 minutes with a 5 minute delay."
            } else {
                "Save a route to view personalised traffic information."
            }

        AlertDialog.Builder(this)
            .setTitle("Traffic Information")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLoadSheddingSchedule() {

        AlertDialog.Builder(this)
            .setTitle("Load-shedding Schedule")
            .setMessage(
                "Current status: Stage 2\n\nNext slot: 14:00 - 16:30"
            )
            .setPositiveButton("OK", null)
            .show()
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