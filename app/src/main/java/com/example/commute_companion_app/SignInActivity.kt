package com.example.commute_companion_app

import android.content.Context
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
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Firebase Authentication instance.
        auth = FirebaseAuth.getInstance()

        // Credential Manager handles the Google SSO account selection.
        credentialManager = CredentialManager.create(this)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        // Existing email/password prototype sign-in.
        findViewById<Button>(R.id.btnSignInSubmit).setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            // --- Validation ---
            if (email.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter your email address.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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

            // Prototype email/password sign-in.
            // Password is intentionally NEVER stored or logged.
            val prefs = AppPreferences(this)
            prefs.accountEmail = email

            Log.d(
                "CommuteCompanion",
                "Email sign in successful — accountEmail='${prefs.accountEmail}'"
            )

            startActivity(Intent(this, BiometricActivity::class.java))
        }

        // Google SSO.
        findViewById<Button>(R.id.btnGoogleSignIn).setOnClickListener {
            startGoogleSignIn()
        }

        // Existing biometric sign-in.
        findViewById<LinearLayout>(R.id.btnBiometric).setOnClickListener {
            startActivity(Intent(this, BiometricActivity::class.java))
        }
    }

    /**
     * Starts the Google SSO account selection flow.
     */
    private fun startGoogleSignIn() {

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(getString(R.string.default_web_client_id))
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = credentialManager.getCredential(
                    context = this@SignInActivity,
                    request = request
                )

                handleGoogleCredential(result.credential)

            } catch (exception: Exception) {

                Log.e(
                    "CommuteCompanion",
                    "Google sign-in failed",
                    exception
                )

                Toast.makeText(
                    this@SignInActivity,
                    "Google sign-in was cancelled or failed.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Processes the credential returned by Credential Manager
     * and signs the user into Firebase Authentication.
     */
    private fun handleGoogleCredential(credential: androidx.credentials.Credential) {

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            try {

                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)

                firebaseAuthWithGoogle(
                    googleIdTokenCredential.idToken
                )

            } catch (exception: GoogleIdTokenParsingException) {

                Log.e(
                    "CommuteCompanion",
                    "Invalid Google ID token",
                    exception
                )

                Toast.makeText(
                    this,
                    "Unable to process Google sign-in.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        } else {

            Log.e(
                "CommuteCompanion",
                "Unexpected credential type received."
            )

            Toast.makeText(
                this,
                "Google sign-in returned an unsupported credential.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    /**
     * Exchanges the Google ID token for a Firebase credential.
     */
    private fun firebaseAuthWithGoogle(idToken: String) {

        val firebaseCredential =
            GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {

                    val user = auth.currentUser

                    val prefs = AppPreferences(this)

                    user?.displayName?.let { name ->
                        prefs.userName = name
                    }

                    user?.email?.let { email ->
                        prefs.accountEmail = email
                    }

                    Log.d(
                        "CommuteCompanion",
                        "Google SSO successful — " +
                                "uid='${user?.uid}', " +
                                "email='${user?.email}'"
                    )

                    Toast.makeText(
                        this,
                        "Google sign-in successful.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Continue through the app's existing authentication flow.
                    startActivity(
                        Intent(
                            this,
                            BiometricActivity::class.java
                        )
                    )

                    finish()

                } else {

                    Log.e(
                        "CommuteCompanion",
                        "Firebase Google authentication failed",
                        task.exception
                    )

                    Toast.makeText(
                        this,
                        "Google sign-in failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}