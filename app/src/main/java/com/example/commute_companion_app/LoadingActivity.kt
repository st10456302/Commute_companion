package com.example.commute_companion_app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val prefs = AppPreferences(this)
        val firebaseUser = FirebaseAuth.getInstance().currentUser

        Log.d(
            "CommuteCompanion",
            "Loading started — " +
                    "onboardingComplete=${prefs.onboardingComplete}, " +
                    "firebaseSignedIn=${firebaseUser != null}, " +
                    "firebaseUid='${firebaseUser?.uid}'"
        )

        Handler(Looper.getMainLooper()).postDelayed({

            if (firebaseUser != null) {

                // Firebase still has an authenticated user.
                // Skip onboarding and open the main application.
                Log.d(
                    "CommuteCompanion",
                    "Returning authenticated user — opening HomeActivity"
                )

                val intent =
                    Intent(
                        this,
                        HomeActivity::class.java
                    ).apply {
                        flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or
                                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }

                startActivity(intent)

            } else {

                // No Firebase user is currently signed in.
                // Start the normal authentication/onboarding flow.
                Log.d(
                    "CommuteCompanion",
                    "No authenticated Firebase user — starting WelcomeActivity"
                )

                startActivity(
                    Intent(
                        this,
                        WelcomeActivity::class.java
                    )
                )
            }

            finish()

        }, 2000)
    }
}