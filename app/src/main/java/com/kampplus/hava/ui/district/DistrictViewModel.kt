package com.kampplus.hava.ui.district

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kampplus.hava.data.DistrictRepository
import com.kampplus.hava.data.RemoteDistrictRepository
import com.kampplus.hava.model.City
import com.kampplus.hava.ui.LoadState
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

class DistrictViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: DistrictRepository
) : ViewModel() {
    constructor(savedStateHandle: SavedStateHandle) : this(savedStateHandle, RemoteDistrictRepository())

    val provinceName: String = checkNotNull(savedStateHandle[ARG_PROVINCE_NAME])
    val provinceLatitude: Double = checkNotNull(savedStateHandle.get<String>(ARG_LATITUDE)).toDouble()
    val provinceLongitude: Double = checkNotNull(savedStateHandle.get<String>(ARG_LONGITUDE)).toDouble()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<LoadState<List<City>>>(LoadState.Loading)
    val uiState: StateFlow<LoadState<List<City>>> = _uiState.asStateFlow()

    private var districts: List<City> = emptyList()

    init {
        load()
    }

    fun onQueryChange(value: String) {
        _query.value = value
        publishFiltered()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = LoadState.Loading
            _uiState.value = try {
                districts = repository.districts(provinceName)
                filteredState()
            } catch (error: IOException) {
                Log.e(TAG, "İlçeler yüklenemedi", error)
                LoadState.Error("İlçeler için internet bağlantını kontrol et.")
            } catch (error: HttpException) {
                Log.e(TAG, "İlçeler yüklenemedi: HTTP ${error.code()}", error)
                LoadState.Error("İlçe servisi şu an cevap vermiyor.")
            }
        }
    }

    private fun publishFiltered() {
        if (districts.isNotEmpty()) _uiState.value = filteredState()
    }

    private fun filteredState(): LoadState<List<City>> {
        val queryValue = _query.value.trim()
        val filtered = if (queryValue.isEmpty()) {
            districts
        } else {
            districts.filter {
                it.name.contains(queryValue, ignoreCase = true)
            }
        }
        return if (filtered.isEmpty()) LoadState.Empty else LoadState.Success(filtered)
    }

    companion object {
        const val ARG_PROVINCE_NAME = "provinceName"
        const val ARG_LATITUDE = "latitude"
        const val ARG_LONGITUDE = "longitude"
        private const val TAG = "DistrictViewModel"
    }
}
