package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.commute_companion_app.BiometricActivity
import com.example.commute_companion_app.HomeActivity
import com.example.commute_companion_app.R
import com.example.commute_companion_app.ResetPasswordActivity
import kotlin.jvm.java

class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        findViewById<android.widget.TextView>(R.id.tvForgotPassword).setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnSignInSubmit).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
        findViewById<android.widget.LinearLayout>(R.id.btnBiometric).setOnClickListener {
            startActivity(Intent(this, BiometricActivity::class.java))
        }
    }
}