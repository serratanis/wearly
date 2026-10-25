package com.kampplus.hava.model

import java.time.LocalDate
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WeatherAdviceTest {

    @Test
    fun `yuksek uv ve yagmur koruyucu oneriler uretir`() {
        val forecast = Forecast(
            current = CurrentWeather(
                temperatureC = 16.0,
                apparentTemperatureC = 14.0,
                weatherCode = 61,
                observedAt = LocalDateTime.parse("2026-10-25T12:00")
            ),
            hourly = emptyList(),
            daily = listOf(
                DailyForecast(
                    date = LocalDate.parse("2026-10-25"),
                    minTemperatureC = 10.0,
                    maxTemperatureC = 18.0,
                    weatherCode = 61,
                    precipitationProbability = 70,
                    uvIndexMax = 7.0
                )
            )
        )

        val titles = forecast.dailyAdvice().map(WeatherAdvice::title)

        assertTrue("Şemsiye + suya dayanıklı üst" in titles)
        assertTrue("Güneş koruması" in titles)
        assertTrue("Kazak + ince ceket" in titles)
    }

    @Test
    fun `yuksek uv icin ayrintili korunma plani uretir`() {
        val forecast = testForecast(uv = 9.0, rain = 10)

        val protection = forecast.sunProtection()!!

        assertEquals("9 · Çok yüksek", protection.level)
        assertTrue(protection.detail.contains("SPF 30+"))
        assertTrue(protection.progress > 0.8f)
    }

    @Test
    fun `gun ozeti uygun disari cikma araligini bulur`() {
        val forecast = testForecast(uv = 2.0, rain = 10).copy(
            hourly = listOf(
                HourlyForecast(LocalDateTime.parse("2026-10-25T10:00"), 20.0, 1, 10),
                HourlyForecast(LocalDateTime.parse("2026-10-25T11:00"), 21.0, 1, 10),
                HourlyForecast(LocalDateTime.parse("2026-10-25T12:00"), 22.0, 1, 80)
            )
        )

        assertEquals("10.00–12.00 dışarı çıkmak için en dengeli aralık", forecast.dayInsight().bestTime)
    }

    @Test
    fun `dengeli kosullar yuksek ve aciklanabilir konfor puani uretir`() {
        val score = testForecast(uv = 2.0, rain = 10).comfortScore()

        assertTrue(score.value in 85..100)
        assertEquals("Çok rahat", score.label)
        assertTrue(score.explanation.isNotBlank())
    }

    @Test
    fun `yogun yagmur konfor puanini dusurur`() {
        val calm = testForecast(uv = 2.0, rain = 0).comfortScore()
        val rainy = testForecast(uv = 2.0, rain = 100).comfortScore()

        assertTrue(rainy.value < calm.value)
        assertTrue(rainy.explanation.contains("yağış"))
    }

    @Test
    fun `etkinlik planlayici daha kuru iki saatlik araligi secer`() {
        val forecast = testForecast(uv = 2.0, rain = 10).copy(
            hourly = listOf(
                HourlyForecast(LocalDateTime.parse("2026-10-25T09:00"), 18.0, 61, 80),
                HourlyForecast(LocalDateTime.parse("2026-10-25T10:00"), 19.0, 3, 10),
                HourlyForecast(LocalDateTime.parse("2026-10-25T11:00"), 20.0, 1, 5),
                HourlyForecast(LocalDateTime.parse("2026-10-25T12:00"), 22.0, 1, 10)
            )
        )

        val window = forecast.bestTimeFor(ActivityType.WALK)!!

        assertEquals("10.00–12.00", window.timeRange)
        assertTrue(window.detail.contains("%7"))
    }

    private fun testForecast(uv: Double, rain: Int) = Forecast(
        current = CurrentWeather(
            temperatureC = 22.0,
            apparentTemperatureC = 21.0,
            weatherCode = 1,
            observedAt = LocalDateTime.parse("2026-10-25T10:00")
        ),
        hourly = emptyList(),
        daily = listOf(
            DailyForecast(
                date = LocalDate.parse("2026-10-25"),
                minTemperatureC = 14.0,
                maxTemperatureC = 23.0,
                weatherCode = 1,
                precipitationProbability = rain,
                uvIndexMax = uv
            )
        )
    )
}
