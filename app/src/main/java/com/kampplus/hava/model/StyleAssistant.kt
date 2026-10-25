package com.kampplus.hava.model

import kotlin.math.roundToInt

data class StyleMessage(val text: String, val fromUser: Boolean)

fun Forecast.styleReply(request: String): String {
    val normalized = request.lowercase()
    val feels = current.apparentTemperatureC ?: current.temperatureC
    val rain = daily.firstOrNull()?.precipitationProbability ?: 0
    val wind = current.windSpeedKmh ?: 0.0
    val uv = daily.firstOrNull()?.uvIndexMax ?: 0.0

    val base = when {
        feels <= 8 -> "ısı tutan bir kazak ve yapılandırılmış uzun mont"
        feels <= 17 -> "ince triko ve hafif ceket"
        feels >= 28 -> "nefes alan, açık renkli ve ince parçalar"
        else -> "hafif bir üst ve gerektiğinde çıkarabileceğin ince katman"
    }
    val style = when {
        listOf("şık", "sik", "davet", "özel").any(normalized::contains) ->
            "Şık görünüm için $base; kumaş pantolon veya midi etek ve kapalı, rahat bir ayakkabı seç."
        listOf("iş", "ofis", "toplantı").any(normalized::contains) ->
            "Ofis için $base; düz kesim pantolon, sade gömlek ve loafer iyi çalışır."
        listOf("spor", "yürüyüş", "kosu", "koşu").any(normalized::contains) ->
            "Hareketli plan için $base; teri uzaklaştıran kumaş ve destekli spor ayakkabı seç."
        listOf("okul", "kampüs", "ders").any(normalized::contains) ->
            "Okul için $base; rahat pantolon, sneaker ve gün içinde çıkarılabilir bir üst katman kullan."
        else -> "Bugün için $base, rahat bir alt parça ve hava değişimine uygun ayakkabı öneririm."
    }
    val weatherExtras = buildList {
        if (rain >= 40) add("Yağış olasılığı %$rain: suya dayanıklı dış katman ve kaymayan ayakkabı ekle")
        if (wind >= 35) add("Rüzgâr ${wind.toInt()} km/sa: kapüşonlu veya rüzgâr kesen bir parça seç")
        if (uv >= 3) add("UV ${uv.toInt()}: şapka/gözlük ve geniş spektrumlu SPF 30+ kullan")
    }
    val reason = listOf(
        "hissedilen ${feels.roundToInt()}°C",
        "yağış %$rain",
        "rüzgâr ${wind.roundToInt()} km/sa",
        "UV ${uv.roundToInt()}"
    ).joinToString(" · ")

    return buildString {
        append(style)
        if (weatherExtras.isNotEmpty()) {
            append("\n\nDikkat: ")
            append(weatherExtras.joinToString(". "))
            append('.')
        }
        append("\n\nNeden: ")
        append(reason)
    }
}
