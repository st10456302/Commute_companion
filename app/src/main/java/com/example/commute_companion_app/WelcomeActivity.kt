package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.commute_companion_app.AccountEntryActivity
import com.example.commute_companion_app.R
import com.example.commute_companion_app.SignInActivity
import kotlin.jvm.java

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        findViewById<android.widget.Button>(R.id.btnGetStarted).setOnClickListener {
            startActivity(Intent(this, AccountEntryActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnAlreadyHaveAccount).setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}