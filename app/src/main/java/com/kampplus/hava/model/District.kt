package com.kampplus.hava.model

import com.google.gson.annotations.SerializedName

data class DistrictResponse(
    val success: Boolean,
    val data: List<District> = emptyList()
)

data class District(
    val slug: String,
    @SerializedName("ad") val name: String,
    @SerializedName("lat") val latitude: Double,
    @SerializedName("lon") val longitude: Double
) {
    fun toCity(provinceName: String): City = City(
        id = "$provinceName/$slug".hashCode().toLong(),
        name = name,
        latitude = latitude,
        longitude = longitude,
        countryCode = "TR",
        country = "Türkiye",
        admin1 = provinceName,
        featureCode = "PPLA2",
        admin2 = name
    )
}
