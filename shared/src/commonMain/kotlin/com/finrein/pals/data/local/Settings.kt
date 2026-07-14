package com.finrein.pals.data.local

expect class Settings() {
    fun putString(key: String, value: String)
    fun getString(key: String, defaultValue: String): String
    fun getStringOrNull(key: String): String?
    fun putBoolean(key: String, value: Boolean)
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun remove(key: String)
    fun clear()
}
