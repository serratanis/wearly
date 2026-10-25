package com.kampplus.hava.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kampplus.hava.model.StyleMessage

/** Stil asistanı konuşmalarını şehir bazında yalnızca cihazda saklar. */
class ChatHistoryStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun messagesFor(cityName: String): List<StyleMessage> = readAll()[cityName.normalizedKey()].orEmpty()

    fun replace(cityName: String, messages: List<StyleMessage>) {
        val conversations = readAll().toMutableMap()
        conversations[cityName.normalizedKey()] = messages.takeLast(MAX_MESSAGES)
        persist(conversations)
    }

    fun clear(cityName: String) {
        val conversations = readAll().toMutableMap()
        conversations.remove(cityName.normalizedKey())
        persist(conversations)
    }

    private fun readAll(): Map<String, List<StyleMessage>> {
        val json = preferences.getString(KEY_CONVERSATIONS, null) ?: return emptyMap()
        return runCatching {
            val type = object : TypeToken<Map<String, List<StyleMessage>>>() {}.type
            gson.fromJson<Map<String, List<StyleMessage>>>(json, type).orEmpty()
        }.getOrDefault(emptyMap())
    }

    private fun persist(conversations: Map<String, List<StyleMessage>>) {
        preferences.edit().putString(KEY_CONVERSATIONS, gson.toJson(conversations)).apply()
    }

    private fun String.normalizedKey() = trim().lowercase()

    private companion object {
        const val PREFERENCES_NAME = "wearly_chat_history"
        const val KEY_CONVERSATIONS = "conversations"
        const val MAX_MESSAGES = 60
    }
}
