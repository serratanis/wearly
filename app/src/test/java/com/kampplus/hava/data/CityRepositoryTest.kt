package com.kampplus.hava.data

import com.kampplus.hava.model.City
import com.kampplus.hava.model.CitySearchResponse
import com.kampplus.hava.model.SearchScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CityRepositoryTest {

    @Test
    fun `tam sehir eslesmesi one gelir ve sehir olmayan kayit elenir`() = runTest {
        val api = object : CityApi {
            override suspend fun search(name: String, count: Int, language: String, countryCode: String?) = CitySearchResponse(
                listOf(
                    City(2, "İstanbul Yeni Havalimanı", 41.2, 28.7, "TR", "Türkiye", "İstanbul", "AIRP"),
                    City(3, "İstanbulboğazı", 41.1, 29.1, "TR", "Türkiye", "İstanbul", "PPL", 200L),
                    City(1, "İstanbul", 41.01, 28.97, "TR", "Türkiye", "İstanbul", "PPLA", 15_600_000L)
                )
            )
        }
        val repository = OpenMeteoCityRepository(api, Dispatchers.Unconfined)

        val results = repository.search("İstanbul", SearchScope.TURKEY)

        assertEquals("İstanbul", results.first().name)
        assertEquals(1, results.size)
        assertFalse(results.any { it.featureCode == "AIRP" })
    }

    @Test
    fun `dunya aramasinda nufussuz mahalleler elenir gercek sehirler kalir`() = runTest {
        val api = object : CityApi {
            override suspend fun search(name: String, count: Int, language: String, countryCode: String?) = CitySearchResponse(
                listOf(
                    City(1, "Londra", 51.5, -0.1, "GB", "Birleşik Krallık", "İngiltere", "PPLC", 8_961_989L),
                    City(2, "London", 42.9, -81.2, "CA", "Kanada", "Ontario", "PPL", 422_324L),
                    City(3, "London Old Town", 51.5, -0.1, "GB", "Birleşik Krallık", "İngiltere", "PPLX")
                )
            )
        }
        val repository = OpenMeteoCityRepository(api, Dispatchers.Unconfined)

        val results = repository.search("London", SearchScope.WORLD)

        assertEquals(listOf("Londra", "London"), results.map(City::name))
    }

    @Test
    fun `turkiye aramasinda ayni adli koy yerine resmi ilce one gelir`() = runTest {
        val api = object : CityApi {
            override suspend fun search(name: String, count: Int, language: String, countryCode: String?) = CitySearchResponse(
                listOf(
                    City(1, "Kartal", 37.1, 36.9, "TR", "Türkiye", "Gaziantep", "PPL", 1_751L, admin2 = "Nurdağı İlçesi"),
                    City(2, "Kartal", 40.9, 29.1, "TR", "Türkiye", "İstanbul", "PPL", admin2 = "Kartal")
                )
            )
        }
        val repository = OpenMeteoCityRepository(api, Dispatchers.Unconfined)

        val results = repository.search("Kartal", SearchScope.TURKEY)

        assertEquals(1, results.size)
        assertEquals("İstanbul", results.single().admin1)
        assertTrue(results.single().isDistrict)
    }
}
