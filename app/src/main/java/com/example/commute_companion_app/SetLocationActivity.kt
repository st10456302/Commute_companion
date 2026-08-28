package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.commute_companion_app.SetRouteActivity
import kotlin.jvm.java

class SetLocationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_location)

        findViewById<android.widget.ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<android.widget.Button>(R.id.btnSaveLocation).setOnClickListener {
            startActivity(Intent(this, SetRouteActivity::class.java))
        }
    }
}