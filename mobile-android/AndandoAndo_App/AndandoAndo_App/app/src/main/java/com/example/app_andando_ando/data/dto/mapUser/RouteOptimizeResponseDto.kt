package com.example.app_andando_ando.data.dto.mapUser

data class RouteStepDto(
    val distanceMeters: Double,
    val durationSeconds: Long,
    val instruction: String?,
    val lat: Double?,
    val lng: Double?
)

data class RouteOptimizeResponseDto(
    val polyline: String,
    val totalDistanceMeters: Double,
    val totalDurationSeconds: Long,
    val steps: List<RouteStepDto>
)
