package com.example.commute_companion_app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class NotificationPermissionActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences

    // Handles the real system permission dialog result on Android 13+ (API 33+).
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        finishOnboardingStep(isGranted)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_permission)

        prefs = AppPreferences(this)

        findViewById<Button>(R.id.btnEnableNotifications).setOnClickListener {
            requestNotificationPermission()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val alreadyGranted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (alreadyGranted) {
                finishOnboardingStep(true)
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Below API 33, there is no runtime notification permission dialog —
            // notifications are considered enabled by default on those versions.
            finishOnboardingStep(true)
        }
    }

    private fun finishOnboardingStep(granted: Boolean) {
        prefs.notificationsEnabled = granted
        prefs.onboardingComplete = true

        Log.d(
            "CommuteCompanion",
            "notificationsEnabled saved = ${prefs.notificationsEnabled}, onboardingComplete = ${prefs.onboardingComplete}"
        )

        // Continue regardless of whether permission was granted or denied.
        // Clear the entire onboarding back stack so Home Dashboard becomes
        // the new task root — pressing Back from Home will no longer walk
        // backward through onboarding.
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
    }
}