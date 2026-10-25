package com.kampplus.hava.model

enum class TemperatureUnit {
    CELSIUS,
    FAHRENHEIT
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserSettings(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dailyAdviceEnabled: Boolean = true
)
