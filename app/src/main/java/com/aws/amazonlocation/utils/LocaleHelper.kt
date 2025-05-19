package com.aws.amazonlocation.utils

import android.content.Context
import android.content.res.Configuration
import androidx.preference.PreferenceManager
import java.util.Locale

/**
 * Helper object to manage application locale changes.
 */
object LocaleHelper {

    /**
     * Sets the application's locale to the specified [language].
     *
     * @param context The context to update.
     * @param language The language code, e.g., "en", "es", or "pt-BR".
     * @return A context with updated locale configuration.
     */
    fun setLocale(context: Context, language: String): Context {
        persistLanguage(context, language)
        return updateResources(context, language)
    }

    /**
     * Persists the selected language code in shared preferences.
     *
     * @param context The context used to access shared preferences.
     * @param language The language code to persist.
     */
    private fun persistLanguage(context: Context, language: String) {
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putString(SELECTED_LANGUAGE, language)
            .apply()
    }

    /**
     * Updates the app's resources configuration to use the specified locale.
     *
     * @param context The context whose resources will be updated.
     * @param language The language code to apply.
     * @return A context with the updated configuration.
     */
    private fun updateResources(context: Context, language: String): Context {
        val locale = if (language.contains("-")) {
            val parts = language.split("-")
            Locale(parts[0], parts[1])
        } else {
            Locale(language)
        }
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * Attaches a base context with the persisted language configuration.
     *
     * @param base The original base context.
     * @return A new context with the appropriate locale set.
     */
    fun attachBaseContext(base: Context): Context {
        val lang = getLanguageCode(base)
        return setLocale(base, lang)
    }
}
