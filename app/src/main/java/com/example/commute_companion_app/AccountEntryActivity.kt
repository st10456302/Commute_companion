
package com.example.commute_companion_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AccountEntryActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.applyLanguage(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_entry)

        findViewById<android.widget.Button>(R.id.btnCreateAccount).setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }

        findViewById<android.widget.Button>(R.id.btnSignIn).setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
    }
}
