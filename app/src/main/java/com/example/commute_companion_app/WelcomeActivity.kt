package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        findViewById<android.widget.Button>(R.id.btnGetStarted).setOnClickListener {
            startActivity(Intent(this, LanguageSelectionActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnAlreadyHaveAccount).setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}