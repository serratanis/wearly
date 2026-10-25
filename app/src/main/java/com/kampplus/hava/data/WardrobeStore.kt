package com.kampplus.hava.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kampplus.hava.model.WardrobeItem
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Kullanıcının onayladığı gardırop kayıtlarını yalnızca cihazda saklar. */
class WardrobeStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _items = MutableStateFlow(readItems())
    val items: StateFlow<List<WardrobeItem>> = _items.asStateFlow()

    fun upsert(item: WardrobeItem) {
        val saved = item.copy(id = item.id.ifBlank { UUID.randomUUID().toString() })
        persist(_items.value.filterNot { it.id == saved.id }.plus(saved).sortedBy(WardrobeItem::name))
    }

    fun delete(id: String) = persist(_items.value.filterNot { it.id == id })

    private fun readItems(): List<WardrobeItem> {
        val json = preferences.getString(KEY_ITEMS, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<WardrobeItem>>() {}.type
            gson.fromJson<List<WardrobeItem>>(json, type).orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun persist(items: List<WardrobeItem>) {
        preferences.edit().putString(KEY_ITEMS, gson.toJson(items)).apply()
        _items.value = items
    }

    private companion object {
        const val PREFERENCES_NAME = "wearly_wardrobe"
        const val KEY_ITEMS = "items"
    }
}
