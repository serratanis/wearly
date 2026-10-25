package com.kampplus.hava.data

import com.kampplus.hava.model.DistrictResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface DistrictApi {
    @GET("public/v1/cografya/il/{province}/ilceler")
    suspend fun districts(@Path("province") province: String): DistrictResponse
}
