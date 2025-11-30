package com.example.prayertime.utils

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

object LanguageHelper {
    fun onAttach(context: Context): Context {
        val lang = getPersistedData(context, Locale.getDefault().language)
        return setLocale(context, lang)
    }

    fun getLanguage(context: Context): String {
        return getPersistedData(context, Locale.getDefault().language)
    }

    fun setLanguage(context: Context, language: String) {
        persist(context, language)
    }

    private fun setLocale(context: Context, language: String): Context {
        return updateResources(context, language)
    }

    private fun getPersistedData(context: Context, defaultLanguage: String): String {
        val preferences = context.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
        return preferences.getString("language", defaultLanguage) ?: defaultLanguage
    }

    private fun persist(context: Context, language: String) {
        val preferences = context.getSharedPreferences("prayer_prefs", Context.MODE_PRIVATE)
        preferences.edit().putString("language", language).apply()
    }

    private fun updateResources(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }
}
