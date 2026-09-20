package com.example.commute_companion_app

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageManager {

    fun applyLanguage(context: Context): Context {
        val prefs = AppPreferences(context)

        val languageCode = when (prefs.selectedLanguage) {
            "isiZulu" -> "zu"
            "Afrikaans" -> "af"
            else -> "en"
        }

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)

        return context.createConfigurationContext(configuration)
    }
}