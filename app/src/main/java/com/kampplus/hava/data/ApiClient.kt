package com.kampplus.hava.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * İnternete giden isteklerin "garsonu".
 *
 * `object` = Kotlin'de tek örnekli (singleton) sınıf. Uygulama boyunca tek bir
 * Retrofit kurulumu yeterlidir; her istekte yeniden kurmak israf olurdu.
 *
 * `by lazy`: alan ilk kez okunduğunda kurulur. Uygulama açılırken değil, ilk
 * istek atıldığında maliyet ödenir.
 *
 * Not: Open-Meteo API anahtarı istemez. Gerçek projelerde anahtarlar koda değil,
 * `local.properties` veya `BuildConfig` üzerinden verilir.
 */
object ApiClient {

    private const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
    private const val FORECAST_BASE_URL = "https://api.open-meteo.com/"
    private const val DISTRICT_BASE_URL = "https://api.apinoktam.erenozdemir.com.tr/"

    /** Şehir arama istemcisi. Kullanımı: `ApiClient.cityApi.search("Ankara")` */
    val cityApi: CityApi by lazy { createApi(GEOCODING_BASE_URL) }

    /** Hava tahmini istemcisi. */
    val forecastApi: ForecastApi by lazy { createApi(FORECAST_BASE_URL) }

    /** Bir il seçildiğinde bağlı ilçeleri ve merkez koordinatlarını getirir. */
    val districtApi: DistrictApi by lazy { createApi(DISTRICT_BASE_URL) }

    /**
     * Verilen adres için bir Retrofit arayüzü üretir.
     *
     * `GsonConverterFactory`: gelen JSON'u data class'lara çeviren dönüştürücü.
     * `inline` + `reified T`: çağrı yerindeki tipi çalışma zamanında bilebilmek için;
     * böylece `CityApi::class.java` yazmaya gerek kalmaz.
     */
    private inline fun <reified T> createApi(baseUrl: String): T = Retrofit.Builder()
        .baseUrl(baseUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(T::class.java)
}
