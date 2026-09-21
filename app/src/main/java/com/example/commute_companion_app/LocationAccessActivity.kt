package com.example.commute_companion_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class LocationAccessActivity : AppCompatActivity() {

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            val coarseGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineGranted || coarseGranted) {
                Log.d(
                    "CommuteCompanion",
                    "Location permission granted"
                )

                openSetLocation(true)
            } else {
                Log.d(
                    "CommuteCompanion",
                    "Location permission denied"
                )

                Toast.makeText(
                    this,
                    "Location permission was not granted. You can enter your location manually.",
                    Toast.LENGTH_LONG
                ).show()

                openSetLocation(false)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_access)

        findViewById<LinearLayout>(
            R.id.btnAllowLocation
        ).setOnClickListener {

            requestLocationPermission()
        }

        findViewById<Button>(
            R.id.btnManualLocation
        ).setOnClickListener {

            openSetLocation(false)
        }

        findViewById<TextView>(
            R.id.tvNotNow
        ).setOnClickListener {

            openSetLocation(false)
        }
    }

    private fun requestLocationPermission() {

        val fineGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        if (fineGranted || coarseGranted) {
            openSetLocation(true)
            return
        }

        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    private fun openSetLocation(useCurrentLocation: Boolean) {

        Log.d(
            "CommuteCompanion",
            "Opening Set Location. Use current location = $useCurrentLocation"
        )

        val intent =
            Intent(
                this,
                SetLocationActivity::class.java
            )

        intent.putExtra(
            "useCurrentLocation",
            useCurrentLocation
        )

        startActivity(intent)
    }
}