package com.kampplus.hava.model

import java.time.LocalDate
import java.time.LocalDateTime
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
    }
}
