package com.kampplus.hava.data

import com.kampplus.hava.model.CitySearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Şehir arama servisinin "menüsü".
 *
 * Retrofit bu arayüzün gövdesini kendisi yazar: biz yalnızca hangi adrese,
 * hangi parametrelerle gidileceğini tarif ederiz.
 *
 * Örnek istek:
 * `GET https://geocoding-api.open-meteo.com/v1/search?name=Ankara&count=10&language=tr`
 */
interface CityApi {

    /**
     * İsme göre şehir arar.
     *
     * `suspend`: fonksiyon durup devam edebilir. Çağrıldığı coroutine, cevap
     * beklenirken ana thread'i bloklamaz.
     *
     * @param name Aranan şehir adı.
     * @param count Kaç sonuç istendiği. Varsayılan değerler sayesinde çağrı tarafı
     *   tek parametreyle `search("Ankara")` yazabilir.
     * @param language Sonuçların dili; Türkçe isimler için "tr".
     * @param countryCode Doluysa sonuçları ISO ülke koduna göre kesin olarak süzer.
     */
    @GET("v1/search")
    suspend fun search(
        @Query("name") name: String,
        @Query("count") count: Int = 20,
        @Query("language") language: String = "tr",
        @Query("countryCode") countryCode: String? = null
    ): CitySearchResponse
}
