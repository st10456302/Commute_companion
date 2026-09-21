package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LocationAccessActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_access)

        val goToSetLocation = {
            val intent = Intent(this, SetLocationActivity::class.java)

            // This screen is only reached during first-time onboarding.
            intent.putExtra("firstTimeSetup", true)

            startActivity(intent)
            finish()
        }

        // "Allow location access"
        // NOTE: This does not yet trigger a real Android runtime permission
        // dialog. That is intentionally deferred to a later, dedicated step.
        // For now, it simply continues onboarding into Set Location.
        findViewById<LinearLayout>(R.id.btnAllowLocation).setOnClickListener {
            Log.d(
                "CommuteCompanion",
                "Location Access: Allow tapped -> continuing to Set Location"
            )
            goToSetLocation()
        }

        // "Enter a location manually"
        findViewById<Button>(R.id.btnManualLocation).setOnClickListener {
            Log.d(
                "CommuteCompanion",
                "Location Access: Manual entry tapped -> continuing to Set Location"
            )
            goToSetLocation()
        }

        // "Not now"
        findViewById<TextView>(R.id.tvNotNow).setOnClickListener {
            Log.d(
                "CommuteCompanion",
                "Location Access: Not now tapped -> continuing to Set Location"
            )
            goToSetLocation()
        }
    }
}