package com.example.commute_companion_app.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CommuteApiService {

    @GET("api/health")
    suspend fun getHealth(): Response<HealthResponse>

    @GET("api/users/me")
    suspend fun getCurrentUser(
        @Header("Authorization") authorization: String
    ): Response<UserResponse>

    @POST("api/users/me")
    suspend fun createCurrentUser(
        @Header("Authorization") authorization: String,
        @Body request: CreateUserRequest
    ): Response<UserResponse>

    @GET("api/locations")
    suspend fun getLocations(
        @Header("Authorization") authorization: String
    ): Response<List<SavedLocationResponse>>

    @POST("api/locations")
    suspend fun createLocation(
        @Header("Authorization") authorization: String,
        @Body request: CreateLocationRequest
    ): Response<SavedLocationResponse>

    @DELETE("api/locations/{id}")
    suspend fun deleteLocation(
        @Header("Authorization") authorization: String,
        @Path("id") id: Int
    ): Response<Unit>

    @GET("api/notificationpreferences")
    suspend fun getNotificationPreferences(
        @Header("Authorization") authorization: String
    ): Response<NotificationPreferencesResponse>

    @POST("api/notificationpreferences")
    suspend fun createNotificationPreferences(
        @Header("Authorization") authorization: String,
        @Body request: NotificationPreferencesRequest
    ): Response<NotificationPreferencesResponse>

    @PUT("api/notificationpreferences")
    suspend fun updateNotificationPreferences(
        @Header("Authorization") authorization: String,
        @Body request: NotificationPreferencesRequest
    ): Response<NotificationPreferencesResponse>
}

data class HealthResponse(
    val status: String,
    val service: String,
    val timestamp: String
)

data class UserResponse(
    val id: Int,
    val firebaseUid: String,
    val email: String,
    val displayName: String,
    val preferredLanguage: String,
    val createdAt: String,
    val updatedAt: String
)

data class CreateUserRequest(
    val email: String?,
    val displayName: String?,
    val preferredLanguage: String?
)

data class SavedLocationResponse(
    val id: Int,
    val userId: String,
    val label: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?,
    val createdAt: String
)

data class CreateLocationRequest(
    val label: String,
    val address: String,
    val latitude: Double?,
    val longitude: Double?
)

data class NotificationPreferencesResponse(
    val id: Int,
    val userId: String,
    val notificationsEnabled: Boolean,
    val trafficAlerts: Boolean,
    val weatherAlerts: Boolean,
    val loadSheddingAlerts: Boolean,
    val updatedAt: String
)

data class NotificationPreferencesRequest(
    val notificationsEnabled: Boolean,
    val trafficAlerts: Boolean,
    val weatherAlerts: Boolean,
    val loadSheddingAlerts: Boolean
)
