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

    private val _activeUserId = MutableStateFlow(
        prefs.getString(KEY_ACTIVE_USER_ID, "harshna63@gmail.com") ?: "harshna63@gmail.com"
    )
    val activeUserId: StateFlow<String> = _activeUserId.asStateFlow()

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

    private val _userProfile = MutableStateFlow(loadUserProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    private fun loadUserProfile(): UserProfile {
        val uid = _activeUserId.value
        val isGoogle = prefs.getBoolean(KEY_GOOGLE_SIGNED_IN + "_" + uid, true)
        val name = prefs.getString(KEY_USER_NAME + "_" + uid, "Harshna") ?: "Harshna"
        val email = prefs.getString(KEY_USER_EMAIL + "_" + uid, "harshna63@gmail.com") ?: "harshna63@gmail.com"
        val bio = prefs.getString(KEY_USER_BIO + "_" + uid, "Stay organized & hit all goals") ?: "Stay organized & hit all goals"
        val avatar = prefs.getString(KEY_USER_AVATAR + "_" + uid, if (name.isNotBlank()) name.first().uppercase() else "H") ?: "H"
        return UserProfile(
            id = uid,
            name = name,
            email = email,
            bio = bio,
            isGoogleSignedIn = isGoogle,
            avatarInitial = avatar
        )
    }

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun updateUserProfile(profile: UserProfile) {
        _userProfile.value = profile
        val uid = profile.id.ifBlank { profile.email }
        _activeUserId.value = uid
        prefs.edit()
            .putString(KEY_ACTIVE_USER_ID, uid)
            .putString(KEY_USER_NAME + "_" + uid, profile.name)
            .putString(KEY_USER_EMAIL + "_" + uid, profile.email)
            .putString(KEY_USER_BIO + "_" + uid, profile.bio)
            .putBoolean(KEY_GOOGLE_SIGNED_IN + "_" + uid, profile.isGoogleSignedIn)
            .putString(KEY_USER_AVATAR + "_" + uid, profile.avatarInitial)
            .apply()
    }

    fun signInWithGoogle(name: String, email: String, photoUrl: String? = null) {
        val initial = if (name.isNotBlank()) name.first().uppercase() else "G"
        val profile = UserProfile(
            id = email,
            name = name,
            email = email,
            bio = "Productive & Focused",
            isGoogleSignedIn = true,
            avatarInitial = initial,
            photoUrl = photoUrl
        )
        updateUserProfile(profile)
    }

    fun switchAccount(email: String, name: String) {
        signInWithGoogle(name, email)
    }

    fun signOut() {
        val guestId = "guest_user"
        val guestProfile = UserProfile(
            id = guestId,
            name = "Guest User",
            email = "guest@local",
            bio = "Offline Local Workspace",
            isGoogleSignedIn = false,
            avatarInitial = "G"
        )
        _activeUserId.value = guestId
        _userProfile.value = guestProfile
        prefs.edit()
            .putString(KEY_ACTIVE_USER_ID, guestId)
            .putBoolean(KEY_GOOGLE_SIGNED_IN + "_" + guestId, false)
            .putString(KEY_USER_NAME + "_" + guestId, guestProfile.name)
            .putString(KEY_USER_EMAIL + "_" + guestId, guestProfile.email)
            .putString(KEY_USER_BIO + "_" + guestId, guestProfile.bio)
            .putString(KEY_USER_AVATAR + "_" + guestId, guestProfile.avatarInitial)
            .apply()
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
        private const val KEY_ACTIVE_USER_ID = "key_active_user_id"
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
