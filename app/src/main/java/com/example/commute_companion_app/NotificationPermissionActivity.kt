package com.example.commute_companion_app

import android.Manifest
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

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            finishOnboardingStep(
                granted
            )
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_notification_permission
        )

        prefs =
            AppPreferences(this)

        NotificationHelper
            .createNotificationChannel(this)

        findViewById<Button>(
            R.id.btnEnableNotifications
        ).setOnClickListener {

            requestNotificationPermission()
        }
    }

    private fun requestNotificationPermission() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            val alreadyGranted =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            if (alreadyGranted) {

                finishOnboardingStep(true)

            } else {

                requestPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }

        } else {

            finishOnboardingStep(true)
        }
    }

    private fun finishOnboardingStep(
        granted: Boolean
    ) {

        prefs.notificationsEnabled =
            granted

        prefs.onboardingComplete =
            true

        Log.d(
            "CommuteCompanion",
            "Notifications enabled = $granted"
        )

        if (granted) {

            NotificationHelper.showNotification(
                this,
                "Commute Companion Ready",
                "You will now receive commute and travel alerts.",
                1001
            )
        }

        val intent =
            Intent(
                this,
                HomeActivity::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}