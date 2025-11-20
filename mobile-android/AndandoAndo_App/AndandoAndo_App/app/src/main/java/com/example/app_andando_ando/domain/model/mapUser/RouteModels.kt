package com.example.app_andando_ando.domain.model.mapUser

data class Coordinate(
    val lat: Double,
    val lng: Double
)

data class Waypoint(
    val poiId: String,
    val coordinate: Coordinate
)

data class RouteRequest(
    val origin: Coordinate,
    val waypoints: List<Waypoint>
)
