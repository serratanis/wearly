package com.kampplus.hava.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Bir şehrin anlık durumu + önümüzdeki saatler ve günler.
 *
 * Tahmin ekranının veri modeli: bir şehre dokunulduğunda gösterilen bilgiler.
 * Zamanlar `timezone=auto` sayesinde şehrin **yerel** saatindedir.
 */
data class Forecast(
    val current: CurrentWeather,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>
)

/**
 * Şu anki hava. Son üç alan servis tarafından her zaman gönderilmez,
 * bu yüzden nullable'dır ve ekranda yalnızca varsa gösterilir.
 */
data class CurrentWeather(
    val temperatureC: Double,
    val weatherCode: Int,
    val observedAt: LocalDateTime,
    val apparentTemperatureC: Double? = null,
    val humidityPercent: Int? = null,
    val windSpeedKmh: Double? = null
)

/** Tek bir saatlik tahmin satırı. */
data class HourlyForecast(
    val time: LocalDateTime,
    val temperatureC: Double,
    val weatherCode: Int,
    val precipitationProbability: Int? = null
)

/** Tek bir günlük tahmin satırı: o günün en düşük ve en yüksek sıcaklığı. */
data class DailyForecast(
    val date: LocalDate,
    val minTemperatureC: Double,
    val maxTemperatureC: Double,
    val weatherCode: Int,
    val precipitationProbability: Int? = null,
    val sunrise: LocalDateTime? = null,
    val sunset: LocalDateTime? = null,
    val uvIndexMax: Double? = null
)
