package com.kampplus.hava.model

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StyleAssistantTest {
    @Test
    fun `sik ve yagmurlu plan hava kosullarina uygun kombin uretir`() {
        val forecast = Forecast(
            current = CurrentWeather(
                temperatureC = 14.0,
                weatherCode = 61,
                observedAt = LocalDateTime.parse("2026-10-25T12:00"),
                apparentTemperatureC = 12.0,
                windSpeedKmh = 18.0
            ),
            hourly = emptyList(),
            daily = listOf(
                DailyForecast(
                    date = LocalDate.parse("2026-10-25"),
                    minTemperatureC = 10.0,
                    maxTemperatureC = 16.0,
                    weatherCode = 61,
                    precipitationProbability = 75,
                    uvIndexMax = 2.0
                )
            )
        )

        val reply = forecast.styleReply("Bugün şık olmam lazım")

        assertTrue(reply.contains("kumaş pantolon"))
        assertTrue(reply.contains("suya dayanıklı"))
        assertTrue(reply.contains("%75"))
        assertTrue(reply.contains("Neden: hissedilen 12°C"))

        val outfit = forecast.outfitRecommendation("Bugün şık olmam lazım")
        assertTrue(outfit.top.isNotBlank())
        assertTrue(outfit.bottom.contains("kumaş pantolon"))
        assertTrue(outfit.protection.contains("suya dayanıklı"))
    }

    @Test
    fun `dolap istegi yalnizca kayitli parcalari kullanir`() {
        val forecast = Forecast(
            current = CurrentWeather(18.0, 1, LocalDateTime.parse("2026-10-25T12:00")),
            hourly = emptyList(),
            daily = emptyList()
        )
        val wardrobe = listOf(
            WardrobeItem("1", "Krem triko", "Üst", "Krem", "Geçiş", "Minimal"),
            WardrobeItem("2", "Siyah pantolon", "Alt", "Siyah", "Dört mevsim", "Business")
        )

        val outfit = forecast.outfitRecommendation("Dolabımdan şık bir kombin", wardrobe)

        assertEquals("Krem triko (Krem)", outfit.top)
        assertEquals("Siyah pantolon (Siyah)", outfit.bottom)
        assertTrue(outfit.shoes.contains("Dolabında"))
    }

    @Test
    fun `kisisel stil ve sicaklik hassasiyeti oneriyi degistirir`() {
        val forecast = Forecast(
            current = CurrentWeather(
                temperatureC = 19.0,
                weatherCode = 1,
                observedAt = LocalDateTime.parse("2026-10-25T12:00"),
                apparentTemperatureC = 18.0
            ),
            hourly = emptyList(),
            daily = emptyList()
        )
        val settings = UserSettings(
            stylePreference = StylePreference.MINIMAL,
            temperatureSensitivity = TemperatureSensitivity.COLD_SENSITIVE,
            outfitPriority = OutfitPriority.COMFORT
        )

        val outfit = forecast.outfitRecommendation("Bugün ne giysem?", settings = settings)

        assertTrue(outfit.title.contains("Konfor odaklı"))
        assertTrue(outfit.title.contains("Minimal"))
        assertTrue(outfit.top.contains("triko"))
        assertTrue(outfit.reason.contains("üşüme hassasiyetine"))
    }
}
