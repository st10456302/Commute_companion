package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.commute_companion_app.LiveDashboardActivity
import com.example.commute_companion_app.OfflineDashboardActivity
import com.example.commute_companion_app.SetLocationActivity
import com.example.commute_companion_app.R
import kotlin.jvm.java

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        findViewById<android.widget.LinearLayout>(R.id.navHome).setOnClickListener { }
        findViewById<android.widget.LinearLayout>(R.id.navAlerts).setOnClickListener {
            startActivity(Intent(this, OfflineDashboardActivity::class.java))
        }
        findViewById<android.widget.LinearLayout>(R.id.navLocations).setOnClickListener {
            startActivity(Intent(this, SetLocationActivity::class.java))
        }
        findViewById<android.widget.LinearLayout>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, LiveDashboardActivity::class.java))
        }
    }
}