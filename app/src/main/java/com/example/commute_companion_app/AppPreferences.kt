package com.example.commute_companion_app

import android.content.Context
import android.content.SharedPreferences

/**
 * Central local storage for the Commute Companion prototype.
 *
 * This wraps Android's SharedPreferences behind a single, simple class.
 * As we implement later steps, we will add more fields here (savedRoute,
 * notificationsEnabled).
 *
 * IMPORTANT: This is a prototype-stage storage layer only. It is intentionally
 * kept simple so that a future real backend (ASP.NET Core API + Azure SQL) can
 * replace or supplement it later without Activities needing to change how they
 * call this class.
 *
 * SECURITY NOTE: Passwords are intentionally NEVER stored here. This class only
 * ever holds non-sensitive account info (name, email) and prototype preferences.
 */
class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Whether the user has fully completed the onboarding flow at least once.
     * Defaults to false until we explicitly set it to true in a later step
     * (once Home Dashboard is reached for the first time).
     */
    var onboardingComplete: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, value).apply()

    /**
     * The language the user selected on the Language Selection screen.
     * Defaults to "English" since that is the option pre-selected in the
     * existing UI when a user has not made a choice yet.
     */
    var selectedLanguage: String
        get() = prefs.getString(KEY_SELECTED_LANGUAGE, "English") ?: "English"
        set(value) = prefs.edit().putString(KEY_SELECTED_LANGUAGE, value).apply()

    /**
     * The user's full name, entered on the Create Account screen.
     * Defaults to an empty string if not yet set.
     */
    var userName: String
        get() = prefs.getString(KEY_USER_NAME, "") ?: ""
        set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

    /**
     * The user's account email, entered on the Create Account screen.
     * Defaults to an empty string if not yet set.
     *
     * NOTE: The corresponding password is deliberately never stored here.
     */
    var accountEmail: String
        get() = prefs.getString(KEY_ACCOUNT_EMAIL, "") ?: ""
        set(value) = prefs.edit().putString(KEY_ACCOUNT_EMAIL, value).apply()

    /**
     * Whether the user chose to enable biometric login on the Biometric screen.
     * Defaults to false until the user explicitly enables it.
     */
    var biometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    /**
     * The location text the user entered/selected on the Set Location screen.
     * Defaults to an empty string if not yet set.
     */
    var savedLocationAddress: String
        get() = prefs.getString(KEY_SAVED_LOCATION_ADDRESS, "") ?: ""
        set(value) = prefs.edit().putString(KEY_SAVED_LOCATION_ADDRESS, value).apply()

    /**
     * Which "Save As" label the user chose for their location
     * (Home / Work / Campus / Custom). Defaults to "Work" since that is
     * the chip shown as pre-selected in the existing UI.
     */
    var savedLocationLabel: String
        get() = prefs.getString(KEY_SAVED_LOCATION_LABEL, "Work") ?: "Work"
        set(value) = prefs.edit().putString(KEY_SAVED_LOCATION_LABEL, value).apply()

    companion object {
        private const val PREFS_NAME = "commute_companion_prefs"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_ACCOUNT_EMAIL = "account_email"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_SAVED_LOCATION_ADDRESS = "saved_location_address"
        private const val KEY_SAVED_LOCATION_LABEL = "saved_location_label"
    }
}