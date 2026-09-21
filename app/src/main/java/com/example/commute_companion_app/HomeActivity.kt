package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        auth = FirebaseAuth.getInstance()
        credentialManager = CredentialManager.create(this)

        val prefs = AppPreferences(this)

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

        val tvLocationSubtitle =
            findViewById<TextView>(R.id.tvLocationSubtitle)

        tvLocationSubtitle.text = "$locationDisplay • 18°C"

        val tvWeatherLocationTag =
            findViewById<TextView>(R.id.tvWeatherLocationTag)

        tvWeatherLocationTag.text = locationDisplay

        val routeDestination = prefs.savedRouteDestination.trim()

        val tvTrafficHeading =
            findViewById<TextView>(R.id.tvTrafficHeading)

        tvTrafficHeading.text =
            if (routeDestination.isNotEmpty()) {
                "Traffic to $routeDestination"
            } else {
                getString(R.string.traffic_to_sandton)
            }

        Log.d(
            "CommuteCompanion",
            "Home Dashboard loaded"
        )

        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            // Already on Home
        }

        findViewById<LinearLayout>(R.id.navAlerts).setOnClickListener {
            Toast.makeText(
                this,
                "Alerts — coming soon",
                Toast.LENGTH_SHORT
            ).show()
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
            showSignOutDialog()
        }
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage("Are you sure you want to sign out?")
            .setPositiveButton("Sign Out") { _, _ ->
                signOut()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun signOut() {
        auth.signOut()

        AppPreferences(this).clearUserData()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val clearRequest = ClearCredentialStateRequest()

                credentialManager.clearCredentialState(
                    clearRequest
                )

                Log.d(
                    "CommuteCompanion",
                    "User signed out successfully"
                )

            } catch (e: ClearCredentialException) {
                Log.e(
                    "CommuteCompanion",
                    "Could not clear credential state",
                    e
                )
            }

            openAccountEntry()
        }
    }

    private fun openAccountEntry() {
        val intent =
            Intent(
                this,
                AccountEntryActivity::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}