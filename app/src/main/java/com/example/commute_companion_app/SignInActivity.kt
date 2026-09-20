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
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth

class SignInActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnSignIn = findViewById<Button>(R.id.btnSignInSubmit)

        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        btnSignIn.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "Please enter your password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSignIn.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    btnSignIn.isEnabled = true

                    if (task.isSuccessful) {
                        val prefs = AppPreferences(this)
                        val user = auth.currentUser

                        prefs.accountEmail = user?.email ?: email

                        if (prefs.userName.isEmpty()) {
                            prefs.userName = user?.displayName ?: ""
                        }

                        Log.d("CommuteCompanion", "Firebase sign in successful")

                        val destination =
                            if (prefs.onboardingComplete) {
                                HomeActivity::class.java
                            } else {
                                BiometricActivity::class.java
                            }

                        val intent = Intent(this, destination)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    } else {
                        val message =
                            if (task.exception is FirebaseNetworkException) {
                                "Please check your internet connection and try again."
                            } else {
                                "Incorrect email or password."
                            }

                        Log.e(
                            "CommuteCompanion",
                            "Firebase sign in failed",
                            task.exception
                        )

                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
        }

        findViewById<LinearLayout>(R.id.btnBiometric).setOnClickListener {
            startActivity(Intent(this, BiometricActivity::class.java))
        }
    }
}