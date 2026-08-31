package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppThemeMode
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("chrono_theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        try {
            AppThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, AppThemeMode.LIGHT.name) ?: AppThemeMode.LIGHT.name)
        } catch (e: Exception) {
            AppThemeMode.LIGHT
        }
    )
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _morningHour = MutableStateFlow(prefs.getInt(KEY_MORNING_HOUR, 9))
    val morningHour: StateFlow<Int> = _morningHour.asStateFlow()

    private val _morningMinute = MutableStateFlow(prefs.getInt(KEY_MORNING_MINUTE, 0))
    val morningMinute: StateFlow<Int> = _morningMinute.asStateFlow()

    private val _eveningHour = MutableStateFlow(prefs.getInt(KEY_EVENING_HOUR, 18))
    val eveningHour: StateFlow<Int> = _eveningHour.asStateFlow()

    private val _eveningMinute = MutableStateFlow(prefs.getInt(KEY_EVENING_MINUTE, 0))
    val eveningMinute: StateFlow<Int> = _eveningMinute.asStateFlow()

    private val _soundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND_ENABLED, true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfile(
            name = prefs.getString(KEY_USER_NAME, "Harshna") ?: "Harshna",
            email = prefs.getString(KEY_USER_EMAIL, "harshna63@gmail.com") ?: "harshna63@gmail.com",
            bio = prefs.getString(KEY_USER_BIO, "Stay organized & hit all goals") ?: "Stay organized & hit all goals",
            isGoogleSignedIn = prefs.getBoolean(KEY_GOOGLE_SIGNED_IN, false),
            avatarInitial = prefs.getString(KEY_USER_AVATAR, "H") ?: "H"
        )
    )
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun updateUserProfile(profile: UserProfile) {
        _userProfile.value = profile
        prefs.edit()
            .putString(KEY_USER_NAME, profile.name)
            .putString(KEY_USER_EMAIL, profile.email)
            .putString(KEY_USER_BIO, profile.bio)
            .putBoolean(KEY_GOOGLE_SIGNED_IN, profile.isGoogleSignedIn)
            .putString(KEY_USER_AVATAR, profile.avatarInitial)
            .apply()
    }

    fun signInWithGoogle(name: String, email: String) {
        val initial = if (name.isNotBlank()) name.first().uppercase() else "G"
        val updated = _userProfile.value.copy(
            name = name,
            email = email,
            isGoogleSignedIn = true,
            avatarInitial = initial
        )
        updateUserProfile(updated)
    }

    fun signOut() {
        val updated = _userProfile.value.copy(
            isGoogleSignedIn = false
        )
        updateUserProfile(updated)
    }

    fun setReminderTimes(morningH: Int, morningM: Int, eveningH: Int, eveningM: Int) {
        _morningHour.value = morningH
        _morningMinute.value = morningM
        _eveningHour.value = eveningH
        _eveningMinute.value = eveningM
        prefs.edit()
            .putInt(KEY_MORNING_HOUR, morningH)
            .putInt(KEY_MORNING_MINUTE, morningM)
            .putInt(KEY_EVENING_HOUR, eveningH)
            .putInt(KEY_EVENING_MINUTE, eveningM)
            .apply()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _soundEnabled.value = enabled
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    companion object {
        private const val KEY_THEME_MODE = "key_app_theme_mode"
        private const val KEY_MORNING_HOUR = "key_morning_hour"
        private const val KEY_MORNING_MINUTE = "key_morning_minute"
        private const val KEY_EVENING_HOUR = "key_evening_hour"
        private const val KEY_EVENING_MINUTE = "key_evening_minute"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_USER_EMAIL = "key_user_email"
        private const val KEY_USER_BIO = "key_user_bio"
        private const val KEY_GOOGLE_SIGNED_IN = "key_google_signed_in"
        private const val KEY_USER_AVATAR = "key_user_avatar"
    }
}
