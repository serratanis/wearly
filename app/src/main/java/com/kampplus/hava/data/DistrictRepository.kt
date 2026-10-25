package com.kampplus.hava.data

import com.kampplus.hava.model.City
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface DistrictRepository {
    suspend fun districts(provinceName: String): List<City>
}

class RemoteDistrictRepository(
    private val api: DistrictApi = ApiClient.districtApi,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : DistrictRepository {
    override suspend fun districts(provinceName: String): List<City> = withContext(ioDispatcher) {
        api.districts(provinceName)
            .takeIf { it.success }
            ?.data
            .orEmpty()
            .map { it.toCity(provinceName) }
            .sortedBy(City::name)
    }
}
