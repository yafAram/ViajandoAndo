package com.example.app_andando_ando.domain.model.mapUser

data class Poi(
    val poiId: String,
    val businessId: String?,
    val name: String,
    val category: String?,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String?,
    val menu: List<MenuItem> = emptyList(),
    val openingHours: List<OpeningHour> = emptyList(),
    val averageRating: Double? = null,
    val isActive: Boolean = true,
    val imageLocalPath: String? = null
)

data class MenuItem(
    val menuItemId: String,
    val name: String,
    val description: String?,
    val price: Double
)

data class OpeningHour(
    val dayOfWeek: Int,
    val open: String,
    val close: String
)
