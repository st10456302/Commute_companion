package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SetRouteActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences
    private lateinit var etRouteName: EditText
    private lateinit var etDestination: EditText
    private lateinit var savedRouteCard: LinearLayout
    private lateinit var tvSavedRouteName: TextView
    private lateinit var tvSavedRouteDestination: TextView
    private lateinit var tvStartingPointValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_route)

        prefs = AppPreferences(this)

        etRouteName = findViewById(R.id.etRouteName)
        etDestination = findViewById(R.id.etDestination)
        savedRouteCard = findViewById(R.id.savedRouteCard)
        tvSavedRouteName = findViewById(R.id.tvSavedRouteName)
        tvSavedRouteDestination = findViewById(R.id.tvSavedRouteDestination)
        tvStartingPointValue = findViewById(R.id.tvStartingPointValue)

        val chipHomeRoute =
            findViewById<LinearLayout>(R.id.chipHomeRoute)

        val chipOfficeRoute =
            findViewById<LinearLayout>(R.id.chipOfficeRoute)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        restoreRoute()
        updateStartingPoint()
        refreshSavedRoute()

        chipHomeRoute.setOnClickListener {
            val homeDestination =
                if (
                    prefs.savedLocationLabel == "Home" &&
                    prefs.savedLocationAddress.isNotBlank()
                ) {
                    prefs.savedLocationAddress
                } else {
                    getString(R.string.home)
                }

            etDestination.setText(homeDestination)
        }

        chipOfficeRoute.setOnClickListener {
            etDestination.setText(
                getString(R.string.office_sandton)
            )
        }

        findViewById<Button>(R.id.btnSaveRoute)
            .setOnClickListener {
                saveRoute()
            }

        findViewById<Button>(R.id.btnDeleteRoute)
            .setOnClickListener {
                deleteRoute()
            }
    }

    private fun restoreRoute() {
        if (prefs.savedRouteName.isNotBlank()) {
            etRouteName.setText(
                prefs.savedRouteName
            )
        }

        if (prefs.savedRouteDestination.isNotBlank()) {
            etDestination.setText(
                prefs.savedRouteDestination
            )
        }
    }

    private fun updateStartingPoint() {
        val startingPoint =
            prefs.savedLocationAddress.trim()

        tvStartingPointValue.text =
            if (startingPoint.isNotEmpty()) {
                startingPoint
            } else {
                getString(R.string.current_location)
            }
    }

    private fun saveRoute() {
        val destination =
            etDestination.text.toString().trim()

        if (destination.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter a destination before saving.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val routeName =
            etRouteName.text.toString().trim()
                .ifEmpty {
                    getString(R.string.morning_commute)
                }

        prefs.savedRouteName = routeName
        prefs.savedRouteDestination = destination

        Log.d(
            "CommuteCompanion",
            "Route saved successfully"
        )

        refreshSavedRoute()

        Toast.makeText(
            this,
            "Route saved successfully.",
            Toast.LENGTH_SHORT
        ).show()

        if (prefs.onboardingComplete) {
            finish()
        } else {
            startActivity(
                Intent(
                    this,
                    NotificationPermissionActivity::class.java
                )
            )
        }
    }

    private fun deleteRoute() {
        if (
            prefs.savedRouteName.isBlank() &&
            prefs.savedRouteDestination.isBlank()
        ) {
            Toast.makeText(
                this,
                "There is no saved route to delete.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        prefs.savedRouteName = ""
        prefs.savedRouteDestination = ""

        etRouteName.text.clear()
        etDestination.text.clear()

        Log.d(
            "CommuteCompanion",
            "Saved route deleted"
        )

        refreshSavedRoute()

        Toast.makeText(
            this,
            "Saved route deleted.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun refreshSavedRoute() {
        val routeName =
            prefs.savedRouteName.trim()

        val destination =
            prefs.savedRouteDestination.trim()

        if (
            routeName.isEmpty() &&
            destination.isEmpty()
        ) {
            savedRouteCard.visibility = View.GONE
        } else {
            savedRouteCard.visibility = View.VISIBLE

            tvSavedRouteName.text =
                routeName.ifEmpty {
                    getString(R.string.morning_commute)
                }

            tvSavedRouteDestination.text =
                destination
        }
    }
}