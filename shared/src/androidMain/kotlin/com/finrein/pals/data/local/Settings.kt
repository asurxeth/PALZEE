package com.finrein.pals.data.local

import android.content.Context
import android.content.SharedPreferences

actual class Settings actual constructor() {
    private val prefs: SharedPreferences by lazy {
        SharedContextRegistry.context.getSharedPreferences("pal_prefs", Context.MODE_PRIVATE)
    }

    actual fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }

    actual fun getString(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    actual fun getStringOrNull(key: String): String? {
        return prefs.getString(key, null)
    }

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.edit().putBoolean(key, value).apply()
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return prefs.getBoolean(key, defaultValue)
    }

    actual fun remove(key: String) {
        prefs.edit().remove(key).apply()
    }

    actual fun clear() {
        prefs.edit().clear().apply()
    }
}
