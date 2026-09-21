package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.commute_companion_app.api.CommuteRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LanguageManager.applyLanguage(newBase)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_create_account)

        auth = FirebaseAuth.getInstance()

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirmPassword)
        val checkboxTerms = findViewById<CheckBox>(R.id.checkboxTerms)
        val btnSubmit = findViewById<Button>(R.id.btnCreateAccountSubmit)

        btnSubmit.setOnClickListener {

            val fullName =
                etFullName.text.toString().trim()

            val email =
                etEmail.text.toString().trim()

            val password =
                etPassword.text.toString()

            val confirmPassword =
                etConfirmPassword.text.toString()

            // --- Validation ---

            if (fullName.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter your full name.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

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
                    "Please enter a password.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(
                    this,
                    "Password must be at least 8 characters.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please confirm your password.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(
                    this,
                    "Passwords do not match.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (!checkboxTerms.isChecked) {
                Toast.makeText(
                    this,
                    "Please accept the Terms of Service and Privacy Policy.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            createFirebaseAccount(
                fullName = fullName,
                email = email,
                password = password
            )
        }
    }

    private fun createFirebaseAccount(
        fullName: String,
        email: String,
        password: String
    ) {

        lifecycleScope.launch {

            try {

                Log.d(
                    "CommuteCompanion",
                    "Creating Firebase email/password account..."
                )

                // Create the Firebase Authentication account.
                val result =
                    auth.createUserWithEmailAndPassword(
                        email,
                        password
                    ).await()

                val firebaseUser =
                    result.user
                        ?: throw Exception(
                            "Firebase account was created but no user was returned."
                        )

                // Store the user's display name in Firebase.
                val profileUpdate =
                    UserProfileChangeRequest.Builder()
                        .setDisplayName(fullName)
                        .build()

                firebaseUser
                    .updateProfile(profileUpdate)
                    .await()

                // Keep non-sensitive information locally.
                val prefs = AppPreferences(this@CreateAccountActivity)

                prefs.userName = fullName
                prefs.accountEmail = email

                Log.d(
                    "CommuteCompanion",
                    "Firebase account created successfully — " +
                            "uid='${firebaseUser.uid}', " +
                            "email='${firebaseUser.email}', " +
                            "displayName='${firebaseUser.displayName}'"
                )

                // Firebase has now authenticated the user, so an ID token
                // is available for authenticated REST API requests.
                syncUserWithApi()

            } catch (exception: Exception) {

                Log.e(
                    "CommuteCompanion",
                    "Firebase account creation failed.",
                    exception
                )

                val message =
                    when {
                        exception.message?.contains(
                            "EMAIL_EXISTS",
                            ignoreCase = true
                        ) == true ->
                            "An account with this email already exists."

                        exception.message?.contains(
                            "already in use",
                            ignoreCase = true
                        ) == true ->
                            "An account with this email already exists."

                        exception.message?.contains(
                            "password",
                            ignoreCase = true
                        ) == true ->
                            "The password does not meet Firebase requirements."

                        else ->
                            "Unable to create your account. Please try again."
                    }

                Toast.makeText(
                    this@CreateAccountActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

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
                    "New user synchronized successfully — " +
                            "databaseId='${userProfile.id}', " +
                            "firebaseUid='${userProfile.firebaseUid}', " +
                            "name='${userProfile.displayName}', " +
                            "email='${userProfile.email}'"
                )

                Toast.makeText(
                    this@CreateAccountActivity,
                    "Account created successfully.",
                    Toast.LENGTH_SHORT
                ).show()

                startActivity(
                    Intent(
                        this@CreateAccountActivity,
                        BiometricActivity::class.java
                    )
                )

                finish()
            }

            result.onFailure { exception ->

                Log.e(
                    "CommuteCompanionAPI",
                    "Firebase account created, but user synchronization failed.",
                    exception
                )

                Toast.makeText(
                    this@CreateAccountActivity,
                    "Account created, but your profile could not be synchronized. Please try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}