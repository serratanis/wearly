package com.kampplus.hava.model

import kotlin.math.roundToInt

data class StyleMessage(val text: String, val fromUser: Boolean)

data class OutfitRecommendation(
    val title: String,
    val top: String,
    val bottom: String,
    val outerwear: String,
    val shoes: String,
    val accessory: String,
    val protection: String,
    val colorPalette: String,
    val reason: String
)

fun Forecast.outfitRecommendation(
    request: String,
    wardrobe: List<WardrobeItem> = emptyList(),
    settings: UserSettings = UserSettings()
): OutfitRecommendation {
    val normalized = request.lowercase()
    val feels = current.apparentTemperatureC ?: current.temperatureC
    val personalizedFeels = feels + when (settings.temperatureSensitivity) {
        TemperatureSensitivity.COLD_SENSITIVE -> -3
        TemperatureSensitivity.BALANCED -> 0
        TemperatureSensitivity.HEAT_SENSITIVE -> 3
    }
    val rain = daily.firstOrNull()?.precipitationProbability ?: 0
    val wind = current.windSpeedKmh ?: 0.0
    val uv = daily.firstOrNull()?.uvIndexMax ?: 0.0

    val wardrobeRequested = "dolap" in normalized || "dolab" in normalized
    val wardrobeOnly = wardrobeRequested && wardrobe.isNotEmpty()
    fun owned(category: String, fallback: String): String = wardrobe
        .firstOrNull { it.category == category }
        ?.let { "${it.name} (${it.color})" }
        ?: fallback

    val weatherTop = when {
        personalizedFeels <= 8 -> "Isı tutan ince kazak"
        personalizedFeels <= 17 -> "İnce triko veya uzun kollu üst"
        personalizedFeels >= 28 -> "Nefes alan pamuklu/keten üst"
        else -> "Hafif gömlek veya tişört"
    }
    val weatherOuterwear = when {
        "daha sıcak" in normalized || personalizedFeels <= 8 -> "Yün karışımlı uzun mont"
        "daha hafif" in normalized || personalizedFeels >= 28 -> "Gerekmez; ince bir overshirt yeterli"
        personalizedFeels <= 17 -> "Çıkarılabilir hafif ceket"
        else -> "İnce overshirt veya blazer"
    }
    val (title, bottom, shoes, accessory) = when {
        listOf("şık", "sik", "davet", "özel").any(normalized::contains) ->
            listOf("Zarif şehir görünümü", "kumaş pantolon veya midi etek", "Kapalı burun loafer", "Minimal takı + yapılandırılmış çanta")
        listOf("iş", "ofis", "toplantı").any(normalized::contains) ->
            listOf("Modern ofis görünümü", "Düz kesim pantolon", "Loafer veya sade deri sneaker", "İnce kemer + düzenli tote")
        listOf("spor", "yürüyüş", "kosu", "koşu").any(normalized::contains) ->
            listOf("Hareketli gün seti", "Esnek, teri uzaklaştıran alt", "Destekli spor ayakkabı", "Hafif bel çantası")
        listOf("okul", "kampüs", "ders").any(normalized::contains) ->
            listOf("Kampüs için katmanlı görünüm", "Rahat düz paça pantolon", "Gün boyu rahat sneaker", "Sırt çantası + ince çorap")
        settings.stylePreference == StylePreference.ELEGANT ->
            listOf("Zarif günlük görünüm", "Akışkan kesimli pantolon veya midi etek", "Sade loafer", "Minimal takı + küçük çanta")
        settings.stylePreference == StylePreference.SPORTY ->
            listOf("Sportif şehir görünümü", "Teknik dokulu rahat alt", "Destekli sneaker", "Hafif çapraz çanta")
        settings.stylePreference == StylePreference.MINIMAL ->
            listOf("Minimal kapsül görünüm", "Temiz kesimli düz paça alt", "Monokrom sneaker veya loafer", "Tek bir sade aksesuar")
        settings.stylePreference == StylePreference.BUSINESS ->
            listOf("Akıllı ofis görünümü", "Düz kesim kumaş pantolon", "Loafer veya sade deri sneaker", "Yapılandırılmış tote")
        else -> listOf("Günlük dengeli görünüm", "Rahat düz paça alt", "Hava değişimine uygun sneaker", "Küçük çapraz çanta")
    }
    val protection = buildList {
        if (rain >= 40) add("Kompakt şemsiye + suya dayanıklı katman")
        if (wind >= 35) add("Rüzgâr kesen veya kapüşonlu parça")
        if (uv >= 3) add("Gözlük + geniş spektrumlu SPF 30+")
        if (isEmpty()) add("Ek hava koruması gerekmiyor")
    }.joinToString(" · ")
    val palette = when {
        rain >= 40 -> "Lacivert · taş · yumuşak lila"
        uv >= 6 -> "Krem · açık mavi · şeftali"
        personalizedFeels <= 12 -> "Bordo · kömür · kırık beyaz"
        else -> "Petrol yeşili · krem · şeftali"
    }
    val reason = listOf(
        "hissedilen ${feels.roundToInt()}°C",
        "yağış %$rain",
        "rüzgâr ${wind.roundToInt()} km/sa",
        "UV ${uv.roundToInt()}",
        when (settings.temperatureSensitivity) {
            TemperatureSensitivity.COLD_SENSITIVE -> "üşüme hassasiyetine göre +1 katman"
            TemperatureSensitivity.HEAT_SENSITIVE -> "ısınma hassasiyetine göre daha nefes alan katman"
            TemperatureSensitivity.BALANCED -> "dengeli sıcaklık tercihi"
        }
    ).joinToString(" · ")

    val finalTop = if (wardrobeOnly) owned("Üst", "Dolabında uygun üst parça yok") else weatherTop
    val finalBottom = if (wardrobeOnly) owned("Alt", "Dolabında uygun alt parça yok") else bottom
    val finalOuterwear = if (wardrobeOnly) owned("Dış giyim", "Dolabında uygun dış katman yok") else weatherOuterwear
    val finalShoes = if (wardrobeOnly) owned("Ayakkabı", "Dolabında uygun ayakkabı yok") else shoes
    val finalAccessory = if (wardrobeOnly) owned("Aksesuar", "İsteğe bağlı aksesuar") else accessory

    return OutfitRecommendation(
        title = when {
            wardrobeOnly -> "Dolabından hava uyumlu görünüm"
            settings.outfitPriority == OutfitPriority.COMFORT -> "Konfor odaklı · $title"
            settings.outfitPriority == OutfitPriority.ELEGANCE -> "Şıklık odaklı · $title"
            else -> title
        },
        top = finalTop,
        bottom = finalBottom,
        outerwear = finalOuterwear,
        shoes = finalShoes,
        accessory = finalAccessory,
        protection = protection,
        colorPalette = palette,
        reason = reason
    )
}

fun Forecast.styleReply(request: String, wardrobe: List<WardrobeItem> = emptyList(), settings: UserSettings = UserSettings()): String =
    outfitRecommendation(request, wardrobe, settings).let { outfit ->
        "${outfit.title}: ${outfit.top}; ${outfit.bottom}; ${outfit.outerwear}; ${outfit.shoes}. " +
            "${outfit.protection}.\n\nNeden: ${outfit.reason}"
    }
