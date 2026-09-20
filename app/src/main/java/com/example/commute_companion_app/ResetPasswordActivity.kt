package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        auth = FirebaseAuth.getInstance()

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnSendResetLink = findViewById<LinearLayout>(R.id.btnSendResetLink)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        findViewById<LinearLayout>(R.id.btnReturnSignIn).setOnClickListener {
            finish()
        }

        btnSendResetLink.setOnClickListener {
            val email = etEmail.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSendResetLink.isEnabled = false

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    btnSendResetLink.isEnabled = true

                    if (task.isSuccessful) {
                        Log.d("CommuteCompanion", "Password reset email requested")

                        Toast.makeText(
                            this,
                            "If an account exists for this email, a reset link has been sent.",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(this, SignInActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        finish()
                    } else {
                        val message =
                            if (task.exception is FirebaseNetworkException) {
                                "Please check your internet connection and try again."
                            } else {
                                "Could not send the reset email. Please try again."
                            }

                        Log.e(
                            "CommuteCompanion",
                            "Password reset failed",
                            task.exception
                        )

                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}