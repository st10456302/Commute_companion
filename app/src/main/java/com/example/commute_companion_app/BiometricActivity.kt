package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class BiometricActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric)

        val prefs = AppPreferences(this)

        findViewById<Button>(R.id.btnEnableBiometric).setOnClickListener {
            prefs.biometricEnabled = true
            Log.d("CommuteCompanion", "biometricEnabled saved = ${prefs.biometricEnabled}")
            startActivity(Intent(this, LocationAccessActivity::class.java))
        }

        findViewById<Button>(R.id.btnNotNow).setOnClickListener {
            prefs.biometricEnabled = false
            Log.d("CommuteCompanion", "biometricEnabled saved = ${prefs.biometricEnabled}")
            startActivity(Intent(this, LocationAccessActivity::class.java))
        }
    }
}