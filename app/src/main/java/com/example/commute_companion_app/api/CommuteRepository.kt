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

    suspend fun getSavedLocations(): Result<List<SavedLocationResponse>> {
        val token = tokenProvider.getIdToken()
            ?: return Result.failure(
                Exception("No Firebase ID token available.")
            )

        return try {
            val response = api.getLocations(
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

    suspend fun createSavedLocation(
        label: String,
        address: String,
        latitude: Double?,
        longitude: Double?
    ): Result<SavedLocationResponse> {

        val token = tokenProvider.getIdToken()
            ?: return Result.failure(
                Exception("No Firebase ID token available.")
            )

        val request = CreateLocationRequest(
            label = label,
            address = address,
            latitude = latitude,
            longitude = longitude
        )

        return try {
            val response = api.createLocation(
                authorization = "Bearer $token",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage =
                    response.errorBody()?.string()
                        ?.takeIf { it.isNotBlank() }
                        ?: "API returned HTTP ${response.code()}"

                Result.failure(
                    Exception(
                        "API returned HTTP ${response.code()}: $errorMessage"
                    )
                )
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun createSavedRoute(
        name: String,
        destination: String
    ): Result<SavedRouteResponse> {

        val token = tokenProvider.getIdToken()
            ?: return Result.failure(
                Exception("No Firebase ID token available.")
            )

        val request = CreateRouteRequest(
            name = name,
            destination = destination
        )

        return try {
            val response = api.createRoute(
                authorization = "Bearer $token",
                request = request
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage =
                    response.errorBody()?.string()
                        ?.takeIf { it.isNotBlank() }
                        ?: "API returned HTTP ${response.code()}"

                Result.failure(
                    Exception(
                        "API returned HTTP ${response.code()}: $errorMessage"
                    )
                )
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun deleteSavedLocation(
        id: Int
    ): Result<Unit> {

        val token = tokenProvider.getIdToken()
            ?: return Result.failure(
                Exception("No Firebase ID token available.")
            )

        return try {
            val response = api.deleteLocation(
                authorization = "Bearer $token",
                id = id
            )

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(
                    Exception("API returned HTTP ${response.code()}")
                )
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getNotificationPreferences():
            Result<NotificationPreferencesResponse> {

        val token = tokenProvider.getIdToken()
            ?: return Result.failure(
                Exception("No Firebase ID token available.")
            )

        return try {
            val response = api.getNotificationPreferences(
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

    suspend fun createNotificationPreferences(
        notificationsEnabled: Boolean,
        trafficAlerts: Boolean,
        weatherAlerts: Boolean,
        loadSheddingAlerts: Boolean
    ): Result<NotificationPreferencesResponse> {

        val token = tokenProvider.getIdToken()
            ?: return Result.failure(
                Exception("No Firebase ID token available.")
            )

        val request = NotificationPreferencesRequest(
            notificationsEnabled = notificationsEnabled,
            trafficAlerts = trafficAlerts,
            weatherAlerts = weatherAlerts,
            loadSheddingAlerts = loadSheddingAlerts
        )

        return try {
            val response = api.createNotificationPreferences(
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

    suspend fun updateNotificationPreferences(
        notificationsEnabled: Boolean,
        trafficAlerts: Boolean,
        weatherAlerts: Boolean,
        loadSheddingAlerts: Boolean
    ): Result<NotificationPreferencesResponse> {

        val token = tokenProvider.getIdToken()
            ?: return Result.failure(
                Exception("No Firebase ID token available.")
            )

        val request = NotificationPreferencesRequest(
            notificationsEnabled = notificationsEnabled,
            trafficAlerts = trafficAlerts,
            weatherAlerts = weatherAlerts,
            loadSheddingAlerts = loadSheddingAlerts
        )

        return try {
            val response = api.updateNotificationPreferences(
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

    suspend fun getDashboard(
        locationId: Int
    ): Result<DashboardResponse> {

        val token = tokenProvider.getIdToken()
            ?: return Result.failure(
                Exception("No Firebase ID token available.")
            )

        return try {
            val response = api.getDashboard(
                authorization = "Bearer $token",
                locationId = locationId
            )

            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMessage =
                    response.errorBody()?.string()
                        ?.takeIf { it.isNotBlank() }
                        ?: "API returned HTTP ${response.code()}"

                Result.failure(
                    Exception(
                        "API returned HTTP ${response.code()}: $errorMessage"
                    )
                )
            }
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}