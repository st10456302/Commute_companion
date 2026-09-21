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
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
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
    private lateinit var swBiometric: SwitchCompat

    private var updatingNotificationSwitch =
        false

    private var updatingBiometricSwitch =
        false

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            prefs.notificationsEnabled =
                granted

            updatingNotificationSwitch =
                true

            swNotifications.isChecked =
                granted

            updatingNotificationSwitch =
                false

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

        setContentView(
            R.layout.activity_settings
        )

        prefs =
            AppPreferences(this)

        auth =
            FirebaseAuth.getInstance()

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
            }
    }

    private fun setupNotificationPreference() {

        swNotifications =
            findViewById(
                R.id.swNotifications
            )

        val permissionGranted =
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

        if (!permissionGranted) {
            prefs.notificationsEnabled =
                false
        }

        updatingNotificationSwitch =
            true

        swNotifications.isChecked =
            prefs.notificationsEnabled &&
                    permissionGranted

        updatingNotificationSwitch =
            false

        swNotifications
            .setOnCheckedChangeListener {
                    _,
                    enabled ->

                if (
                    updatingNotificationSwitch
                ) {
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

        prefs.notificationsEnabled =
            true

        NotificationHelper.showNotification(
            this,
            "Notifications Enabled",
            "Traffic, weather and commute alerts are enabled.",
            1000
        )
    }

    private fun disableNotifications() {

        prefs.notificationsEnabled =
            false

        NotificationHelper
            .cancelNotifications(this)

        Toast.makeText(
            this,
            "Notifications disabled.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setupBiometricPreference() {

        swBiometric =
            findViewById(
                R.id.swBiometric
            )

        updatingBiometricSwitch =
            true

        swBiometric.isChecked =
            prefs.biometricEnabled

        updatingBiometricSwitch =
            false

        swBiometric
            .setOnCheckedChangeListener {
                    _,
                    enabled ->

                if (
                    updatingBiometricSwitch
                ) {
                    return@setOnCheckedChangeListener
                }

                if (enabled) {

                    enableBiometricLogin()

                } else {

                    prefs.biometricEnabled =
                        false

                    Log.d(
                        "CommuteCompanion",
                        "Biometric login disabled"
                    )
                }
            }
    }

    private fun enableBiometricLogin() {

        val biometricManager =
            BiometricManager.from(this)

        val result =
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )

        when (result) {

            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {

                resetBiometricSwitch()

                Toast.makeText(
                    this,
                    "Set up a fingerprint or face on your device first.",
                    Toast.LENGTH_LONG
                ).show()
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {

                resetBiometricSwitch()

                Toast.makeText(
                    this,
                    "This device does not support biometric authentication.",
                    Toast.LENGTH_LONG
                ).show()
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {

                resetBiometricSwitch()

                Toast.makeText(
                    this,
                    "Biometric authentication is currently unavailable.",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {

                resetBiometricSwitch()

                Toast.makeText(
                    this,
                    "Biometric authentication is unavailable.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showBiometricPrompt() {

        val executor =
            ContextCompat.getMainExecutor(this)

        val biometricPrompt =
            BiometricPrompt(
                this,
                executor,
                object :
                    BiometricPrompt.AuthenticationCallback() {

                    override fun onAuthenticationSucceeded(
                        result: BiometricPrompt.AuthenticationResult
                    ) {
                        super.onAuthenticationSucceeded(
                            result
                        )

                        prefs.biometricEnabled =
                            true

                        updatingBiometricSwitch =
                            true

                        swBiometric.isChecked =
                            true

                        updatingBiometricSwitch =
                            false

                        Toast.makeText(
                            this@SettingsActivity,
                            "Biometric login enabled.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()

                        Toast.makeText(
                            this@SettingsActivity,
                            "Biometric not recognised.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onAuthenticationError(
                        errorCode: Int,
                        errString: CharSequence
                    ) {
                        super.onAuthenticationError(
                            errorCode,
                            errString
                        )

                        resetBiometricSwitch()
                    }
                }
            )

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(
                    "Enable Biometric Login"
                )
                .setSubtitle(
                    "Verify your fingerprint or face"
                )
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_WEAK
                )
                .setNegativeButtonText(
                    "Cancel"
                )
                .build()

        biometricPrompt.authenticate(
            promptInfo
        )
    }

    private fun resetBiometricSwitch() {

        prefs.biometricEnabled =
            false

        updatingBiometricSwitch =
            true

        swBiometric.isChecked =
            false

        updatingBiometricSwitch =
            false
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