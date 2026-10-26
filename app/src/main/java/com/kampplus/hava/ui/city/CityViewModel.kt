package com.kampplus.hava.ui.city

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.data.OpenMeteoCityRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.model.SearchScope
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

/**
 * Şehir listesi ekranının beyni.
 *
 * Görev dağılımı:
 * - Ekran (Composable) yalnızca çizer.
 * - ViewModel state'i tutar ve kararları verir.
 * - Repository veriyi getirir.
 *
 * ViewModel ekran döndüğünde (Activity yeniden kurulurken) yaşamaya devam eder;
 * bu yüzden arama metni ve sonuçlar kaybolmaz.
 *
 * @param repository Varsayılanı gerçek servis. Testlerde sahte bir repository verilir.
 *   Tüm parametrelerin varsayılanı olduğu için Kotlin parametresiz bir kurucu da üretir;
 *   ekranlarda `viewModel()` bunu kullanabilir.
 */
class CityViewModel(
    private val repository: CityRepository = OpenMeteoCityRepository()
) : ViewModel() {

    // State ViewModel'de MutableStateFlow ile tutulur.
    // Değiştirilebilir alan private'tır; dışarıya yalnızca okunabilir hâli açılır.
    private val _query = MutableStateFlow(INITIAL_QUERY)
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<LoadState<List<City>>>(LoadState.Loading)
    val uiState: StateFlow<LoadState<List<City>>> = _uiState.asStateFlow()

    private val _scope = MutableStateFlow(SearchScope.TURKEY)
    val scope: StateFlow<SearchScope> = _scope.asStateFlow()
    private var initialCityApplied = false

    init {
        // Ekran boş açılmasın diye açılışta bir arama yapılır.
        search()
    }

    /** Kullanıcı arama kutusuna her harf yazdığında çağrılır. */
    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
    }

    fun setInitialCity(city: String) {
        if (initialCityApplied) return
        initialCityApplied = true
        val cleanCity = city.trim()
        if (cleanCity.isNotEmpty() && cleanCity != _query.value) {
            _query.value = cleanCity
            search()
        }
    }

    fun onScopeChange(newScope: SearchScope) {
        if (_scope.value == newScope) return
        _scope.value = newScope
        search()
    }

    /**
     * Aramayı çalıştırır ve sonucu dört durumdan birine çevirir.
     *
     * `viewModelScope.launch`: yeni bir coroutine başlatır. Ekran kapanırsa bu kapsam
     * işi otomatik iptal eder, yarıda kalan istek sızıntı yapmaz.
     */
    fun search() {
        viewModelScope.launch {
            _uiState.value = LoadState.Loading
            _uiState.value = try {
                val cities = repository.search(_query.value, _scope.value)
                // Cevap geldi ama içi boş: bu bir hata değil, "Empty" durumudur.
                if (cities.isEmpty()) LoadState.Empty else LoadState.Success(cities)
            } catch (e: IOException) {
                // İnternet yok, DNS çözülemedi veya zaman aşımı.
                Log.e(TAG, "Arama başarısız: bağlantı hatası", e)
                LoadState.Error("İnternet bağlantını kontrol et.")
            } catch (e: HttpException) {
                // Sunucu yanıt verdi ama 4xx/5xx döndü.
                Log.e(TAG, "Arama başarısız: HTTP ${e.code()}", e)
                LoadState.Error("Sunucu şu an cevap vermiyor.")
            } catch (e: JsonSyntaxException) {
                // Beklenmeyen JSON: model ile yanıt uyuşmuyor.
                Log.e(TAG, "Arama başarısız: yanıt çözümlenemedi", e)
                LoadState.Error("Bir şeyler ters gitti. Tekrar dene.")
            }
            // Kullanıcıya sade mesaj, Logcat'e teknik ayrıntı.
        }
    }

    private companion object {
        const val TAG = "CityViewModel"

        /** Uygulama ilk açıldığında aranan şehir. */
        const val INITIAL_QUERY = "İstanbul"
    }
}
