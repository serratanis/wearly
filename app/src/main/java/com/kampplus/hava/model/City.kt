package com.kampplus.hava.model

import com.google.gson.annotations.SerializedName

/**
 * Open-Meteo geocoding servisinden dönen şehir arama yanıtı.
 *
 * Gson, JSON alanlarını bu data class'ın aynı isimli alanlarına kendisi doldurur;
 * bize düşen yalnızca kalıbı doğru çizmek.
 *
 * Arama sonucu bulunamazsa servis `results` alanını **hiç göndermez**. Bu yüzden
 * alan nullable'dır ve "Boş" (Empty) durumunu temsil eder.
 */
data class CitySearchResponse(
    val results: List<City>?
)

/**
 * Bir şehir. Hem ağ yanıtının modeli hem de ekranların kullandığı modeldir
 * (tek model; ayrı bir DTO katmanı yoktur).
 *
 * @param id Geocoding servisinin şehir kimliği. Navigasyonda şehri taşımak için kullanılır.
 * @param latitude Enlem. Tahmin (forecast) isteği bu koordinatla yapılır.
 * @param longitude Boylam.
 * @param countryCode Ülke kodu, örneğin "TR". Kart üzerindeki rozette gösterilir.
 * @param admin1 Şehrin bağlı olduğu il/bölge. Kartta alt yazı olarak geçer.
 */
data class City(
    val id: Long,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    // JSON'da alt çizgili (country_code), Kotlin'de camelCase (countryCode).
    // @SerializedName iki ismi birbirine bağlar.
    @SerializedName("country_code")
    val countryCode: String?,
    val country: String?,
    // Bazı kayıtlarda bölge bilgisi gelmez; soru işareti uygulamayı çökmekten korur.
    val admin1: String?,
    @SerializedName("feature_code")
    val featureCode: String? = null,
    val population: Long? = null,
    val timezone: String? = null,
    val admin2: String? = null
) {
    /** Kartta ve tahmin ekranında gösterilen tekrar içermeyen konum bilgisi. */
    val subtitle: String
        get() = listOfNotNull(
            admin1?.takeUnless { it.equals(name, ignoreCase = true) },
            displayCountry
        ).distinct().joinToString(", ")

    val displayCountry: String?
        get() = when (countryCode) {
            "TR" -> "Türkiye"
            else -> country
        }

    /** Open-Meteo'da ilçeler, adı kendi admin2 alanıyla eşleşen yerleşimlerdir. */
    val isDistrict: Boolean
        get() = countryCode == "TR" && admin2?.equals(name, ignoreCase = true) == true

    val locationType: String
        get() = when {
            isDistrict -> "İlçe"
            countryCode == "TR" -> "İl"
            else -> "Şehir"
        }
}

/** Arama kapsamı: Türkiye'nin il ve ilçeleri veya dünya genelindeki yerleşimler. */
enum class SearchScope(val countryCode: String?) {
    TURKEY("TR"),
    WORLD(null)
}
