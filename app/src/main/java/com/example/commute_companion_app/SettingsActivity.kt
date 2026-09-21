package com.example.commute_companion_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.exceptions.ClearCredentialException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences
    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager
    private lateinit var swNotifications: SwitchCompat

    private var updatingNotificationSwitch = false

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            prefs.notificationsEnabled =
                granted

            updatingNotificationSwitch = true
            swNotifications.isChecked = granted
            updatingNotificationSwitch = false

            if (granted) {

                NotificationHelper.showNotification(
                    this,
                    "Notifications Enabled",
                    "Commute alerts are now enabled.",
                    1000
                )

                Toast.makeText(
                    this,
                    "Notifications enabled.",
                    Toast.LENGTH_SHORT
                ).show()

            } else {

                Toast.makeText(
                    this,
                    "Notification permission was not granted.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        prefs = AppPreferences(this)
        auth = FirebaseAuth.getInstance()

        credentialManager =
            CredentialManager.create(this)

        NotificationHelper
            .createNotificationChannel(this)

        findViewById<ImageButton>(
            R.id.btnBack
        ).setOnClickListener {
            finish()
        }

        loadAccountDetails()
        setupLanguagePreference()
        setupNotificationPreference()
        setupBiometricPreference()

        findViewById<Button>(
            R.id.btnSignOut
        ).setOnClickListener {
            showSignOutDialog()
        }
    }

    private fun loadAccountDetails() {

        val user =
            auth.currentUser

        val name =
            prefs.userName.ifBlank {
                user?.displayName
                    ?: "Commute Companion User"
            }

        val email =
            prefs.accountEmail.ifBlank {
                user?.email ?: ""
            }

        findViewById<TextView>(
            R.id.tvSettingsName
        ).text = name

        findViewById<TextView>(
            R.id.tvSettingsEmail
        ).text = email
    }

    private fun setupLanguagePreference() {

        val languageGroup =
            findViewById<RadioGroup>(
                R.id.rgLanguage
            )

        when (prefs.selectedLanguage) {

            "isiZulu" ->
                languageGroup.check(
                    R.id.radioSettingsZulu
                )

            "Afrikaans" ->
                languageGroup.check(
                    R.id.radioSettingsAfrikaans
                )

            else ->
                languageGroup.check(
                    R.id.radioSettingsEnglish
                )
        }

        languageGroup
            .setOnCheckedChangeListener {
                    _,
                    checkedId ->

                prefs.selectedLanguage =
                    when (checkedId) {

                        R.id.radioSettingsZulu ->
                            "isiZulu"

                        R.id.radioSettingsAfrikaans ->
                            "Afrikaans"

                        else ->
                            "English"
                    }

                Log.d(
                    "CommuteCompanion",
                    "Language preference updated"
                )
            }
    }

    private fun setupNotificationPreference() {

        swNotifications =
            findViewById(
                R.id.swNotifications
            )

        val systemPermissionGranted =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU
            ) {

                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            } else {

                true
            }

        if (!systemPermissionGranted) {
            prefs.notificationsEnabled = false
        }

        updatingNotificationSwitch = true

        swNotifications.isChecked =
            prefs.notificationsEnabled &&
                    systemPermissionGranted

        updatingNotificationSwitch = false

        swNotifications
            .setOnCheckedChangeListener {
                    _,
                    enabled ->

                if (updatingNotificationSwitch) {
                    return@setOnCheckedChangeListener
                }

                if (enabled) {

                    enableNotifications()

                } else {

                    disableNotifications()
                }
            }
    }

    private fun enableNotifications() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            val granted =
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED

            if (!granted) {

                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )

                return
            }
        }

        prefs.notificationsEnabled = true

        NotificationHelper.showNotification(
            this,
            "Notifications Enabled",
            "Traffic, weather and commute alerts are enabled.",
            1000
        )

        Log.d(
            "CommuteCompanion",
            "Notifications enabled"
        )
    }

    private fun disableNotifications() {

        prefs.notificationsEnabled = false

        NotificationHelper
            .cancelNotifications(this)

        Toast.makeText(
            this,
            "Notifications disabled.",
            Toast.LENGTH_SHORT
        ).show()

        Log.d(
            "CommuteCompanion",
            "Notifications disabled"
        )
    }

    private fun setupBiometricPreference() {

        val swBiometric =
            findViewById<SwitchCompat>(
                R.id.swBiometric
            )

        swBiometric.isChecked =
            prefs.biometricEnabled

        swBiometric
            .setOnCheckedChangeListener {
                    _,
                    enabled ->

                prefs.biometricEnabled =
                    enabled

                Log.d(
                    "CommuteCompanion",
                    "Biometric preference updated"
                )
            }
    }

    private fun showSignOutDialog() {

        AlertDialog.Builder(this)
            .setTitle("Sign Out")
            .setMessage(
                "Are you sure you want to sign out?"
            )
            .setPositiveButton(
                "Sign Out"
            ) { _, _ ->

                signOut()
            }
            .setNegativeButton(
                "Cancel",
                null
            )
            .show()
    }

    private fun signOut() {

        auth.signOut()

        NotificationHelper
            .cancelNotifications(this)

        prefs.clearUserData()

        CoroutineScope(
            Dispatchers.Main
        ).launch {

            try {

                credentialManager
                    .clearCredentialState(
                        ClearCredentialStateRequest()
                    )

                Log.d(
                    "CommuteCompanion",
                    "User signed out from settings"
                )

            } catch (
                e: ClearCredentialException
            ) {

                Log.e(
                    "CommuteCompanion",
                    "Credential cleanup failed",
                    e
                )
            }

            openAccountEntry()
        }
    }

    private fun openAccountEntry() {

        val intent =
            Intent(
                this,
                AccountEntryActivity::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}