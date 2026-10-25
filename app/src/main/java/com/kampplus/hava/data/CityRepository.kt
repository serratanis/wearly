package com.kampplus.hava.data

import com.kampplus.hava.model.City
import com.kampplus.hava.model.SearchScope
import com.kampplus.hava.model.TurkeyProvinces
import java.text.Normalizer
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Şehir verisine erişimin tek kapısı.
 *
 * ViewModel'in görevi state yönetmek, Repository'nin görevi veriyi getirmektir.
 * ViewModel verinin internetten mi yoksa sabit bir listeden mi geldiğini bilmez;
 * bu sayede veri kaynağı değişse de ViewModel'e dokunmak gerekmez.
 *
 * Arayüz olarak tanımlanmıştır ki testlerde sahte (fake) bir uygulama verilebilsin.
 */
interface CityRepository {
    /** İsme göre şehir arar. Sonuç yoksa boş liste döner. */
    suspend fun search(query: String, scope: SearchScope = SearchScope.TURKEY): List<City>
}

/**
 * Gerçek uygulama: aramayı Open-Meteo geocoding servisine sorar.
 *
 * @param api Varsayılanı [ApiClient.cityApi]; testlerde sahte bir API verilebilir.
 * @param ioDispatcher Ağ işlerinin çalışacağı thread havuzu.
 */
class OpenMeteoCityRepository(
    private val api: CityApi = ApiClient.cityApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : CityRepository {

    override suspend fun search(query: String, scope: SearchScope): List<City> = withContext(ioDispatcher) {
        // `suspend` tek başına thread değiştirmez. İşi arka plana almak
        // için withContext ile IO dispatcher'ına geçiyoruz.
        val cleanQuery = query.trim()
        val response = api.search(name = cleanQuery, countryCode = scope.countryCode)
        // Sonuç bulunamazsa servis "results" alanını hiç göndermez; orEmpty() bunu
        // boş listeye çevirir ve ViewModel bunu "Empty" durumu olarak yorumlar.
        response.results.orEmpty()
            .asSequence()
            .filter { it.isRealSettlement() }
            .filter { scope != SearchScope.TURKEY || it.isOfficialProvinceOrDistrict() }
            .distinctBy { listOf(it.name.normalized(), it.countryCode, it.admin1?.normalized(), it.admin2?.normalized()) }
            .sortedWith(cityRanking(cleanQuery, scope))
            .take(MAX_RESULTS)
            .toList()
    }

    private fun cityRanking(query: String, scope: SearchScope) = compareByDescending<City> {
        scope == SearchScope.WORLD && it.featureCode == NATIONAL_CAPITAL
    }.thenByDescending {
        it.name.normalized() == query.normalized()
    }.thenByDescending {
        scope == SearchScope.TURKEY && TurkeyProvinces.contains(it.name)
    }.thenByDescending {
        scope == SearchScope.TURKEY && it.isDistrict
    }.thenByDescending {
        scope == SearchScope.TURKEY && it.featureCode in ADMINISTRATIVE_CENTERS
    }.thenByDescending {
        it.population ?: 0L
    }.thenBy { it.name }

    private fun City.isRealSettlement(): Boolean = when (featureCode) {
        in ADMINISTRATIVE_CENTERS -> true
        "PPL" -> isDistrict || (population ?: 0L) >= MIN_CITY_POPULATION
        else -> false
    }

    private fun City.isOfficialProvinceOrDistrict(): Boolean =
        TurkeyProvinces.contains(name) || (isDistrict && admin1?.let(TurkeyProvinces::contains) == true)

    private companion object {
        const val MAX_RESULTS = 12
        const val MIN_CITY_POPULATION = 1_000L
        const val NATIONAL_CAPITAL = "PPLC"
        val ADMINISTRATIVE_CENTERS = setOf("PPLC", "PPLA", "PPLA2", "PPLA3", "PPLA4")
    }
}

private fun String.normalized(): String = Normalizer.normalize(
    lowercase(Locale.forLanguageTag("tr")),
    Normalizer.Form.NFD
).replace(Regex("\\p{Mn}+"), "")
