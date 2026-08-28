package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.commute_companion_app.SetLocationActivity

import kotlin.jvm.java

class LiveDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_live_dashboard)

        findViewById<android.widget.LinearLayout>(R.id.locationSelector).setOnClickListener {
            startActivity(Intent(this, SetLocationActivity::class.java))
        }
    }
}