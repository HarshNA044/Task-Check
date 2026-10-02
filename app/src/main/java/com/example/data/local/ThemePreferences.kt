package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Local device preferences for theme and reminder notification schedule.
 * All settings are stored locally on the device using SharedPreferences.
 */
class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("chrono_theme_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        try {
            AppThemeMode.valueOf(prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name)
        } catch (e: Exception) {
            AppThemeMode.SYSTEM
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

    fun setThemeMode(mode: AppThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
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
    }
}
