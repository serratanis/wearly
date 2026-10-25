package com.kampplus.hava.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kampplus.hava.model.City
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Kullanıcının seçtiği şehirleri cihazda tutar; ağ veya hesap gerektirmez. */
class FavoriteCityStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _cities = MutableStateFlow(readCities())
    val cities: StateFlow<List<City>> = _cities.asStateFlow()

    fun toggle(city: City) {
        val current = _cities.value
        persist(
            if (current.any { it.sameLocationAs(city) }) {
                current.filterNot { it.sameLocationAs(city) }
            } else {
                current.plus(city).sortedBy(City::name)
            }
        )
    }

    private fun readCities(): List<City> {
        val json = preferences.getString(KEY_CITIES, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<City>>() {}.type
            gson.fromJson<List<City>>(json, type).orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun persist(cities: List<City>) {
        preferences.edit().putString(KEY_CITIES, gson.toJson(cities)).apply()
        _cities.value = cities
    }

    private fun City.sameLocationAs(other: City): Boolean = id == other.id && latitude == other.latitude && longitude == other.longitude

    private companion object {
        const val PREFERENCES_NAME = "wearly_favorite_cities"
        const val KEY_CITIES = "cities"
    }
}
