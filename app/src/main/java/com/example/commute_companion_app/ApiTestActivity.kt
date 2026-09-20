package com.example.commute_companion_app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.commute_companion_app.api.CommuteRepository
import kotlinx.coroutines.launch

class ApiTestActivity : AppCompatActivity() {

    private val repository = CommuteRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

            val userResult = repository.getCurrentUser()

            userResult.onSuccess { response ->
                Log.d(
                    "CommuteCompanionAPI",
                    "Firebase authentication successful. UID: ${response.firebaseUid}"
                )
            }

            userResult.onFailure { exception ->
                Log.e(
                    "CommuteCompanionAPI",
                    "Authenticated API request failed",
                    exception
                )
            }
        }
    }
}