package com.example.commute_companion_app

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class SetLocationActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences
    private lateinit var etSearchLocation: EditText
    private lateinit var savedLocationCard: LinearLayout
    private lateinit var tvSavedLocationLabel: TextView
    private lateinit var tvSavedLocationAddress: TextView

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var currentlySelectedLabel: String = "Work"

    private val locationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->

            val fineGranted =
                permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true

            val coarseGranted =
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (fineGranted || coarseGranted) {
                getCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    "Location permission is required to use your current location.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_location)

        prefs = AppPreferences(this)

        etSearchLocation =
            findViewById(R.id.etSearchLocation)

        savedLocationCard =
            findViewById(R.id.savedLocationCard)

        tvSavedLocationLabel =
            findViewById(R.id.tvSavedLocationLabel)

        tvSavedLocationAddress =
            findViewById(R.id.tvSavedLocationAddress)

        val chipHome =
            findViewById<TextView>(R.id.chipHome)

        val chipWork =
            findViewById<TextView>(R.id.chipWork)

        val chipCampus =
            findViewById<TextView>(R.id.chipCampus)

        val chipCustom =
            findViewById<TextView>(R.id.chipCustom)

        val chips = listOf(
            chipHome,
            chipWork,
            chipCampus,
            chipCustom
        )

        val chipLabels = listOf(
            "Home",
            "Work",
            "Campus",
            "Custom"
        )

        currentlySelectedLabel =
            prefs.savedLocationLabel

        if (prefs.savedLocationAddress.isNotBlank()) {
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
            chipLabels.indexOf(
                currentlySelectedLabel
            ).let { index ->

                if (index == -1) {
                    1
                } else {
                    index
                }
            }

        selectChip(initialIndex)

        chips.forEachIndexed { index, chip ->

            chip.setOnClickListener {
                selectChip(index)
            }
        }

        findViewById<ImageButton>(
            R.id.btnBack
        ).setOnClickListener {

            finish()
        }

        findViewById<LinearLayout>(
            R.id.btnUseCurrentLocation
        ).setOnClickListener {

            requestCurrentLocation()
        }

        findViewById<LinearLayout>(
            R.id.suggestedLocationCard
        ).setOnClickListener {

            val suggestedLocation =
                "${getString(R.string.sandton_central)}, " +
                        getString(
                            R.string.sandton_central_address
                        )

            etSearchLocation.setText(
                suggestedLocation
            )
        }

        findViewById<Button>(
            R.id.btnSaveLocation
        ).setOnClickListener {

            saveLocation()
        }

        findViewById<Button>(
            R.id.btnDeleteLocation
        ).setOnClickListener {

            deleteLocation()
        }

        refreshSavedLocation()

        if (
            intent.getBooleanExtra(
                "useCurrentLocation",
                false
            )
        ) {
            requestCurrentLocation()
        }
    }

    private fun requestCurrentLocation() {

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

            getCurrentLocation()

        } else {

            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {

        Toast.makeText(
            this,
            "Getting current location...",
            Toast.LENGTH_SHORT
        ).show()

        fusedLocationClient
            .getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                null
            )
            .addOnSuccessListener { location ->

                if (location != null) {

                    Log.d(
                        "CommuteCompanion",
                        "Location received"
                    )

                    convertLocationToAddress(
                        location.latitude,
                        location.longitude
                    )

                } else {

                    Toast.makeText(
                        this,
                        "Unable to get your location. Make sure location services are turned on.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener { error ->

                Log.e(
                    "CommuteCompanion",
                    "Location request failed",
                    error
                )

                Toast.makeText(
                    this,
                    "Could not get your current location.",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun convertLocationToAddress(
        latitude: Double,
        longitude: Double
    ) {

        val geocoder =
            Geocoder(
                this,
                Locale.getDefault()
            )

        if (!Geocoder.isPresent()) {

            showCoordinateFallback(
                latitude,
                longitude
            )

            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            geocoder.getFromLocation(
                latitude,
                longitude,
                1
            ) { addresses ->

                runOnUiThread {

                    if (addresses.isNotEmpty()) {

                        val address =
                            addresses[0]
                                .getAddressLine(0)

                        showLocationAddress(
                            address
                        )

                    } else {

                        showCoordinateFallback(
                            latitude,
                            longitude
                        )
                    }
                }
            }

        } else {

            CoroutineScope(
                Dispatchers.IO
            ).launch {

                val address =
                    try {

                        @Suppress("DEPRECATION")
                        geocoder.getFromLocation(
                            latitude,
                            longitude,
                            1
                        )?.firstOrNull()
                            ?.getAddressLine(0)

                    } catch (e: Exception) {

                        Log.e(
                            "CommuteCompanion",
                            "Geocoding failed",
                            e
                        )

                        null
                    }

                withContext(
                    Dispatchers.Main
                ) {

                    if (!address.isNullOrBlank()) {

                        showLocationAddress(
                            address
                        )

                    } else {

                        showCoordinateFallback(
                            latitude,
                            longitude
                        )
                    }
                }
            }
        }
    }

    private fun showLocationAddress(
        address: String
    ) {

        etSearchLocation.setText(
            address
        )

        Toast.makeText(
            this,
            "Current location found.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showCoordinateFallback(
        latitude: Double,
        longitude: Double
    ) {

        val coordinates =
            String.format(
                Locale.US,
                "%.5f, %.5f",
                latitude,
                longitude
            )

        etSearchLocation.setText(
            coordinates
        )

        Toast.makeText(
            this,
            "Location found. Address lookup was unavailable.",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun saveLocation() {

        val location =
            etSearchLocation
                .text
                .toString()
                .trim()

        if (location.isEmpty()) {

            Toast.makeText(
                this,
                "Please enter a location before saving.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        prefs.savedLocationAddress =
            location

        prefs.savedLocationLabel =
            currentlySelectedLabel

        Log.d(
            "CommuteCompanion",
            "Location saved successfully"
        )

        refreshSavedLocation()

        Toast.makeText(
            this,
            "$currentlySelectedLabel location saved.",
            Toast.LENGTH_SHORT
        ).show()

        if (prefs.onboardingComplete) {

            finish()

        } else {

            startActivity(
                Intent(
                    this,
                    SetRouteActivity::class.java
                )
            )
        }
    }

    private fun deleteLocation() {

        if (
            prefs.savedLocationAddress.isBlank()
        ) {

            Toast.makeText(
                this,
                "There is no saved location to delete.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        prefs.savedLocationAddress = ""
        prefs.savedLocationLabel = "Work"

        currentlySelectedLabel = "Work"

        etSearchLocation.text.clear()

        Log.d(
            "CommuteCompanion",
            "Saved location deleted"
        )

        refreshSavedLocation()

        Toast.makeText(
            this,
            "Saved location deleted.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun refreshSavedLocation() {

        val address =
            prefs.savedLocationAddress.trim()

        if (address.isEmpty()) {

            savedLocationCard.visibility =
                View.GONE

        } else {

            savedLocationCard.visibility =
                View.VISIBLE

            tvSavedLocationLabel.text =
                prefs.savedLocationLabel

            tvSavedLocationAddress.text =
                address
        }
    }
}