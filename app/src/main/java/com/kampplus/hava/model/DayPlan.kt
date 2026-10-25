package com.kampplus.hava.model

data class DayPlan(
    val id: String,
    val title: String,
    val date: String,
    val time: String,
    val location: String = "",
    val eventType: String = "Günlük",
    val dressExpectation: String = "",
    val note: String = ""
)
