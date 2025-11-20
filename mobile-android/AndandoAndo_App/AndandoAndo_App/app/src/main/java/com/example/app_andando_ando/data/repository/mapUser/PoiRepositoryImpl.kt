package com.example.app_andando_ando.data.repository.mapUser

import com.example.app_andando_ando.data.api.PoiApi
import com.example.app_andando_ando.domain.model.mapUser.Poi
import com.example.app_andando_ando.domain.model.mapUser.MenuItem
import com.example.app_andando_ando.domain.model.mapUser.OpeningHour
import javax.inject.Inject

class PoiRepositoryImpl @Inject constructor(private val api: PoiApi) : PoiRepository {

    override suspend fun getPois(lat: Double, lng: Double, radius: Int, category: String?, name: String?): List<Poi> {
        val dtos = api.getPois(lat, lng, radius, category, name)
        return dtos.map { it.toDomain() }
    }

    override suspend fun getPoiById(id: String): Poi {
        val dto = api.getPoiById(id)
        return dto.toDomain()
    }

    // mapeos locales
    private fun com.example.app_andando_ando.data.dto.mapUser.PoiDto.toDomain(): Poi {
        return Poi(
            poiId = this.poiId,
            businessId = this.businessId,
            name = this.name,
            category = this.category,
            description = this.description,
            latitude = this.latitude,
            longitude = this.longitude,
            imageUrl = this.imageUrl,
            menu = this.menu?.map { MenuItem(it.menuItemId, it.name, it.description, it.price) } ?: emptyList(),
            openingHours = this.openingHours?.map { OpeningHour(it.dayOfWeek, it.open, it.close) } ?: emptyList(),
            averageRating = this.averageRating,
            isActive = this.isActive,
            imageLocalPath = this.imageLocalPath
        )
    }
}
