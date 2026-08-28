package com.example.commute_companion_app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.commute_companion_app.R

class ResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<android.widget.LinearLayout>(R.id.btnReturnSignIn).setOnClickListener { finish() }
    }
}