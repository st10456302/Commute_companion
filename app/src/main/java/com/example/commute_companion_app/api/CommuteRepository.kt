package com.example.commute_companion_app.api

import com.example.commute_companion_app.FirebaseTokenProvider

class CommuteRepository {

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

    suspend fun getCurrentUser(): Result<CurrentUserResponse> {
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
}