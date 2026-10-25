package com.kampplus.hava.data

import android.content.Context
import com.kampplus.hava.model.TemperatureUnit
import com.kampplus.hava.model.ThemeMode
import com.kampplus.hava.model.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Küçük kullanıcı tercihlerini cihazda kalıcı ve çevrimdışı saklar. */
class UserPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<UserSettings> = _settings.asStateFlow()

    fun setTemperatureUnit(unit: TemperatureUnit) = update(KEY_TEMPERATURE, unit.name)

    fun setThemeMode(mode: ThemeMode) = update(KEY_THEME, mode.name)

    fun setDailyAdviceEnabled(enabled: Boolean) {
        preferences.edit().putBoolean(KEY_ADVICE, enabled).apply()
        _settings.value = _settings.value.copy(dailyAdviceEnabled = enabled)
    }

    private fun update(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
        _settings.value = readSettings()
    }

    private fun readSettings() = UserSettings(
        temperatureUnit = preferences.getString(KEY_TEMPERATURE, null).toEnumOrDefault(TemperatureUnit.CELSIUS),
        themeMode = preferences.getString(KEY_THEME, null).toEnumOrDefault(ThemeMode.SYSTEM),
        dailyAdviceEnabled = preferences.getBoolean(KEY_ADVICE, true)
    )

    private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
        enumValues<T>().firstOrNull { it.name == this } ?: default

    private companion object {
        const val PREFERENCES_NAME = "hava_preferences"
        const val KEY_TEMPERATURE = "temperature_unit"
        const val KEY_THEME = "theme_mode"
        const val KEY_ADVICE = "daily_advice"
    }
}
