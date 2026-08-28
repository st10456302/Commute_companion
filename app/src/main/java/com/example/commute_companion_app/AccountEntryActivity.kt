package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.commute_companion_app.LanguageSelectionActivity
import com.example.commute_companion_app.SignInActivity
import com.example.commute_companion_app.R
import kotlin.jvm.java

class AccountEntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_entry)

        findViewById<android.widget.Button>(R.id.btnCreateAccount).setOnClickListener {
            startActivity(Intent(this, LanguageSelectionActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.btnSignIn).setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}