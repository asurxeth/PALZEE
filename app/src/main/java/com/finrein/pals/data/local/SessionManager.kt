package com.finrein.pals.data.local

import android.content.Context
import com.finrein.pals.domain.model.User

class SessionManager(context: Context) {
    private val prefs = context.getSharedPreferences("pal_prefs", Context.MODE_PRIVATE)

    fun saveUser(user: User) {
        prefs.edit().apply {
            putString("user_id", user.id)
            putString("user_email", user.email)
            putString("user_display_name", user.displayName)
            putBoolean("user_is_passkey_registered", user.isPasskeyRegistered)
            apply()
        }
    }

    fun getUser(): User? {
        val id = prefs.getString("user_id", null) ?: return null
        val email = prefs.getString("user_email", null)
        val displayName = prefs.getString("user_display_name", null)
        val isPasskeyRegistered = prefs.getBoolean("user_is_passkey_registered", false)
        return User(id, email, displayName, isPasskeyRegistered)
    }

    fun updateDisplayName(name: String) {
        val u = getUser()
        if (u != null) {
            saveUser(u.copy(displayName = name))
        }
    }

    fun clearUser() {
        val hasLoggedInBefore = prefs.getBoolean("has_logged_in_before", false)
        prefs.edit().clear().putBoolean("has_logged_in_before", hasLoggedInBefore).apply()
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
    }

    fun isOnboardingCompleted(): Boolean {
        return prefs.getBoolean("onboarding_completed", false)
    }

    fun saveAvatarUri(uri: String?) {
        prefs.edit().putString("avatar_uri", uri).apply()
    }

    fun getAvatarUri(): String? {
        return prefs.getString("avatar_uri", null)
    }

    fun setFirstLogin(isFirst: Boolean) {
        prefs.edit().putBoolean("is_first_login", isFirst).apply()
    }

    fun isFirstLogin(): Boolean {
        return prefs.getBoolean("is_first_login", true)
    }

    fun setHasLoggedInBefore(hasLoggedIn: Boolean) {
        prefs.edit().putBoolean("has_logged_in_before", hasLoggedIn).apply()
    }

    fun hasLoggedInBefore(): Boolean {
        return prefs.getBoolean("has_logged_in_before", false)
    }

    fun saveThemeColor(color: String) {
        prefs.edit().putString("selected_theme_color", color).apply()
    }

    fun getThemeColor(): String {
        return prefs.getString("selected_theme_color", "yellow") ?: "yellow"
    }
}
