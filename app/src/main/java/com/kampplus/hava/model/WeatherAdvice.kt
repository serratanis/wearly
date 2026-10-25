package com.kampplus.hava.model

/** Gerçek tahmin değerlerinden üretilen, kısa ve uygulanabilir günlük öneri. */
data class WeatherAdvice(
    val icon: String,
    val title: String,
    val message: String,
    val category: AdviceCategory
)

enum class AdviceCategory { CLOTHING, RAIN, SUN, WIND }

data class DayInsight(
    val headline: String,
    val summary: String,
    val bestTime: String?
)

data class SunProtection(
    val level: String,
    val action: String,
    val detail: String,
    val progress: Float
)

/** Kullanıcıya tek bakışta okunabilen ve gerekçesi görülebilen 0–100 hava konforu. */
data class ComfortScore(
    val value: Int,
    val label: String,
    val explanation: String
)

enum class ActivityType {
    WALK,
    SPORT,
    COFFEE,
    COMMUTE
}

data class ActivityWindow(
    val timeRange: String,
    val detail: String
)

/** Önümüzdeki gerçek saatlik tahminden seçilen etkinliğe en uygun iki saatlik aralık. */
fun Forecast.bestTimeFor(activity: ActivityType): ActivityWindow? {
    val hours = hourly.take(24).filter { hour ->
        when (activity) {
            ActivityType.WALK -> hour.time.hour in 8..20
            ActivityType.SPORT -> hour.time.hour in 7..20
            ActivityType.COFFEE -> hour.time.hour in 9..21
            ActivityType.COMMUTE -> hour.time.hour in 7..10 || hour.time.hour in 16..19
        }
    }
    val windows = hours.windowed(size = 2, step = 1).filter { it[1].time == it[0].time.plusHours(1) }
    val idealTemperature = when (activity) {
        ActivityType.WALK -> 20
        ActivityType.SPORT -> 16
        ActivityType.COFFEE -> 22
        ActivityType.COMMUTE -> 19
    }
    val best = windows.minByOrNull { window ->
        window.sumOf { hour ->
            val rainWeight = if (activity == ActivityType.SPORT) 3 else 2
            (hour.precipitationProbability ?: 0) * rainWeight +
                kotlin.math.abs(hour.temperatureC - idealTemperature).toInt() * 4
        }
    } ?: return null
    val averageRain = best.map { it.precipitationProbability ?: 0 }.average().toInt()
    val averageTemperature = best.map(HourlyForecast::temperatureC).average().toInt()
    return ActivityWindow(
        timeRange = "%02d.00–%02d.00".format(best.first().time.hour, best.last().time.hour + 1),
        detail = "$averageTemperature°C · ortalama yağış olasılığı %$averageRain"
    )
}

fun Forecast.comfortScore(): ComfortScore {
    val feelsLike = current.apparentTemperatureC ?: current.temperatureC
    val rain = daily.firstOrNull()?.precipitationProbability ?: 0
    val wind = current.windSpeedKmh ?: 0.0
    val humidity = current.humidityPercent ?: 50
    val uv = daily.firstOrNull()?.uvIndexMax ?: 0.0

    val temperaturePenalty = (kotlin.math.abs(feelsLike - 21.0) * 3.0).coerceAtMost(32.0)
    val rainPenalty = (rain * 0.3).coerceAtMost(30.0)
    val windPenalty = ((wind - 15.0).coerceAtLeast(0.0) * 0.8).coerceAtMost(18.0)
    val humidityPenalty = ((kotlin.math.abs(humidity - 50) - 20).coerceAtLeast(0) * 0.35).coerceAtMost(10.0)
    val uvPenalty = ((uv - 6.0).coerceAtLeast(0.0) * 2.0).coerceAtMost(10.0)
    val score = (100 - temperaturePenalty - rainPenalty - windPenalty - humidityPenalty - uvPenalty)
        .toInt()
        .coerceIn(0, 100)
    val label = when {
        score >= 85 -> "Çok rahat"
        score >= 70 -> "Rahat"
        score >= 50 -> "Dengeli"
        score >= 30 -> "Hazırlık gerekli"
        else -> "Zorlayıcı"
    }
    val strongestFactor = listOf(
        rainPenalty to "yağış",
        temperaturePenalty to "hissedilen sıcaklık",
        windPenalty to "rüzgâr",
        humidityPenalty to "nem",
        uvPenalty to "UV"
    ).maxBy { it.first }
    val explanation = if (strongestFactor.first < 5) {
        "Koşullar günün büyük bölümünde dengeli görünüyor."
    } else {
        "Puanı en çok ${strongestFactor.second} etkiliyor; ayrıntılı önerileri aşağıda görebilirsin."
    }
    return ComfortScore(score, label, explanation)
}

fun Forecast.dayInsight(): DayInsight {
    val today = daily.firstOrNull()
    val min = today?.minTemperatureC
    val max = today?.maxTemperatureC
    val rain = today?.precipitationProbability ?: 0
    val condition = WeatherCondition.fromWeatherCode(today?.weatherCode ?: current.weatherCode)
    val range = if (min != null && max != null) max - min else 0.0
    val best = bestOutdoorWindow()

    val headline = when {
        rain >= 70 -> "Yağış planını belirleyecek"
        (today?.uvIndexMax ?: 0.0) >= 8 -> "Güneş güçlü, korunarak çık"
        range >= 10 -> "Katmanlı giyinme günü"
        current.windSpeedKmh?.let { it >= 35 } == true -> "Rüzgârı hesaba kat"
        else -> "Plan yapmak için dengeli bir gün"
    }
    val summary = buildList {
        add("${condition.label.lowercase()} bir hava bekleniyor")
        if (min != null && max != null) add("sıcaklık ${min.toInt()}°–${max.toInt()}° arasında")
        if (rain >= 40) add("yağış olasılığı %$rain")
    }.joinToString(", ").replaceFirstChar(Char::uppercase) + "."

    return DayInsight(headline, summary, best)
}

fun Forecast.sunProtection(): SunProtection? {
    val uv = daily.firstOrNull()?.uvIndexMax ?: return null
    return when {
        uv < 3 -> SunProtection(
            level = "${uv.toInt()} · Düşük",
            action = "Temel koruma yeterli",
            detail = "Uzun süre dışarıda kalacaksan güneş gözlüğünü yanında bulundur.",
            progress = (uv / 11).toFloat()
        )
        uv < 6 -> SunProtection(
            level = "${uv.toInt()} · Orta",
            action = "SPF 30+ ve gölge",
            detail = "Geniş spektrumlu kremi çıkmadan önce uygula; dışarıdaysan 2 saatte bir ve terleme sonrası yenile.",
            progress = (uv / 11).toFloat()
        )
        uv < 8 -> SunProtection(
            level = "${uv.toInt()} · Yüksek",
            action = "SPF 30+, şapka ve gözlük",
            detail = "Öğle saatlerinde gölgeyi seç; kremi 2 saatte bir, yüzme veya terleme sonrası yeniden uygula.",
            progress = (uv / 11).toFloat()
        )
        else -> SunProtection(
            level = "${uv.toInt()} · Çok yüksek",
            action = "Ekstra korun",
            detail = "Öğle güneşini sınırla; sık dokulu giysi, şapka, gözlük ve geniş spektrumlu SPF 30+ kullan.",
            progress = (uv / 11).toFloat().coerceAtMost(1f)
        )
    }
}

fun Forecast.dailyAdvice(): List<WeatherAdvice> {
    val today = daily.firstOrNull()
    val feelsLike = current.apparentTemperatureC ?: current.temperatureC
    return buildList {
        add(
            when {
                feelsLike <= 8 -> WeatherAdvice(
                    "🧥",
                    "Mont + kapalı ayakkabı",
                    "Isı tutan bir üst katman ve kalın çorap tercih et.",
                    AdviceCategory.CLOTHING
                )
                feelsLike <= 17 -> WeatherAdvice(
                    "🧶",
                    "Kazak + ince ceket",
                    "Sabah-akşam için çıkarılabilir bir dış katman taşı.",
                    AdviceCategory.CLOTHING
                )
                feelsLike >= 28 -> WeatherAdvice(
                    "👕",
                    "İnce ve nefes alan",
                    "Açık renkli kıyafet, şapka ve su iyi bir kombin.",
                    AdviceCategory.CLOTHING
                )
                else -> WeatherAdvice("👟", "Tişört + hafif katman", "Akşama kalacaksan yanına ince bir üst al.", AdviceCategory.CLOTHING)
            }
        )

        val rainChance = today?.precipitationProbability ?: 0
        if (rainChance >= 40) {
            add(
                WeatherAdvice(
                    "☂️",
                    "Şemsiye + suya dayanıklı üst",
                    "Yağış olasılığı %$rainChance; kaymayan bir ayakkabı tercih et.",
                    AdviceCategory.RAIN
                )
            )
        }

        val uv = today?.uvIndexMax
        if (uv != null && uv >= 3) {
            val protection = if (uv >= 6) "SPF 50" else "SPF 30+"
            add(
                WeatherAdvice(
                    "🧴",
                    "Güneş koruması",
                    "$protection geniş spektrumlu krem, şapka ve gözlük kullan; 2 saatte bir yenile.",
                    AdviceCategory.SUN
                )
            )
        }

        val wind = current.windSpeedKmh
        if (wind != null && wind >= 35) {
            add(
                WeatherAdvice(
                    "💨",
                    "Rüzgâr kesen dış katman",
                    "${wind.toInt()} km/sa rüzgâr bekleniyor; şemsiye yerine kapüşon daha rahat olabilir.",
                    AdviceCategory.WIND
                )
            )
        }
    }
}

private fun Forecast.bestOutdoorWindow(): String? {
    val candidates = hourly
        .filter { it.time.hour in 8..20 }
        .take(24)
        .windowed(size = 2, step = 1)
        .filter { window -> window.all { (it.precipitationProbability ?: 0) < 35 } }
    val best = candidates.minByOrNull { window ->
        window.sumOf { (it.precipitationProbability ?: 0) + kotlin.math.abs(it.temperatureC - 21).toInt() * 2 }
    } ?: return null
    return "%02d.00–%02d.00 dışarı çıkmak için en dengeli aralık".format(best.first().time.hour, best.last().time.hour + 1)
}
