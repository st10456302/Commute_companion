package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        // --- Step 1 diagnostic: confirm AppPreferences reads/writes correctly ---
        val prefs = AppPreferences(this)
        Log.d("CommuteCompanion", "onboardingComplete (before) = ${prefs.onboardingComplete}")
        // -----------------------------------------------------------------------

        Handler(Looper.getMainLooper()).postDelayed({
            if (prefs.onboardingComplete) {
                // Returning user — skip onboarding entirely and go straight to Home.
                // Home becomes the new task root, matching the Step 9 back-stack fix,
                // so pressing Back from Home does not reveal Welcome/onboarding.
                Log.d("CommuteCompanion", "Loading complete — onboarding complete, skipping to HomeActivity")

                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                // First-time / incomplete onboarding — unchanged existing behavior.
                Log.d("CommuteCompanion", "Loading complete — onboarding not complete, starting WelcomeActivity")

                startActivity(Intent(this, WelcomeActivity::class.java))
            }
            finish()
        }, 2000)
    }
}