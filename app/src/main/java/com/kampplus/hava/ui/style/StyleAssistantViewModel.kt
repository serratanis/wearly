package com.kampplus.hava.ui.style

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonSyntaxException
import com.kampplus.hava.data.OpenMeteoWeatherRepository
import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.OutfitRecommendation
import com.kampplus.hava.model.StyleMessage
import com.kampplus.hava.model.UserSettings
import com.kampplus.hava.model.WardrobeItem
import com.kampplus.hava.model.outfitRecommendation
import com.kampplus.hava.model.styleReply
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class StyleAssistantViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: WeatherRepository
) : ViewModel() {
    constructor(savedStateHandle: SavedStateHandle) : this(savedStateHandle, OpenMeteoWeatherRepository())

    val cityName: String = savedStateHandle.get<String>(ARG_NAME).orEmpty()
    private val latitude = savedStateHandle.get<String>(ARG_LATITUDE)?.toDoubleOrNull() ?: 0.0
    private val longitude = savedStateHandle.get<String>(ARG_LONGITUDE)?.toDoubleOrNull() ?: 0.0

    private val _forecast = MutableStateFlow<LoadState<Forecast>>(LoadState.Loading)
    val forecast: StateFlow<LoadState<Forecast>> = _forecast.asStateFlow()

    private val _messages = MutableStateFlow(
        listOf(StyleMessage("Bugünkü planını anlat; gerçek hava durumuna uygun bir kombin hazırlayayım.", false))
    )
    val messages: StateFlow<List<StyleMessage>> = _messages.asStateFlow()

    private val _latestOutfit = MutableStateFlow<OutfitRecommendation?>(null)
    val latestOutfit: StateFlow<OutfitRecommendation?> = _latestOutfit.asStateFlow()

    init {
        load()
    }

    fun send(text: String, wardrobe: List<WardrobeItem> = emptyList(), settings: UserSettings = UserSettings()) {
        val request = text.trim()
        if (request.isEmpty()) return
        val current = (_forecast.value as? LoadState.Success)?.data
        _messages.value += StyleMessage(request, true)
        val wardrobeRequested = request.contains("dolap", ignoreCase = true) ||
            request.contains("dolab", ignoreCase = true)
        if (wardrobeRequested && wardrobe.isEmpty()) {
            _latestOutfit.value = null
            _messages.value +=
                StyleMessage(
                    "Dolabın henüz boş. My Wardrobe ekranından parçalarını ekledikten sonra yalnızca sahip olduklarınla kombin hazırlayabilirim.",
                    false
                )
            return
        }
        _latestOutfit.value = current?.outfitRecommendation(request, wardrobe, settings)
        _messages.value += StyleMessage(
            current?.styleReply(request, wardrobe, settings)
                ?: "Hava verisini henüz alamadım. Bağlantı geldiğinde öneriyi güncelleyelim.",
            false
        )
    }

    fun load() {
        viewModelScope.launch {
            _forecast.value = try {
                LoadState.Success(repository.getForecast(latitude, longitude))
            } catch (error: IOException) {
                Log.e(TAG, "Stil hava verisi alınamadı", error)
                LoadState.Error("İnternet bağlantını kontrol et.")
            } catch (error: HttpException) {
                Log.e(TAG, "Stil hava verisi alınamadı: HTTP ${error.code()}", error)
                LoadState.Error("Hava servisi şu an cevap vermiyor.")
            } catch (error: JsonSyntaxException) {
                Log.e(TAG, "Stil hava verisi çözümlenemedi", error)
                LoadState.Error("Hava verisi okunamadı.")
            } catch (error: IllegalStateException) {
                Log.e(TAG, "Stil hava verisi eksik", error)
                LoadState.Error("Hava verisi alınamadı.")
            }
        }
    }

    companion object {
        private const val TAG = "StyleAssistant"
        const val ARG_NAME = "styleName"
        const val ARG_LATITUDE = "styleLatitude"
        const val ARG_LONGITUDE = "styleLongitude"
    }
}
