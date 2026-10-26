package com.kampplus.hava.ui.compare

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.CityRepository
import com.kampplus.hava.data.OpenMeteoCityRepository
import com.kampplus.hava.data.OpenMeteoWeatherRepository
import com.kampplus.hava.data.WeatherRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.model.Forecast
import com.kampplus.hava.model.SearchScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ComparedCity(val city: City, val forecast: Forecast)

sealed interface CompareUiState {
    data object Idle : CompareUiState
    data object Loading : CompareUiState
    data class Success(val first: ComparedCity, val second: ComparedCity) : CompareUiState
    data class Error(val message: String) : CompareUiState
}

class CompareViewModel(
    private val cityRepository: CityRepository = OpenMeteoCityRepository(),
    private val weatherRepository: WeatherRepository = OpenMeteoWeatherRepository()
) : ViewModel() {
    private val _firstQuery = MutableStateFlow("İstanbul")
    val firstQuery: StateFlow<String> = _firstQuery.asStateFlow()

    private val _secondQuery = MutableStateFlow("Ankara")
    val secondQuery: StateFlow<String> = _secondQuery.asStateFlow()

    private val _uiState = MutableStateFlow<CompareUiState>(CompareUiState.Idle)
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    init {
        compare()
    }

    fun setFirstQuery(value: String) {
        _firstQuery.value = value
    }

    fun setSecondQuery(value: String) {
        _secondQuery.value = value
    }

    fun compare() {
        val first = _firstQuery.value.trim()
        val second = _secondQuery.value.trim()
        if (first.isBlank() || second.isBlank()) {
            _uiState.value = CompareUiState.Error("Karşılaştırmak için iki şehir yaz.")
            return
        }
        viewModelScope.launch {
            _uiState.value = CompareUiState.Loading
            _uiState.value = runCatching {
                val firstCity = findCity(first)
                val secondCity = findCity(second)
                CompareUiState.Success(
                    first = ComparedCity(firstCity, weatherRepository.getForecast(firstCity.latitude, firstCity.longitude)),
                    second = ComparedCity(secondCity, weatherRepository.getForecast(secondCity.latitude, secondCity.longitude))
                )
            }.getOrElse { error ->
                CompareUiState.Error(error.message ?: "Şehirler karşılaştırılamadı. Bağlantını kontrol et.")
            }
        }
    }

    private suspend fun findCity(query: String): City = cityRepository.search(query, SearchScope.WORLD).firstOrNull()
        ?: error("“$query” için şehir bulunamadı.")
}
