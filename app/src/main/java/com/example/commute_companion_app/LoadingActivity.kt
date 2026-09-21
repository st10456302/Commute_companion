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

        val prefs =
            AppPreferences(this)

        val auth =
            FirebaseAuth.getInstance()

        Handler(
            Looper.getMainLooper()
        ).postDelayed({

            val intent =
                when {

                    auth.currentUser != null &&
                            prefs.onboardingComplete &&
                            prefs.biometricEnabled -> {

                        Log.d(
                            "CommuteCompanion",
                            "Returning user requires biometric authentication"
                        )

                        Intent(
                            this,
                            BiometricActivity::class.java
                        ).apply {

                            putExtra(
                                BiometricActivity.EXTRA_MODE,
                                BiometricActivity.MODE_AUTHENTICATE
                            )
                        }
                    }

                    auth.currentUser != null &&
                            prefs.onboardingComplete -> {

                        Log.d(
                            "CommuteCompanion",
                            "Authenticated user opening Home"
                        )

                        Intent(
                            this,
                            HomeActivity::class.java
                        )
                    }

                    auth.currentUser != null -> {

                        Log.d(
                            "CommuteCompanion",
                            "Authenticated user continuing onboarding"
                        )

                        Intent(
                            this,
                            BiometricActivity::class.java
                        ).apply {

                            putExtra(
                                BiometricActivity.EXTRA_MODE,
                                BiometricActivity.MODE_SETUP
                            )
                        }
                    }

                    prefs.onboardingComplete -> {

                        Log.d(
                            "CommuteCompanion",
                            "Returning user needs sign in"
                        )

                        Intent(
                            this,
                            AccountEntryActivity::class.java
                        )
                    }

                    else -> {

                        Log.d(
                            "CommuteCompanion",
                            "First-time user opening Welcome"
                        )

                        Intent(
                            this,
                            WelcomeActivity::class.java
                        )
                    }
                }

            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()

        }, 2000)
    }
}