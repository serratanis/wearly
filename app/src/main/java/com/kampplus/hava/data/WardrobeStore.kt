package com.kampplus.hava.data

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kampplus.hava.model.WardrobeItem
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Kullanıcının onayladığı gardırop kayıtlarını yalnızca cihazda saklar. */
class WardrobeStore(private val context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _items = MutableStateFlow(readItems())
    val items: StateFlow<List<WardrobeItem>> = _items.asStateFlow()

    fun upsert(item: WardrobeItem) {
        val saved = item.copy(id = item.id.ifBlank { UUID.randomUUID().toString() })
        _items.value.firstOrNull { it.id == saved.id }
            ?.imageFileName
            ?.takeIf { it.isNotBlank() && it != saved.imageFileName }
            ?.let(::deletePhoto)
        persist(_items.value.filterNot { it.id == saved.id }.plus(saved).sortedBy(WardrobeItem::name))
    }

    fun delete(id: String) {
        _items.value.firstOrNull { it.id == id }?.imageFileName?.takeIf(String::isNotBlank)?.let(::deletePhoto)
        persist(_items.value.filterNot { it.id == id })
    }

    /** Galeri izni geçici olsa bile fotoğrafı uygulamanın özel alanına kalıcı kopyalar. */
    fun importPhoto(uri: Uri): String? = runCatching {
        val directory = context.filesDir.resolve(PHOTO_DIRECTORY).apply { mkdirs() }
        val fileName = "${UUID.randomUUID()}.jpg"
        val target = directory.resolve(fileName)
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "Fotoğraf açılamadı" }
            target.outputStream().use(input::copyTo)
        }
        fileName
    }.getOrNull()

    private fun deletePhoto(fileName: String) {
        runCatching { context.filesDir.resolve(PHOTO_DIRECTORY).resolve(fileName).delete() }
    }

    private fun readItems(): List<WardrobeItem> {
        val json = preferences.getString(KEY_ITEMS, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<WardrobeItem>>() {}.type
            gson.fromJson<List<WardrobeItem>>(json, type).orEmpty().map { item ->
                // Eski kayıtlarda fotoğraf alanı yoktur; Gson bu alanı null bırakabilir.
                item.copy(imageFileName = item.imageFileName.orEmpty())
            }
        }.getOrDefault(emptyList())
    }

    private fun persist(items: List<WardrobeItem>) {
        preferences.edit().putString(KEY_ITEMS, gson.toJson(items)).apply()
        _items.value = items
    }

    private companion object {
        const val PREFERENCES_NAME = "wearly_wardrobe"
        const val KEY_ITEMS = "items"
        const val PHOTO_DIRECTORY = "wardrobe_photos"
    }
}
