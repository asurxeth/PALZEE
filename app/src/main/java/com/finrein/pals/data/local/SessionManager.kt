package com.finrein.pals.data.local

import com.finrein.pals.domain.model.User

class SessionManager(private val settings: Settings = Settings()) {

    fun saveUser(user: User) {
        settings.putString("user_id", user.id)
        settings.putString("user_email", user.email ?: "")
        settings.putString("user_display_name", user.displayName ?: "")
        settings.putBoolean("user_is_passkey_registered", user.isPasskeyRegistered)
    }

    fun getUser(): User? {
        val id = settings.getStringOrNull("user_id") ?: return null
        val email = settings.getStringOrNull("user_email")
        val displayName = settings.getStringOrNull("user_display_name")
        val isPasskeyRegistered = settings.getBoolean("user_is_passkey_registered", false)
        return User(id, email, displayName, isPasskeyRegistered)
    }

    fun updateDisplayName(name: String) {
        val u = getUser()
        if (u != null) {
            saveUser(u.copy(displayName = name))
        }
    }

    fun clearUser() {
        val hasLoggedInBefore = settings.getBoolean("has_logged_in_before", false)
        settings.clear()
        settings.putBoolean("has_logged_in_before", hasLoggedInBefore)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        settings.putBoolean("onboarding_completed", completed)
    }

    fun isOnboardingCompleted(): Boolean {
        return settings.getBoolean("onboarding_completed", false)
    }

    fun saveAvatarUri(uri: String?) {
        if (uri != null) {
            settings.putString("avatar_uri", uri)
        } else {
            settings.remove("avatar_uri")
        }
    }

    fun getAvatarUri(): String? {
        return settings.getStringOrNull("avatar_uri")
    }

    fun setFirstLogin(isFirst: Boolean) {
        settings.putBoolean("is_first_login", isFirst)
    }

    fun isFirstLogin(): Boolean {
        return settings.getBoolean("is_first_login", true)
    }

    fun setHasLoggedInBefore(hasLoggedIn: Boolean) {
        settings.putBoolean("has_logged_in_before", hasLoggedIn)
    }

    fun hasLoggedInBefore(): Boolean {
        return settings.getBoolean("has_logged_in_before", false)
    }

    fun saveThemeColor(color: String) {
        settings.putString("selected_theme_color", color)
    }

    fun getThemeColor(): String {
        return settings.getString("selected_theme_color", "blue")
    }

    fun saveNotificationInterval(interval: String) {
        settings.putString("notification_interval", interval)
    }

    fun getNotificationInterval(): String {
        return settings.getString("notification_interval", "every 1hr")
    }
}
