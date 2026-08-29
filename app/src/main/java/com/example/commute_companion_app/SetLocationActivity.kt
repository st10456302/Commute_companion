package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SetLocationActivity : AppCompatActivity() {

    // Tracks which "Save As" chip is currently selected, defaults to "Work"
    // since that is the chip shown pre-highlighted in the existing UI.
    private var currentlySelectedLabel: String = "Work"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_location)

        val etSearchLocation = findViewById<EditText>(R.id.etSearchLocation)
        val chipHome = findViewById<TextView>(R.id.chipHome)
        val chipWork = findViewById<TextView>(R.id.chipWork)
        val chipCampus = findViewById<TextView>(R.id.chipCampus)
        val chipCustom = findViewById<TextView>(R.id.chipCustom)

        val chips = listOf(chipHome, chipWork, chipCampus, chipCustom)
        val chipLabels = listOf("Home", "Work", "Campus", "Custom")

        val prefs = AppPreferences(this)

        // Restore whatever was previously saved (if this screen is revisited)
        currentlySelectedLabel = prefs.savedLocationLabel
        if (prefs.savedLocationAddress.isNotEmpty()) {
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

        // Apply the restored/default chip selection visually on screen load
        val initialIndex = chipLabels.indexOf(currentlySelectedLabel).let { if (it == -1) 1 else it }
        selectChip(initialIndex)

        chips.forEachIndexed { i, chip -> chip.setOnClickListener { selectChip(i) } }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnSaveLocation).setOnClickListener {
            val locationText = etSearchLocation.text.toString().trim()

            if (locationText.isEmpty()) {
                Toast.makeText(this, "Please enter a location before saving.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            prefs.savedLocationAddress = locationText
            prefs.savedLocationLabel = currentlySelectedLabel

            Log.d(
                "CommuteCompanion",
                "Location saved — address='${prefs.savedLocationAddress}', label='${prefs.savedLocationLabel}'"
            )

            startActivity(Intent(this, SetRouteActivity::class.java))
        }
    }
}