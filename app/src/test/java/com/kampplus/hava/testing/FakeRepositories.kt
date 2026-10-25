package com.kampplus.hava.testing

import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.model.CurrentWeather
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.SearchScope
import java.time.LocalDateTime

/**
 * Testlerde gerçek repository yerine kullanılan sahteler.
 *
 * Repository'yi arayüz olarak tanımlamamızın sebebi tam olarak budur: ViewModel
 * testleri internete çıkmadan, istenen her sonucu (başarı / boş / hata)
 * kolayca üretebilir.
 */

/**
 * @param result Her çağrıda dönecek liste.
 * @param error Doluysa [search] bu hatayı fırlatır.
 */
class FakeCityRepository(
    var result: List<City> = emptyList(),
    var error: Throwable? = null
) : CityRepository {

    /** Son aranan metin; ViewModel'in doğru sorguyu ilettiğini doğrulamak için. */
    var lastQuery: String? = null
        private set

    var lastScope: SearchScope? = null
        private set

    /** Kaç kez arama yapıldığı; "Tekrar dene" davranışını doğrulamak için. */
    var callCount: Int = 0
        private set

    override suspend fun search(query: String, scope: SearchScope): List<City> {
        lastQuery = query
        lastScope = scope
        callCount++
        error?.let { throw it }
        return result
    }
}

/** @param error Doluysa [getForecast] bu hatayı fırlatır. */
class FakeWeatherRepository(
    var result: Forecast = sampleForecast(),
    var error: Throwable? = null
) : WeatherRepository {

    var callCount: Int = 0
        private set

    override suspend fun getForecast(latitude: Double, longitude: Double): Forecast {
        callCount++
        error?.let { throw it }
        return result
    }
}

/** Testlerde tekrar tekrar yazmamak için hazır bir şehir. */
fun sampleCity(id: Long = 323786L, name: String = "Ankara") = City(
    id = id,
    name = name,
    latitude = 39.91987,
    longitude = 32.85427,
    countryCode = "TR",
    country = "Türkiye",
    admin1 = "Ankara"
)

/** Testlerde tekrar tekrar yazmamak için hazır bir tahmin. */
fun sampleForecast() = Forecast(
    current = CurrentWeather(
        temperatureC = 21.0,
        weatherCode = 0,
        observedAt = LocalDateTime.parse("2026-09-24T11:30")
    ),
    hourly = emptyList(),
    daily = emptyList()
)
