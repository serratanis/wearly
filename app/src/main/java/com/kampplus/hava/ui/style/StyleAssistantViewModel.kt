package com.kampplus.hava.ui.style

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.OpenMeteoWeatherRepository
import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.StyleMessage
import com.kampplus.hava.model.styleReply
import com.kampplus.hava.ui.LoadState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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

    init {
        load()
    }

    fun send(text: String) {
        val request = text.trim()
        if (request.isEmpty()) return
        val current = (_forecast.value as? LoadState.Success)?.data
        _messages.value += StyleMessage(request, true)
        _messages.value += StyleMessage(
            current?.styleReply(request) ?: "Hava verisini henüz alamadım. Bağlantı geldiğinde öneriyi güncelleyelim.",
            false
        )
    }

    fun load() {
        viewModelScope.launch {
            _forecast.value = try {
                LoadState.Success(repository.getForecast(latitude, longitude))
            } catch (_: Exception) {
                LoadState.Error("Hava verisi alınamadı.")
            }
        }
    }

    companion object {
        const val ARG_NAME = "styleName"
        const val ARG_LATITUDE = "styleLatitude"
        const val ARG_LONGITUDE = "styleLongitude"
    }
}
