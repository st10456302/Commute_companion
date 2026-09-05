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

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        findViewById<TextView>(R.id.tvForgotPassword).setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }

        findViewById<Button>(R.id.btnSignInSubmit).setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()

            // --- Validation ---
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

            // --- Prototype "sign in": no real backend yet. ---
            // A valid-format email + non-empty password is treated as successful.
            // Password is intentionally NEVER stored or logged.
            val prefs = AppPreferences(this)
            prefs.accountEmail = email

            Log.d("CommuteCompanion", "Sign in successful — accountEmail='${prefs.accountEmail}'")

            startActivity(Intent(this, BiometricActivity::class.java))
        }

        findViewById<LinearLayout>(R.id.btnBiometric).setOnClickListener {
            startActivity(Intent(this, BiometricActivity::class.java))
        }
    }
}