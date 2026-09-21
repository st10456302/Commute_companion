package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth =
            FirebaseAuth.getInstance()

        credentialManager =
            CredentialManager.create(this)

        val etEmail =
            findViewById<EditText>(
                R.id.etEmail
            )

        val etPassword =
            findViewById<EditText>(
                R.id.etPassword
            )

        val btnSignIn =
            findViewById<Button>(
                R.id.btnSignInSubmit
            )

        findViewById<TextView>(
            R.id.tvForgotPassword
        ).setOnClickListener {

            startActivity(
                Intent(
                    this,
                    ResetPasswordActivity::class.java
                )
            )
        }

        btnSignIn.setOnClickListener {

            val email =
                etEmail.text
                    .toString()
                    .trim()

            val password =
                etPassword.text
                    .toString()

            if (email.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please enter your email address.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (
                !Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()
            ) {

                Toast.makeText(
                    this,
                    "Please enter a valid email address.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (password.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please enter your password.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            btnSignIn.isEnabled =
                false

            auth.signInWithEmailAndPassword(
                email,
                password
            ).addOnCompleteListener { task ->

                btnSignIn.isEnabled =
                    true

                if (task.isSuccessful) {

                    saveUserDetails()
                    openNextScreen()

                } else {

                    val message =
                        if (
                            task.exception is
                                    FirebaseNetworkException
                        ) {
                            "Please check your internet connection and try again."
                        } else {
                            "Incorrect email or password."
                        }

                    Log.e(
                        "CommuteCompanion",
                        "Firebase sign in failed",
                        task.exception
                    )

                    Toast.makeText(
                        this,
                        message,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        findViewById<Button>(
            R.id.btnGoogleSignIn
        ).setOnClickListener {

            signInWithGoogle()
        }

        findViewById<LinearLayout>(
            R.id.btnBiometric
        ).setOnClickListener {

            openBiometricLogin()
        }
    }

    private fun openBiometricLogin() {

        val prefs =
            AppPreferences(this)

        if (!prefs.biometricEnabled) {

            Toast.makeText(
                this,
                "Biometric login has not been enabled yet.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        if (auth.currentUser == null) {

            Toast.makeText(
                this,
                "Sign in with your password or Google first to restore your session.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val intent =
            Intent(
                this,
                BiometricActivity::class.java
            )

        intent.putExtra(
            BiometricActivity.EXTRA_MODE,
            BiometricActivity.MODE_AUTHENTICATE
        )

        startActivity(intent)
    }

    private fun signInWithGoogle() {

        val googleOption =
            GetSignInWithGoogleOption.Builder(
                getString(
                    R.string.default_web_client_id
                )
            ).build()

        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(
                    googleOption
                )
                .build()

        CoroutineScope(
            Dispatchers.Main
        ).launch {

            try {

                val result =
                    credentialManager.getCredential(
                        context =
                            this@SignInActivity,
                        request =
                            request
                    )

                val credential =
                    result.credential

                if (
                    credential is CustomCredential &&
                    credential.type ==
                    GoogleIdTokenCredential
                        .TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {

                    val googleCredential =
                        GoogleIdTokenCredential
                            .createFrom(
                                credential.data
                            )

                    authenticateGoogleUser(
                        googleCredential.idToken
                    )

                } else {

                    Toast.makeText(
                        this@SignInActivity,
                        "Google sign in could not be completed.",
                        Toast.LENGTH_LONG
                    ).show()
                }

            } catch (
                e: GetCredentialException
            ) {

                Log.e(
                    "CommuteCompanion",
                    "Google sign in cancelled or unavailable",
                    e
                )

                Toast.makeText(
                    this@SignInActivity,
                    "Google sign in was cancelled or unavailable.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun authenticateGoogleUser(
        idToken: String
    ) {

        val credential =
            GoogleAuthProvider
                .getCredential(
                    idToken,
                    null
                )

        auth.signInWithCredential(
            credential
        ).addOnCompleteListener { task ->

            if (task.isSuccessful) {

                Log.d(
                    "CommuteCompanion",
                    "Google sign in successful"
                )

                saveUserDetails()
                openNextScreen()

            } else {

                Log.e(
                    "CommuteCompanion",
                    "Google Firebase sign in failed",
                    task.exception
                )

                Toast.makeText(
                    this,
                    "Google sign in failed. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun saveUserDetails() {

        val user =
            auth.currentUser
                ?: return

        val prefs =
            AppPreferences(this)

        prefs.accountEmail =
            user.email ?: ""

        if (
            !user.displayName
                .isNullOrBlank()
        ) {

            prefs.userName =
                user.displayName ?: ""
        }
    }

    private fun openNextScreen() {

        val prefs =
            AppPreferences(this)

        val intent =
            if (
                prefs.onboardingComplete
            ) {

                Intent(
                    this,
                    HomeActivity::class.java
                )

            } else {

                Intent(
                    this,
                    BiometricActivity::class.java
                ).apply {

                    putExtra(
                        BiometricActivity.EXTRA_MODE,
                        BiometricActivity.MODE_SETUP
                    )
                }
            }

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
    }
}