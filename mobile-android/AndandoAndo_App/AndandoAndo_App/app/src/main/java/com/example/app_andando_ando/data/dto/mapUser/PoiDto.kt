package com.example.app_andando_ando.data.dto.mapUser

data class PoiDto(
    val poiId: String,
    val businessId: String?,
    val name: String,
    val category: String?,
    val description: String?,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String?,
    val menu: List<MenuItemDto>?,
    val openingHours: List<OpeningHourDto>?,
    val averageRating: Double?,
    val isActive: Boolean,
    val imageLocalPath: String?
)

data class MenuItemDto(
    val menuItemId: String,
    val name: String,
    val description: String?,
    val price: Double
)

data class OpeningHourDto(
    val dayOfWeek: Int,
    val open: String,
    val close: String
)
