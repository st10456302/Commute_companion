package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.graphics.Color
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
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SetLocationActivity : AppCompatActivity() {

    private var currentlySelectedLabel: String = "Work"

    private lateinit var locationMap: MapView

    private var circleAnnotationManager: CircleAnnotationManager? = null
    private var locationAnnotation: CircleAnnotation? = null

    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null

    private var geocodingJob: Job? = null

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

        setupMap()

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

        val prefs = AppPreferences(this)

        currentlySelectedLabel =
            prefs.savedLocationLabel.ifEmpty {
                "Work"
            }

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

        setupAddressSearch(etSearchLocation)

        findViewById<ImageButton>(R.id.btnBack)
            .setOnClickListener {
                finish()
            }

        findViewById<Button>(R.id.btnSaveLocation)
            .setOnClickListener {

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

                if (
                    selectedLatitude == null ||
                    selectedLongitude == null
                ) {

                    Toast.makeText(
                        this,
                        "Please wait for the location to be found on the map.",
                        Toast.LENGTH_LONG
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
                                "address='$locationText', " +
                                "latitude='$selectedLatitude', " +
                                "longitude='$selectedLongitude'"
                    )

                    val result =
                        repository.createSavedLocation(
                            label = currentlySelectedLabel,
                            address = locationText,
                            latitude = selectedLatitude,
                            longitude = selectedLongitude
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
                                    "address='${savedLocation.address}', " +
                                    "latitude='${savedLocation.latitude}', " +
                                    "longitude='${savedLocation.longitude}'"
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

    private fun setupMap() {

        locationMap =
            findViewById(R.id.locationMap)

        val initialPoint =
            Point.fromLngLat(
                28.0473,
                -26.2041
            )

        locationMap.mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(initialPoint)
                .zoom(11.0)
                .build()
        )

        locationMap.mapboxMap.loadStyle(
            Style.STANDARD
        ) {

            circleAnnotationManager =
                locationMap.annotations
                    .createCircleAnnotationManager()

            Log.d(
                "CommuteCompanion",
                "Mapbox location map initialized."
            )
        }
    }

    private fun setupAddressSearch(
        editText: EditText
    ) {

        editText.addTextChangedListener(
            object : android.text.TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    val address =
                        s?.toString()
                            ?.trim()
                            .orEmpty()

                    geocodingJob?.cancel()

                    selectedLatitude = null
                    selectedLongitude = null

                    removeLocationAnnotation()

                    if (address.length < 5) {

                        findViewById<TextView>(
                            R.id.tvMapStatus
                        ).text =
                            "Enter an address to locate it"

                        return
                    }

                    findViewById<TextView>(
                        R.id.tvMapStatus
                    ).text =
                        "Finding location..."

                    geocodingJob =
                        lifecycleScope.launch {

                            delay(900)

                            geocodeAddress(
                                address
                            )
                        }
                }

                override fun afterTextChanged(
                    s: android.text.Editable?
                ) {
                }
            }
        )
    }

    private suspend fun geocodeAddress(
        address: String
    ) {

        Log.d(
            "CommuteCompanionAPI",
            "Geocoding address='$address'"
        )

        val repository =
            CommuteRepository(
                AppPreferences(this)
            )

        val result =
            repository.geocodeLocation(
                address
            )

        result.onSuccess { coordinates ->

            selectedLatitude =
                coordinates.latitude

            selectedLongitude =
                coordinates.longitude

            Log.d(
                "CommuteCompanionAPI",
                "Address geocoded successfully — " +
                        "latitude='${coordinates.latitude}', " +
                        "longitude='${coordinates.longitude}'"
            )

            runOnUiThread {

                updateMapLocation(
                    coordinates.latitude,
                    coordinates.longitude
                )

                findViewById<TextView>(
                    R.id.tvMapStatus
                ).text =
                    "Location found"
            }
        }

        result.onFailure { exception ->

            Log.e(
                "CommuteCompanionAPI",
                "Address could not be geocoded.",
                exception
            )

            runOnUiThread {

                findViewById<TextView>(
                    R.id.tvMapStatus
                ).text =
                    "Location not found"

                Toast.makeText(
                    this,
                    "Location not found. Try a more specific address.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateMapLocation(
        latitude: Double,
        longitude: Double
    ) {

        val point =
            Point.fromLngLat(
                longitude,
                latitude
            )

        locationMap.mapboxMap.flyTo(
            CameraOptions.Builder()
                .center(point)
                .zoom(16.0)
                .build()
        )

        removeLocationAnnotation()

        val annotationManager =
            circleAnnotationManager

        if (annotationManager == null) {

            Log.w(
                "CommuteCompanion",
                "Mapbox annotation manager is not ready yet."
            )

            return
        }

        val options =
            CircleAnnotationOptions()
                .withPoint(point)
                .withCircleColor(
                    Color.rgb(15, 36, 56)
                )
                .withCircleRadius(10.0)
                .withCircleStrokeColor(
                    Color.WHITE
                )
                .withCircleStrokeWidth(3.0)
                .withDraggable(false)

        locationAnnotation =
            annotationManager.create(
                options
            )

        Log.d(
            "CommuteCompanion",
            "Mapbox map moved to selected location — " +
                    "latitude=$latitude, " +
                    "longitude=$longitude"
        )
    }

    private fun removeLocationAnnotation() {

        val annotation =
            locationAnnotation

        val manager =
            circleAnnotationManager

        if (
            annotation != null &&
            manager != null
        ) {

            manager.delete(
                annotation
            )
        }

        locationAnnotation = null
    }

    private fun setupNavigation() {

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
            // Already on Set Location.
        }

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

    override fun onStart() {
        super.onStart()

        if (::locationMap.isInitialized) {
            locationMap.onStart()
        }
    }

    override fun onStop() {

        if (::locationMap.isInitialized) {
            locationMap.onStop()
        }

        super.onStop()
    }

    override fun onDestroy() {

        geocodingJob?.cancel()

        if (::locationMap.isInitialized) {
            locationMap.onDestroy()
        }

        super.onDestroy()
    }
}