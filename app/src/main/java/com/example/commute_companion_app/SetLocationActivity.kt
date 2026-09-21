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

class SetLocationActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences
    private lateinit var etSearchLocation: EditText
    private lateinit var savedLocationCard: LinearLayout
    private lateinit var tvSavedLocationLabel: TextView
    private lateinit var tvSavedLocationAddress: TextView

    private var currentlySelectedLabel: String = "Work"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_location)

        prefs = AppPreferences(this)

        etSearchLocation = findViewById(R.id.etSearchLocation)
        savedLocationCard = findViewById(R.id.savedLocationCard)
        tvSavedLocationLabel = findViewById(R.id.tvSavedLocationLabel)
        tvSavedLocationAddress = findViewById(R.id.tvSavedLocationAddress)

        val chipHome = findViewById<TextView>(R.id.chipHome)
        val chipWork = findViewById<TextView>(R.id.chipWork)
        val chipCampus = findViewById<TextView>(R.id.chipCampus)
        val chipCustom = findViewById<TextView>(R.id.chipCustom)

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

        currentlySelectedLabel = prefs.savedLocationLabel

        if (prefs.savedLocationAddress.isNotBlank()) {
            etSearchLocation.setText(prefs.savedLocationAddress)
        }

        fun selectChip(index: Int) {
            chips.forEachIndexed { i, chip ->
                if (i == index) {
                    chip.setBackgroundResource(R.drawable.bg_card_selected)
                    chip.setTextColor(getColor(R.color.teal_dark))
                } else {
                    chip.setBackgroundResource(R.drawable.bg_button_outline_light)
                    chip.setTextColor(getColor(R.color.text_primary))
                }
            }

            currentlySelectedLabel = chipLabels[index]
        }

        val initialIndex =
            chipLabels.indexOf(currentlySelectedLabel)
                .let { index ->
                    if (index == -1) 1 else index
                }

        selectChip(initialIndex)

        chips.forEachIndexed { index, chip ->
            chip.setOnClickListener {
                selectChip(index)
            }
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(
            R.id.suggestedLocationCard
        ).setOnClickListener {

            val suggestedLocation =
                "${getString(R.string.sandton_central)}, " +
                        getString(R.string.sandton_central_address)

            etSearchLocation.setText(suggestedLocation)
        }

        findViewById<Button>(R.id.btnSaveLocation)
            .setOnClickListener {

                saveLocation()
            }

        findViewById<Button>(R.id.btnDeleteLocation)
            .setOnClickListener {

                deleteLocation()
            }

        refreshSavedLocation()
    }

    private fun saveLocation() {
        val location =
            etSearchLocation.text.toString().trim()

        if (location.isEmpty()) {
            Toast.makeText(
                this,
                "Please enter a location before saving.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        prefs.savedLocationAddress = location
        prefs.savedLocationLabel = currentlySelectedLabel

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
        if (prefs.savedLocationAddress.isBlank()) {
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
            savedLocationCard.visibility = View.GONE
        } else {
            savedLocationCard.visibility = View.VISIBLE

            tvSavedLocationLabel.text =
                prefs.savedLocationLabel

            tvSavedLocationAddress.text =
                address
        }
    }
}