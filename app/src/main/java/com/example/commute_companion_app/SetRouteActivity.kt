package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.commute_companion_app.api.CommuteRepository
import kotlinx.coroutines.launch

class SetRouteActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LanguageManager.applyLanguage(newBase)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_set_route)

        val etRouteName =
            findViewById<EditText>(R.id.etRouteName)

        val etDestination =
            findViewById<EditText>(R.id.etDestination)

        val tvStartingPoint =
            findViewById<TextView>(R.id.tvStartingPoint)

        val prefs =
            AppPreferences(this)

        // Restore whether this route setup came from first-time onboarding.
        val firstTimeSetup =
            intent.getBooleanExtra("firstTimeSetup", false)

        // Display the location saved through Set Location.
        val savedLocation =
            prefs.savedLocationAddress.trim()

        if (savedLocation.isNotEmpty()) {
            tvStartingPoint.text = savedLocation
        } else {
            tvStartingPoint.text =
                getString(R.string.current_location)

            Log.w(
                "CommuteCompanion",
                "No saved location was found for route starting point."
            )
        }

        Log.d(
            "CommuteCompanion",
            "Route starting point loaded — " +
                    "label='${prefs.savedLocationLabel}', " +
                    "address='$savedLocation'"
        )

        // Restore the previously saved route.
        if (prefs.savedRouteName.isNotEmpty()) {
            etRouteName.setText(
                prefs.savedRouteName
            )
        }

        if (prefs.savedRouteDestination.isNotEmpty()) {
            etDestination.setText(
                prefs.savedRouteDestination
            )
        }

        findViewById<Button>(R.id.btnSaveRoute)
            .setOnClickListener {

                val destination =
                    etDestination.text
                        .toString()
                        .trim()

                if (destination.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Please enter a destination before saving.",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }

                val routeName =
                    etRouteName.text
                        .toString()
                        .trim()
                        .ifEmpty {
                            getString(
                                R.string.morning_commute
                            )
                        }

                val saveButton =
                    findViewById<Button>(
                        R.id.btnSaveRoute
                    )

                saveButton.isEnabled = false

                lifecycleScope.launch {

                    val repository =
                        CommuteRepository(
                            AppPreferences(
                                this@SetRouteActivity
                            )
                        )

                    Log.d(
                        "CommuteCompanionAPI",
                        "Saving route through REST API — " +
                                "name='$routeName', " +
                                "startingPoint='$savedLocation', " +
                                "destination='$destination'"
                    )

                    val result =
                        repository.createSavedRoute(
                            name = routeName,
                            destination = destination
                        )

                    result.onSuccess { savedRoute ->

                        prefs.savedRouteName =
                            routeName

                        prefs.savedRouteDestination =
                            destination

                        Log.d(
                            "CommuteCompanionAPI",
                            "Route saved successfully — " +
                                    "databaseId='${savedRoute.id}', " +
                                    "name='${savedRoute.name}', " +
                                    "destination='${savedRoute.destination}', " +
                                    "latitude='${savedRoute.destinationLatitude}', " +
                                    "longitude='${savedRoute.destinationLongitude}'"
                        )

                        Toast.makeText(
                            this@SetRouteActivity,
                            "Route saved successfully.",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.d(
                            "CommuteCompanion",
                            "Route setup complete — returning to Home Dashboard. " +
                                    "firstTimeSetup=$firstTimeSetup"
                        )

                        startActivity(
                            Intent(
                                this@SetRouteActivity,
                                HomeActivity::class.java
                            ).apply {
                                flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                            }
                        )

                        finish()
                    }

                    result.onFailure { exception ->

                        Log.e(
                            "CommuteCompanionAPI",
                            "Route could not be saved through REST API",
                            exception
                        )

                        saveButton.isEnabled = true

                        Toast.makeText(
                            this@SetRouteActivity,
                            "Unable to save route. Please check the destination and try again.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }
}