package com.example.commute_companion_app.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface CommuteApiService {

    @GET("api/health")
    suspend fun getHealth(): Response<HealthResponse>

    @GET("api/auth/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): Response<CurrentUserResponse>
}

data class HealthResponse(
    val status: String,
    val service: String,
    val timestamp: String
)

data class CurrentUserResponse(
    val firebaseUid: String
)