package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.commute_companion_app.NotificationPermissionActivity
import kotlin.jvm.java

class CreateAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        findViewById<android.widget.Button>(R.id.btnCreateAccountSubmit).setOnClickListener {
            startActivity(Intent(this, NotificationPermissionActivity::class.java))
        }
    }
}