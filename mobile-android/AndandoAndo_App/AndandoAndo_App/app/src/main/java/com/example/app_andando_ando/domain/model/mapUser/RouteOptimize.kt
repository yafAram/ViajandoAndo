package com.example.app_andando_ando.domain.model.mapUser

data class RouteStep(
    val distanceMeters: Double,
    val durationSeconds: Long,
    val instruction: String?,
    val lat: Double?,
    val lng: Double?
)

data class RouteOptimizeResponse(
    val polyline: String,
    val totalDistanceMeters: Double,
    val totalDurationSeconds: Long,
    val steps: List<RouteStep>
)