package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth

class BiometricActivity : AppCompatActivity() {

    private lateinit var prefs: AppPreferences
    private lateinit var auth: FirebaseAuth

    private var biometricMode = MODE_SETUP

    companion object {
        const val EXTRA_MODE = "biometricMode"
        const val MODE_SETUP = "setup"
        const val MODE_AUTHENTICATE = "authenticate"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric)

        prefs = AppPreferences(this)
        auth = FirebaseAuth.getInstance()

        biometricMode =
            intent.getStringExtra(EXTRA_MODE)
                ?: MODE_SETUP

        val btnBiometric =
            findViewById<Button>(
                R.id.btnEnableBiometric
            )

        val btnNotNow =
            findViewById<Button>(
                R.id.btnNotNow
            )

        if (biometricMode == MODE_AUTHENTICATE) {

            btnBiometric.text =
                "Authenticate with Biometrics"

            btnNotNow.text =
                "Use another sign-in method"

            btnBiometric.setOnClickListener {
                showBiometricPrompt()
            }

            btnNotNow.setOnClickListener {
                useAlternativeSignIn()
            }

            showBiometricPrompt()

        } else {

            btnBiometric.setOnClickListener {
                showBiometricPrompt()
            }

            btnNotNow.setOnClickListener {

                prefs.biometricEnabled =
                    false

                continueOnboarding()
            }
        }
    }

    private fun showBiometricPrompt() {

        val biometricManager =
            BiometricManager.from(this)

        val result =
            biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )

        when (result) {

            BiometricManager.BIOMETRIC_SUCCESS -> {
                authenticateUser()
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {

                Toast.makeText(
                    this,
                    "No fingerprint or face is enrolled on this device.",
                    Toast.LENGTH_LONG
                ).show()

                if (
                    biometricMode ==
                    MODE_AUTHENTICATE
                ) {
                    useAlternativeSignIn()
                }
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {

                Toast.makeText(
                    this,
                    "This device does not support biometric authentication.",
                    Toast.LENGTH_LONG
                ).show()

                if (
                    biometricMode ==
                    MODE_AUTHENTICATE
                ) {
                    useAlternativeSignIn()
                }
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {

                Toast.makeText(
                    this,
                    "Biometric authentication is currently unavailable.",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {

                Toast.makeText(
                    this,
                    "Biometric authentication is unavailable.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun authenticateUser() {

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

                        Log.d(
                            "CommuteCompanion",
                            "Biometric authentication successful"
                        )

                        if (
                            biometricMode ==
                            MODE_SETUP
                        ) {

                            prefs.biometricEnabled =
                                true

                            Toast.makeText(
                                this@BiometricActivity,
                                "Biometric login enabled.",
                                Toast.LENGTH_SHORT
                            ).show()

                            continueOnboarding()

                        } else {

                            openHome()
                        }
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()

                        Toast.makeText(
                            this@BiometricActivity,
                            "Biometric not recognised. Try again.",
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

                        Log.d(
                            "CommuteCompanion",
                            "Biometric authentication ended: $errString"
                        )
                    }
                }
            )

        val promptInfo =
            BiometricPrompt.PromptInfo.Builder()
                .setTitle(
                    "Commute Companion"
                )
                .setSubtitle(
                    if (
                        biometricMode ==
                        MODE_SETUP
                    ) {
                        "Verify your biometric to enable quick login"
                    } else {
                        "Verify your identity to continue"
                    }
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

    private fun continueOnboarding() {

        startActivity(
            Intent(
                this,
                LocationAccessActivity::class.java
            )
        )
    }

    private fun openHome() {

        if (auth.currentUser == null) {

            useAlternativeSignIn()
            return
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

    private fun useAlternativeSignIn() {

        auth.signOut()

        val intent =
            Intent(
                this,
                SignInActivity::class.java
            )

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }
}