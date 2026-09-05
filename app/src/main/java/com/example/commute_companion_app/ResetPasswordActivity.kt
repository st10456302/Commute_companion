package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        val etEmail = findViewById<EditText>(R.id.etEmail)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<LinearLayout>(R.id.btnReturnSignIn).setOnClickListener { finish() }

        findViewById<LinearLayout>(R.id.btnSendResetLink).setOnClickListener {
            val email = etEmail.text.toString().trim()

            // --- Validation ---
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- Simulated success (no real backend/email service in this prototype) ---
            Toast.makeText(
                this,
                "If an account exists for $email, a reset link has been sent.",
                Toast.LENGTH_LONG
            ).show()

            // Return to Sign In without stacking duplicate Reset Password / Sign In
            // instances on the back stack.
            val intent = Intent(this, SignInActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }
}