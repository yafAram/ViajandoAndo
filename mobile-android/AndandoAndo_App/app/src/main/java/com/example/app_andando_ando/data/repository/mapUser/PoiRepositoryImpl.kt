package com.example.app_andando_ando.data.repository

import com.example.app_andando_ando.data.api.PoiApi
import com.example.app_andando_ando.domain.model.mapUser.Poi
import com.example.app_andando_ando.domain.repository.PoiRepository
import javax.inject.Inject

class PoiRepositoryImpl @Inject constructor(private val api: PoiApi) : PoiRepository {
    override suspend fun getPois(lat: Double, lng: Double, radius: Int, category: String?, name: String?): List<Poi> {
        val dtos = api.getPois(lat, lng, radius, category, name)
        return dtos.map { Poi(
            poiId = it.poiId,
            businessId = it.businessId,
            name = it.name,
            category = it.category,
            description = it.description,
            latitude = it.latitude,
            longitude = it.longitude,
            isActive = it.isActive,
            createdAt = it.createdAt
        ) }
    }

    override suspend fun getPoiById(id: String): Poi {
        val it = api.getPoiById(id)
        return Poi(
            poiId = it.poiId,
            businessId = it.businessId,
            name = it.name,
            category = it.category,
            description = it.description,
            latitude = it.latitude,
            longitude = it.longitude,
            isActive = it.isActive,
            createdAt = it.createdAt
        )
    }
}
