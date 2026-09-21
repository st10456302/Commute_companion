package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.example.commute_companion_app.api.CommuteRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var credentialManager: CredentialManager

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LanguageManager.applyLanguage(newBase)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        credentialManager =
            CredentialManager.create(this)

        val etEmail =
            findViewById<EditText>(R.id.etEmail)

        val etPassword =
            findViewById<EditText>(R.id.etPassword)

        val emailInputContainer =
            findViewById<LinearLayout>(R.id.emailInputContainer)

        val passwordInputContainer =
            findViewById<LinearLayout>(R.id.passwordInputContainer)

        val emailIcon =
            findViewById<ImageView>(R.id.ivEmailIcon)

        val passwordIcon =
            findViewById<ImageView>(R.id.ivPasswordIcon)

        val passwordLabel =
            findViewById<TextView>(R.id.tvPasswordLabel)

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

        // Email/password Firebase sign-in.
        findViewById<Button>(
            R.id.btnSignInSubmit
        ).setOnClickListener {

            val email =
                etEmail.text.toString().trim()

            val password =
                etPassword.text.toString()

            // Reset previous validation state.
            clearValidationErrors(
                emailInputContainer = emailInputContainer,
                passwordInputContainer = passwordInputContainer,
                emailIcon = emailIcon,
                passwordIcon = passwordIcon,
                passwordLabel = passwordLabel
            )

            // --- Validation ---

            if (email.isEmpty()) {

                showEmailError(
                    emailInputContainer,
                    emailIcon
                )

                Toast.makeText(
                    this,
                    "Please enter your email address.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

                showEmailError(
                    emailInputContainer,
                    emailIcon
                )

                Toast.makeText(
                    this,
                    "Please enter a valid email address.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            if (password.isEmpty()) {

                showPasswordError(
                    passwordInputContainer,
                    passwordIcon,
                    passwordLabel
                )

                Toast.makeText(
                    this,
                    "Please enter your password.",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            signInWithEmailPassword(
                email = email,
                password = password
            )
        }

        // Google SSO.
        findViewById<Button>(
            R.id.btnGoogleSignIn
        ).setOnClickListener {
            startGoogleSignIn()
        }

        // Existing biometric sign-in.
        findViewById<LinearLayout>(
            R.id.btnBiometric
        ).setOnClickListener {
            startActivity(
                Intent(
                    this,
                    BiometricActivity::class.java
                )
            )
        }
    }

    private fun showEmailError(
        emailInputContainer: LinearLayout,
        emailIcon: ImageView
    ) {
        emailInputContainer.setBackgroundResource(
            R.drawable.bg_input_error
        )

        emailIcon.setColorFilter(
            getColor(R.color.red_error)
        )
    }

    private fun showPasswordError(
        passwordInputContainer: LinearLayout,
        passwordIcon: ImageView,
        passwordLabel: TextView
    ) {
        passwordInputContainer.setBackgroundResource(
            R.drawable.bg_input_error
        )

        passwordIcon.setColorFilter(
            getColor(R.color.red_error)
        )

        passwordLabel.setTextColor(
            getColor(R.color.red_error)
        )
    }

    private fun clearValidationErrors(
        emailInputContainer: LinearLayout,
        passwordInputContainer: LinearLayout,
        emailIcon: ImageView,
        passwordIcon: ImageView,
        passwordLabel: TextView
    ) {
        emailInputContainer.setBackgroundResource(
            R.drawable.bg_input_field
        )

        passwordInputContainer.setBackgroundResource(
            R.drawable.bg_input_field
        )

        emailIcon.setColorFilter(
            getColor(R.color.text_secondary)
        )

        passwordIcon.setColorFilter(
            getColor(R.color.text_secondary)
        )

        passwordLabel.setTextColor(
            getColor(R.color.text_primary)
        )
    }

    /**
     * Signs an existing user into Firebase using
     * their registered email address and password.
     */
    private fun signInWithEmailPassword(
        email: String,
        password: String
    ) {

        lifecycleScope.launch {

            try {

                Log.d(
                    "CommuteCompanion",
                    "Signing in with Firebase email/password..."
                )

                val result =
                    auth.signInWithEmailAndPassword(
                        email,
                        password
                    ).await()

                val user =
                    result.user
                        ?: throw Exception(
                            "Firebase sign-in succeeded but no user was returned."
                        )

                val prefs =
                    AppPreferences(this@SignInActivity)

                prefs.accountEmail =
                    user.email ?: email

                user.displayName?.let { name ->
                    prefs.userName = name
                }

                Log.d(
                    "CommuteCompanion",
                    "Firebase email/password sign-in successful — " +
                            "uid='${user.uid}', " +
                            "email='${user.email}', " +
                            "displayName='${user.displayName}'"
                )

                syncUserWithApi()

            } catch (exception: Exception) {

                Log.e(
                    "CommuteCompanion",
                    "Firebase email/password sign-in failed.",
                    exception
                )

                Toast.makeText(
                    this@SignInActivity,
                    "Incorrect email or password.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    /**
     * Starts the Google SSO account selection flow.
     */
    private fun startGoogleSignIn() {

        val googleIdOption =
            GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(
                    getString(
                        R.string.default_web_client_id
                    )
                )
                .setAutoSelectEnabled(false)
                .build()

        val request =
            GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

        lifecycleScope.launch {

            try {

                val result =
                    credentialManager.getCredential(
                        context = this@SignInActivity,
                        request = request
                    )

                handleGoogleCredential(
                    result.credential
                )

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
    private fun handleGoogleCredential(
        credential: androidx.credentials.Credential
    ) {

        if (
            credential is CustomCredential &&
            credential.type ==
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {

            try {

                val googleIdTokenCredential =
                    GoogleIdTokenCredential.createFrom(
                        credential.data
                    )

                firebaseAuthWithGoogle(
                    googleIdTokenCredential.idToken
                )

            } catch (
                exception: GoogleIdTokenParsingException
            ) {

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
     * Exchanges the Google ID token for a Firebase credential,
     * then synchronizes the authenticated user with the REST API.
     */
    private fun firebaseAuthWithGoogle(
        idToken: String
    ) {

        val firebaseCredential =
            GoogleAuthProvider.getCredential(
                idToken,
                null
            )

        auth.signInWithCredential(
            firebaseCredential
        ).addOnCompleteListener(this) { task ->

            if (task.isSuccessful) {

                val user =
                    auth.currentUser

                val prefs =
                    AppPreferences(this)

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

                syncUserWithApi()

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

    /**
     * Creates or retrieves the current user's application profile
     * through the authenticated REST API.
     */
    private fun syncUserWithApi() {

        val repository =
            CommuteRepository(
                AppPreferences(this)
            )

        lifecycleScope.launch {

            val result =
                repository.syncCurrentUser()

            result.onSuccess { userProfile ->

                Log.d(
                    "CommuteCompanionAPI",
                    "User synchronized successfully — " +
                            "databaseId='${userProfile.id}', " +
                            "firebaseUid='${userProfile.firebaseUid}', " +
                            "name='${userProfile.displayName}', " +
                            "email='${userProfile.email}', " +
                            "language='${userProfile.preferredLanguage}'"
                )

                Toast.makeText(
                    this@SignInActivity,
                    "Sign-in successful.",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(
                    Intent(
                        this@SignInActivity,
                        BiometricActivity::class.java
                    )
                )

                finish()
            }

            result.onFailure { exception ->

                Log.e(
                    "CommuteCompanionAPI",
                    "User synchronization failed",
                    exception
                )

                Toast.makeText(
                    this@SignInActivity,
                    "Signed in, but your profile could not be synchronized.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}