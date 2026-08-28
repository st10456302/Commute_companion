package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.commute_companion_app.HomeActivity

import kotlin.jvm.java

class BiometricActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric)

        val goHome = { startActivity(Intent(this, HomeActivity::class.java)) }
        findViewById<android.widget.Button>(R.id.btnEnableBiometric).setOnClickListener { goHome() }
        findViewById<android.widget.Button>(R.id.btnNotNow).setOnClickListener { goHome() }
    }
}