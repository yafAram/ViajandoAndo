package com.example.app_andando_ando.utils

data class WaypointSimple(
    val poiId: String,
    val name: String = "",
    val lat: Double,
    val lng: Double,
    val imageUrl: String? = null // <-- agregado: URL relativa/absoluta del POI (opcional)
)
