package com.example.commute_companion_app.api

import com.example.commute_companion_app.AppPreferences
import com.example.commute_companion_app.FirebaseTokenProvider

class CommuteRepository(
    private val appPreferences: AppPreferences
) {

    private val api = ApiClient.service
    private val tokenProvider = FirebaseTokenProvider()

    suspend fun checkApiHealth(): Result<HealthResponse> {
        return try {
            val response = api.getHealth()

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception("API returned HTTP ${response.code()}")
                )
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getCurrentUser(): Result<UserResponse> {
        val token = tokenProvider.getIdToken()
            ?: return Result.failure(
                Exception("No Firebase ID token available.")
            )

        return try {
            val response = api.getCurrentUser(
                authorization = "Bearer $token"
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception("API returned HTTP ${response.code()}")
                )
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun createCurrentUser(): Result<UserResponse> {
        val token = tokenProvider.getIdToken()
            ?: return Result.failure(
                Exception("No Firebase ID token available.")
            )

        val request = CreateUserRequest(
            email = tokenProvider.getEmail(),
            displayName = tokenProvider.getDisplayName(),
            preferredLanguage = appPreferences.selectedLanguage
        )

        return try {
            val response = api.createCurrentUser(
                authorization = "Bearer $token",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(
                    Exception("API returned HTTP ${response.code()}")
                )
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun syncCurrentUser(): Result<UserResponse> {
        val existingUser = getCurrentUser()

        if (existingUser.isSuccess) {
            return existingUser
        }

        return createCurrentUser()
    }
}