package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.commute_companion_app.api.CommuteRepository
import kotlinx.coroutines.launch

class SetLocationActivity : AppCompatActivity() {

    private var currentlySelectedLabel: String = "Work"

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LanguageManager.applyLanguage(newBase)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_set_location)

        val firstTimeSetup =
            intent.getBooleanExtra("firstTimeSetup", false)

        // ---------------------------------------------------------
        // Bottom navigation
        // ---------------------------------------------------------
        val bottomNavBar =
            findViewById<android.view.View>(R.id.bottomNavBar)

        bottomNavBar.visibility =
            if (firstTimeSetup) {
                android.view.View.GONE
            } else {
                android.view.View.VISIBLE
            }

        if (!firstTimeSetup) {
            setupNavigation()
        }

        // ---------------------------------------------------------
        // Location screen
        // ---------------------------------------------------------
        val etSearchLocation =
            findViewById<EditText>(R.id.etSearchLocation)

        val chipHome =
            findViewById<TextView>(R.id.chipHome)

        val chipWork =
            findViewById<TextView>(R.id.chipWork)

        val chipCampus =
            findViewById<TextView>(R.id.chipCampus)

        val chipCustom =
            findViewById<TextView>(R.id.chipCustom)

        val chips =
            listOf(
                chipHome,
                chipWork,
                chipCampus,
                chipCustom
            )

        val chipLabels =
            listOf(
                "Home",
                "Work",
                "Campus",
                "Custom"
            )

        val prefs =
            AppPreferences(this)

        currentlySelectedLabel =
            prefs.savedLocationLabel

        if (prefs.savedLocationAddress.isNotEmpty()) {
            etSearchLocation.setText(
                prefs.savedLocationAddress
            )
        }

        fun selectChip(index: Int) {

            chips.forEachIndexed { i, chip ->

                if (i == index) {

                    chip.setBackgroundResource(
                        R.drawable.bg_card_selected
                    )

                    chip.setTextColor(
                        getColor(R.color.teal_dark)
                    )

                } else {

                    chip.setBackgroundResource(
                        R.drawable.bg_button_outline_light
                    )

                    chip.setTextColor(
                        getColor(R.color.text_primary)
                    )
                }
            }

            currentlySelectedLabel =
                chipLabels[index]
        }

        val initialIndex =
            chipLabels
                .indexOf(currentlySelectedLabel)
                .let {
                    if (it == -1) 1 else it
                }

        selectChip(initialIndex)

        chips.forEachIndexed { i, chip ->

            chip.setOnClickListener {
                selectChip(i)
            }
        }

        // ---------------------------------------------------------
        // Back button
        // ---------------------------------------------------------
        findViewById<ImageButton>(
            R.id.btnBack
        ).setOnClickListener {
            finish()
        }

        // ---------------------------------------------------------
        // Save location
        // ---------------------------------------------------------
        findViewById<Button>(
            R.id.btnSaveLocation
        ).setOnClickListener {

            val locationText =
                etSearchLocation
                    .text
                    .toString()
                    .trim()

            if (locationText.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please enter a location before saving.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val saveButton =
                findViewById<Button>(
                    R.id.btnSaveLocation
                )

            saveButton.isEnabled = false

            lifecycleScope.launch {

                val repository =
                    CommuteRepository(
                        AppPreferences(
                            this@SetLocationActivity
                        )
                    )

                Log.d(
                    "CommuteCompanionAPI",
                    "Saving location through REST API — " +
                            "label='$currentlySelectedLabel', " +
                            "address='$locationText'"
                )

                val result =
                    repository.createSavedLocation(
                        label = currentlySelectedLabel,
                        address = locationText,
                        latitude = null,
                        longitude = null
                    )

                result.onSuccess { savedLocation ->

                    prefs.savedLocationAddress =
                        locationText

                    prefs.savedLocationLabel =
                        currentlySelectedLabel

                    Log.d(
                        "CommuteCompanionAPI",
                        "Location saved successfully — " +
                                "databaseId='${savedLocation.id}', " +
                                "label='${savedLocation.label}', " +
                                "address='${savedLocation.address}'"
                    )

                    Toast.makeText(
                        this@SetLocationActivity,
                        "Location saved successfully.",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(
                        Intent(
                            this@SetLocationActivity,
                            SetRouteActivity::class.java
                        ).apply {

                            putExtra(
                                "firstTimeSetup",
                                firstTimeSetup
                            )
                        }
                    )

                    finish()
                }

                result.onFailure { exception ->

                    Log.e(
                        "CommuteCompanionAPI",
                        "Location could not be saved through REST API",
                        exception
                    )

                    saveButton.isEnabled = true

                    Toast.makeText(
                        this@SetLocationActivity,
                        "Unable to save location. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    // -------------------------------------------------------------
    // Main app bottom navigation
    // -------------------------------------------------------------
    private fun setupNavigation() {

        // Home
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

        // Alerts
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

        // Locations
        // Already on Set Location.
        findViewById<LinearLayout>(
            R.id.navLocations
        ).setOnClickListener {
            // Already on Set Location.
        }

        // Profile
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