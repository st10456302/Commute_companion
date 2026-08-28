package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.commute_companion_app.HomeActivity

class SetRouteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_route)

        findViewById<android.widget.Button>(R.id.btnSaveRoute).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }
}