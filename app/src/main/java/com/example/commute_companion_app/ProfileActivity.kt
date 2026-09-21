package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LanguageManager.applyLanguage(newBase)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)

        // Settings
        findViewById<Button>(
            R.id.btnSettings
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SettingsActivity::class.java
                )
            )
        }

        // Sign Out
        findViewById<Button>(
            R.id.btnSignOut
        ).setOnClickListener {

            FirebaseAuth.getInstance().signOut()

            AppPreferences(this).clearSession()

            startActivity(
                Intent(
                    this,
                    SignInActivity::class.java
                ).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            )

            finish()
        }

        // Bottom navigation
        findViewById<LinearLayout>(
            R.id.navHome
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    HomeActivity::class.java
                ).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            )
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
            // Already on Profile.
        }

        BottomNavigationHelper.setSelectedTab(
            findViewById(android.R.id.content),
            BottomNavigationHelper.Tab.PROFILE
        )

        updateProfileInformation()
    }

    override fun onResume() {
        super.onResume()

        BottomNavigationHelper.setSelectedTab(
            findViewById(android.R.id.content),
            BottomNavigationHelper.Tab.PROFILE
        )

        // Refresh the displayed preferences whenever
        // the user returns to the Profile screen.
        updateProfileInformation()
    }

    private fun updateProfileInformation() {

        val prefs = AppPreferences(this)

        // Profile information
        val profileName = prefs.userName.trim().ifEmpty {
            getString(R.string.profile_user)
        }

        val profileEmail = prefs.accountEmail.trim().ifEmpty {
            getString(R.string.not_available)
        }

        findViewById<TextView>(
            R.id.tvProfileName
        ).text = profileName

        findViewById<TextView>(
            R.id.tvProfileEmail
        ).text = profileEmail

        // Account information
        findViewById<TextView>(
            R.id.tvAccountName
        ).text = profileName

        findViewById<TextView>(
            R.id.tvAccountEmail
        ).text = profileEmail

        // Saved location
        val location = prefs.savedLocationAddress.trim().ifEmpty {
            getString(R.string.no_saved_location)
        }

        val locationLabel = prefs.savedLocationLabel.trim()

        findViewById<TextView>(
            R.id.tvSavedLocation
        ).text =
            if (locationLabel.isNotEmpty()) {
                "${getString(R.string.location)}: $locationLabel — $location"
            } else {
                "${getString(R.string.location)}: $location"
            }

        // Saved route
        val routeName = prefs.savedRouteName.trim()
        val routeDestination = prefs.savedRouteDestination.trim()

        findViewById<TextView>(
            R.id.tvSavedRoute
        ).text =
            when {
                routeName.isNotEmpty() &&
                        routeDestination.isNotEmpty() ->
                    "${getString(R.string.route)}: $routeName → $routeDestination"

                routeDestination.isNotEmpty() ->
                    "${getString(R.string.destination)}: $routeDestination"

                else ->
                    getString(R.string.no_saved_route)
            }

        // Language
        findViewById<TextView>(
            R.id.tvLanguage
        ).text =
            "${getString(R.string.language)}: ${prefs.selectedLanguage}"

        // Notifications
        findViewById<TextView>(
            R.id.tvNotifications
        ).text =
            "${getString(R.string.notifications)}: ${
                if (prefs.notificationsEnabled) {
                    getString(R.string.enabled)
                } else {
                    getString(R.string.disabled)
                }
            }"

        // Biometric
        findViewById<TextView>(
            R.id.tvBiometric
        ).text =
            "${getString(R.string.biometric_login)}: ${
                if (prefs.biometricEnabled) {
                    getString(R.string.enabled)
                } else {
                    getString(R.string.disabled)
                }
            }"
    }
}