package com.example.app_andando_ando.domain.repository

import com.example.app_andando_ando.domain.model.mapUser.RouteOptimizeResponse


interface RouteRepository {
    suspend fun optimize(originLat: Double, originLng: Double, waypoints: List<Pair<Double,Double>>, mode: String): RouteOptimizeResponse
}
