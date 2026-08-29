package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SetRouteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_route)

        val etRouteName = findViewById<EditText>(R.id.etRouteName)
        val etDestination = findViewById<EditText>(R.id.etDestination)
        val chipHomeRoute = findViewById<LinearLayout>(R.id.chipHomeRoute)
        val chipOfficeRoute = findViewById<LinearLayout>(R.id.chipOfficeRoute)

        val prefs = AppPreferences(this)

        // Restore whatever was previously saved (if this screen is revisited)
        if (prefs.savedRouteName.isNotEmpty()) {
            etRouteName.setText(prefs.savedRouteName)
        }
        if (prefs.savedRouteDestination.isNotEmpty()) {
            etDestination.setText(prefs.savedRouteDestination)
        }

        // Quick Select shortcuts fill in the Destination field
        chipHomeRoute.setOnClickListener {
            etDestination.setText(getString(R.string.home))
        }
        chipOfficeRoute.setOnClickListener {
            etDestination.setText(getString(R.string.office_sandton))
        }

        findViewById<Button>(R.id.btnSaveRoute).setOnClickListener {
            val destination = etDestination.text.toString().trim()

            if (destination.isEmpty()) {
                Toast.makeText(this, "Please enter a destination before saving.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Route Name is optional — fall back to a sensible default if left blank.
            val routeName = etRouteName.text.toString().trim().ifEmpty { getString(R.string.morning_commute) }

            prefs.savedRouteName = routeName
            prefs.savedRouteDestination = destination

            Log.d(
                "CommuteCompanion",
                "Route saved — name='${prefs.savedRouteName}', destination='${prefs.savedRouteDestination}'"
            )

            startActivity(Intent(this, NotificationPermissionActivity::class.java))
        }
    }
}