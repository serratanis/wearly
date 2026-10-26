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

enum class StylePreference {
    CASUAL,
    ELEGANT,
    SPORTY,
    MINIMAL,
    BUSINESS
}

enum class TemperatureSensitivity {
    COLD_SENSITIVE,
    BALANCED,
    HEAT_SENSITIVE
}

enum class OutfitPriority {
    COMFORT,
    BALANCED,
    ELEGANCE
}

data class UserSettings(
    val temperatureUnit: TemperatureUnit = TemperatureUnit.CELSIUS,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dailyAdviceEnabled: Boolean = true,
    val stylePreference: StylePreference = StylePreference.CASUAL,
    val temperatureSensitivity: TemperatureSensitivity = TemperatureSensitivity.BALANCED,
    val outfitPriority: OutfitPriority = OutfitPriority.BALANCED,
    val homeCity: String = "İstanbul"
)
