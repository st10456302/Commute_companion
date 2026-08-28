package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.commute_companion_app.SetLocationActivity

import kotlin.jvm.java

class LocationAccessActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location_access)

        val goToHome = { startActivity(Intent(this, HomeActivity::class.java)) }
        findViewById<android.widget.LinearLayout>(R.id.btnAllowLocation).setOnClickListener { goToHome() }
        findViewById<android.widget.Button>(R.id.btnManualLocation).setOnClickListener {
            startActivity(Intent(this, SetLocationActivity::class.java))
        }
        findViewById<android.widget.TextView>(R.id.tvNotNow).setOnClickListener { goToHome() }
    }
}