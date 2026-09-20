package com.example.commute_companion_app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.commute_companion_app.api.CommuteRepository
import kotlinx.coroutines.launch

class ApiTestActivity : AppCompatActivity() {

    private lateinit var repository: CommuteRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        repository = CommuteRepository(
            AppPreferences(this)
        )

        lifecycleScope.launch {

            val healthResult = repository.checkApiHealth()

            healthResult.onSuccess { response ->
                Log.d(
                    "CommuteCompanionAPI",
                    "API health: ${response.status}, service: ${response.service}"
                )
            }

            healthResult.onFailure { exception ->
                Log.e(
                    "CommuteCompanionAPI",
                    "API health request failed",
                    exception
                )
            }

            var userResult = repository.getCurrentUser()

            if (userResult.isFailure) {
                Log.d(
                    "CommuteCompanionAPI",
                    "User profile not found. Creating profile..."
                )

                userResult = repository.createCurrentUser()
            }

            userResult.onSuccess { response ->
                Log.d(
                    "CommuteCompanionAPI",
                    "User profile synced. Database ID: ${response.id}, name: ${response.displayName}, language: ${response.preferredLanguage}"
                )
            }

            userResult.onFailure { exception ->
                Log.e(
                    "CommuteCompanionAPI",
                    "User profile request failed",
                    exception
                )
            }
        }
    }
}