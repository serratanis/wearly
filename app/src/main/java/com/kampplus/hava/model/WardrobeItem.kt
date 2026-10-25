package com.kampplus.hava.model

data class WardrobeItem(
    val id: String,
    val name: String,
    val category: String,
    val color: String,
    val season: String,
    val style: String,
    val purpose: String = "",
    val note: String = ""
)
