package com.kampplus.hava.data

import com.kampplus.hava.testing.createApi
import com.kampplus.hava.testing.readResource
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Şehir arama API'sinin gerçek bir JSON yanıtını doğru okuduğunu doğrular.
 *
 * Bu test neden önemli? Gson eşlemesi **sessizce** bozulur: `@SerializedName`
 * unutulursa alan hata vermeden `null` kalır ve hata ancak ekranda fark edilir.
 * Burada gerçek yanıt dosyasıyla eşlemeyi kilitliyoruz.
 */
class CityApiTest {

    private lateinit var server: MockWebServer
    private lateinit var api: CityApi

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        api = server.createApi()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `yanittaki alanlar City modeline dogru eslenir`() = runTest {
        server.enqueue(MockResponse().setBody(readResource("geocoding_berlin.json")))

        val response = api.search("Berlin")
        val berlin = checkNotNull(response.results).first()

        assertEquals(2950159L, berlin.id)
        assertEquals("Berlin", berlin.name)
        assertEquals(52.52437, berlin.latitude, 0.00001)
        assertEquals(13.41053, berlin.longitude, 0.00001)
        // country_code -> countryCode eşlemesi @SerializedName ile yapılır.
        assertEquals("DE", berlin.countryCode)
        assertEquals("Almanya", berlin.country)
        assertEquals("Berlin", berlin.admin1)
        assertEquals("PPLC", berlin.featureCode)
        assertEquals(3426354L, berlin.population)
        assertEquals("Europe/Berlin", berlin.timezone)
    }

    @Test
    fun `eksik alanlar null kalir ve cozumleme cokmez`() = runTest {
        server.enqueue(MockResponse().setBody(readResource("geocoding_berlin.json")))

        // İkinci kayıtta country_code alanı hiç yok.
        val maryland = checkNotNull(api.search("Berlin").results)[1]

        assertNull(maryland.countryCode)
        assertEquals("Maryland", maryland.admin1)
    }

    @Test
    fun `subtitle bolge ve ulkeyi birlestirir`() = runTest {
        server.enqueue(MockResponse().setBody(readResource("geocoding_berlin.json")))

        val berlin = checkNotNull(api.search("Berlin").results).first()

        // Şehir adı ile admin1 aynıysa gereksiz tekrar yapılmaz.
        assertEquals("Almanya", berlin.subtitle)
    }

    @Test
    fun `sonuc yoksa results alani null gelir`() = runTest {
        // Open-Meteo sonuç bulamazsa "results" anahtarını hiç göndermez.
        server.enqueue(MockResponse().setBody("""{"generationtime_ms":0.4}"""))

        val response = api.search("xyzxyz")

        assertNull(response.results)
        // ViewModel bunu orEmpty() ile boş listeye çevirip Empty durumu üretir.
        assertTrue(response.results.orEmpty().isEmpty())
    }

    @Test
    fun `istek dogru yola ve parametrelerle gider`() = runTest {
        server.enqueue(MockResponse().setBody(readResource("geocoding_berlin.json")))

        api.search("Ankara")

        val request = server.takeRequest()
        val path = checkNotNull(request.path)
        assertTrue("Yol v1/search olmali: $path", path.startsWith("/v1/search"))
        assertTrue("name parametresi eksik: $path", path.contains("name=Ankara"))
        assertTrue("count varsayilani 20 olmali: $path", path.contains("count=20"))
        assertTrue("language varsayilani tr olmali: $path", path.contains("language=tr"))
    }

    @Test
    fun `ulke filtresi countryCode parametresi olarak gonderilir`() = runTest {
        server.enqueue(MockResponse().setBody(readResource("geocoding_berlin.json")))

        api.search(name = "Ankara", countryCode = "TR")

        assertTrue(checkNotNull(server.takeRequest().path).contains("countryCode=TR"))
    }
}
