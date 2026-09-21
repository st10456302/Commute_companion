package com.example.commute_companion_app

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(
            PREFS_NAME,
            Context.MODE_PRIVATE
        )

    var onboardingComplete: Boolean
        get() =
            prefs.getBoolean(
                KEY_ONBOARDING_COMPLETE,
                false
            )
        set(value) =
            prefs.edit()
                .putBoolean(
                    KEY_ONBOARDING_COMPLETE,
                    value
                )
                .apply()

    var selectedLanguage: String
        get() =
            prefs.getString(
                KEY_SELECTED_LANGUAGE,
                "English"
            ) ?: "English"
        set(value) =
            prefs.edit()
                .putString(
                    KEY_SELECTED_LANGUAGE,
                    value
                )
                .apply()

    var userName: String
        get() =
            prefs.getString(
                KEY_USER_NAME,
                ""
            ) ?: ""
        set(value) =
            prefs.edit()
                .putString(
                    KEY_USER_NAME,
                    value
                )
                .apply()

    var accountEmail: String
        get() =
            prefs.getString(
                KEY_ACCOUNT_EMAIL,
                ""
            ) ?: ""
        set(value) =
            prefs.edit()
                .putString(
                    KEY_ACCOUNT_EMAIL,
                    value
                )
                .apply()

    var biometricEnabled: Boolean
        get() =
            prefs.getBoolean(
                KEY_BIOMETRIC_ENABLED,
                false
            )
        set(value) =
            prefs.edit()
                .putBoolean(
                    KEY_BIOMETRIC_ENABLED,
                    value
                )
                .apply()

    var savedLocationAddress: String
        get() =
            prefs.getString(
                KEY_SAVED_LOCATION_ADDRESS,
                ""
            ) ?: ""
        set(value) =
            prefs.edit()
                .putString(
                    KEY_SAVED_LOCATION_ADDRESS,
                    value
                )
                .apply()

    var savedLocationLabel: String
        get() =
            prefs.getString(
                KEY_SAVED_LOCATION_LABEL,
                "Work"
            ) ?: "Work"
        set(value) =
            prefs.edit()
                .putString(
                    KEY_SAVED_LOCATION_LABEL,
                    value
                )
                .apply()

    var savedRouteName: String
        get() =
            prefs.getString(
                KEY_SAVED_ROUTE_NAME,
                ""
            ) ?: ""
        set(value) =
            prefs.edit()
                .putString(
                    KEY_SAVED_ROUTE_NAME,
                    value
                )
                .apply()

    var savedRouteDestination: String
        get() =
            prefs.getString(
                KEY_SAVED_ROUTE_DESTINATION,
                ""
            ) ?: ""
        set(value) =
            prefs.edit()
                .putString(
                    KEY_SAVED_ROUTE_DESTINATION,
                    value
                )
                .apply()

    var notificationsEnabled: Boolean
        get() =
            prefs.getBoolean(
                KEY_NOTIFICATIONS_ENABLED,
                false
            )
        set(value) =
            prefs.edit()
                .putBoolean(
                    KEY_NOTIFICATIONS_ENABLED,
                    value
                )
                .apply()

    fun clearUserData() {
        prefs.edit()
            .remove(KEY_USER_NAME)
            .remove(KEY_ACCOUNT_EMAIL)
            .remove(KEY_BIOMETRIC_ENABLED)
            .remove(KEY_SAVED_LOCATION_ADDRESS)
            .remove(KEY_SAVED_LOCATION_LABEL)
            .remove(KEY_SAVED_ROUTE_NAME)
            .remove(KEY_SAVED_ROUTE_DESTINATION)
            .putBoolean(
                KEY_ONBOARDING_COMPLETE,
                false
            )
            .apply()
    }

    companion object {
        private const val PREFS_NAME =
            "commute_companion_prefs"

        private const val KEY_ONBOARDING_COMPLETE =
            "onboarding_complete"

        private const val KEY_SELECTED_LANGUAGE =
            "selected_language"

        private const val KEY_USER_NAME =
            "user_name"

        private const val KEY_ACCOUNT_EMAIL =
            "account_email"

        private const val KEY_BIOMETRIC_ENABLED =
            "biometric_enabled"

        private const val KEY_SAVED_LOCATION_ADDRESS =
            "saved_location_address"

        private const val KEY_SAVED_LOCATION_LABEL =
            "saved_location_label"

        private const val KEY_SAVED_ROUTE_NAME =
            "saved_route_name"

        private const val KEY_SAVED_ROUTE_DESTINATION =
            "saved_route_destination"

        private const val KEY_NOTIFICATIONS_ENABLED =
            "notifications_enabled"
    }
}