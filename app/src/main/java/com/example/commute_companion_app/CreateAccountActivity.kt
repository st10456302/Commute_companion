package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.UserProfileChangeRequest

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

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
            val fullName = etFullName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (fullName.isEmpty()) {
                Toast.makeText(this, "Please enter your full name.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter a password.", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this, "Please confirm your password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
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

            btnSubmit.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    btnSubmit.isEnabled = true

                    if (task.isSuccessful) {
                        val prefs = AppPreferences(this)
                        prefs.userName = fullName
                        prefs.accountEmail = email

                        val profileUpdate = UserProfileChangeRequest.Builder()
                            .setDisplayName(fullName)
                            .build()

                        auth.currentUser?.updateProfile(profileUpdate)

                        Log.d("CommuteCompanion", "Firebase account created")

                        Toast.makeText(
                            this,
                            "Account created successfully.",
                            Toast.LENGTH_SHORT
                        ).show()

                        val intent = Intent(this, BiometricActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        val message = when (task.exception) {
                            is FirebaseAuthUserCollisionException ->
                                "An account already exists with this email."

                            is FirebaseAuthWeakPasswordException ->
                                "Please choose a stronger password."

                            is FirebaseAuthInvalidCredentialsException ->
                                "Please enter a valid email address."

                            is FirebaseNetworkException ->
                                "Please check your internet connection and try again."

                            else ->
                                "Could not create your account. Please try again."
                        }

                        Log.e(
                            "CommuteCompanion",
                            "Firebase account creation failed",
                            task.exception
                        )

                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}