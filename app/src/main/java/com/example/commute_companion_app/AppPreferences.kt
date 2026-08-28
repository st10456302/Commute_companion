package com.example.commute_companion_app

import android.content.Context
import android.content.SharedPreferences

/**
 * Central local storage for the Commute Companion prototype.
 *
 * This wraps Android's SharedPreferences behind a single, simple class.
 * As we implement later steps, we will add more fields here (accountEmail,
 * savedLocation, savedRoute, notificationsEnabled, biometricEnabled).
 *
 * IMPORTANT: This is a prototype-stage storage layer only. It is intentionally
 * kept simple so that a future real backend (ASP.NET Core API + Azure SQL) can
 * replace or supplement it later without Activities needing to change how they
 * call this class.
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

    companion object {
        private const val PREFS_NAME = "commute_companion_prefs"
        private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
        private const val KEY_SELECTED_LANGUAGE = "selected_language"
    }
}