package com.finrein.pals.core.data.local

import android.content.Context
import android.content.SharedPreferences

class Settings() {
    private val prefs: SharedPreferences by lazy {
        SharedContextRegistry.context.getSharedPreferences("pal_prefs", Context.MODE_PRIVATE)
    }

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    fun getStringOrNull(key: String): String? {
        return prefs.getString(key, null)
    }

    fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }
}
