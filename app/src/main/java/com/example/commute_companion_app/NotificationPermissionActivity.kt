package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.commute_companion_app.LocationAccessActivity
import com.example.commute_companion_app.R

import kotlin.jvm.java

class NotificationPermissionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_permission)

        findViewById<android.widget.Button>(R.id.btnEnableNotifications).setOnClickListener {
            startActivity(Intent(this, LocationAccessActivity::class.java))
        }
    }
}