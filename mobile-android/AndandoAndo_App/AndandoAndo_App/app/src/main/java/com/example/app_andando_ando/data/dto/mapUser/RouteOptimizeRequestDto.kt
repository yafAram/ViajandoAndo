package com.example.app_andando_ando.data.dto.mapUser

data class CoordinateDto(val lat: Double, val lng: Double)

data class WaypointDto(
    val poiId: String? = null,
    val coordinate: CoordinateDto? = null
)

data class RouteOptimizeRequestDto(
    val origin: CoordinateDto,
    val waypoints: List<WaypointDto>,
    val mode: String,
    val recalcMode: String? = null
)