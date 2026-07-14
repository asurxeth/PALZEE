package com.finrein.pals.data.local

import platform.Foundation.NSUserDefaults

actual class Settings actual constructor() {
    private val prefs = NSUserDefaults.standardUserDefaults

    actual fun putString(key: String, value: String) {
        prefs.setObject(value, forKey = key)
    }

    actual fun getString(key: String, defaultValue: String): String {
        return prefs.stringForKey(key) ?: defaultValue
    }

    actual fun getStringOrNull(key: String): String? {
        return prefs.stringForKey(key)
    }

    actual fun putBoolean(key: String, value: Boolean) {
        prefs.setBool(value, forKey = key)
    }

    actual fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        if (prefs.objectForKey(key) == null) return defaultValue
        return prefs.boolForKey(key)
    }

    actual fun remove(key: String) {
        prefs.removeObjectForKey(key)
    }

    actual fun clear() {
        val dict = prefs.dictionaryRepresentation()
        dict.keys.forEach { key ->
            if (key is String) {
                prefs.removeObjectForKey(key)
            }
        }
    }
}
