package com.kampplus.hava.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kampplus.hava.model.DayPlan
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** My Day kayıtlarını hesap gerektirmeden yalnızca cihazda saklar. */
class DayPlanStore(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()
    private val _plans = MutableStateFlow(readPlans())
    val plans: StateFlow<List<DayPlan>> = _plans.asStateFlow()

    fun upsert(plan: DayPlan) {
        val saved = plan.copy(id = plan.id.ifBlank { UUID.randomUUID().toString() })
        val updated = _plans.value
            .filterNot { it.id == saved.id }
            .plus(saved)
            .sortedWith(compareBy(DayPlan::date, DayPlan::time))
        persist(updated)
    }

    fun delete(id: String) = persist(_plans.value.filterNot { it.id == id })

    private fun readPlans(): List<DayPlan> {
        val json = preferences.getString(KEY_PLANS, null) ?: return emptyList()
        return runCatching {
            val type = object : TypeToken<List<DayPlan>>() {}.type
            gson.fromJson<List<DayPlan>>(json, type).orEmpty()
        }.getOrDefault(emptyList())
    }

    private fun persist(plans: List<DayPlan>) {
        preferences.edit().putString(KEY_PLANS, gson.toJson(plans)).apply()
        _plans.value = plans
    }

    private companion object {
        const val PREFERENCES_NAME = "wearly_day_plans"
        const val KEY_PLANS = "plans"
    }
}
