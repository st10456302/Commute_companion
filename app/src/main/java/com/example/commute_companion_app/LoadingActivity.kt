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

        // --- Step 1 test only: prove AppPreferences reads/writes correctly ---
        val prefs = AppPreferences(this)
        Log.d("CommuteCompanion", "onboardingComplete (before) = ${prefs.onboardingComplete}")
        // We are NOT changing the stored value yet — just confirming we can read it.
        // In a later step, we'll set this to true once the user reaches Home Dashboard.
        // -----------------------------------------------------------------------

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }, 2000)
    }
}