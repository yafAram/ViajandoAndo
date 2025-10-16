package com.example.app_andando_ando.data.repository


import com.example.app_andando_ando.data.api.RoutesApi
import com.example.app_andando_ando.data.dto.mapUser.CoordinateDto
import com.example.app_andando_ando.data.dto.mapUser.RouteOptimizeRequestDto
import com.example.app_andando_ando.data.dto.mapUser.WaypointDto
import com.example.app_andando_ando.domain.model.mapUser.RouteOptimizeResponse
import com.example.app_andando_ando.domain.model.mapUser.RouteStep
import com.example.app_andando_ando.domain.repository.RouteRepository
import javax.inject.Inject

class RouteRepositoryImpl @Inject constructor(private val api: RoutesApi) : RouteRepository {
    override suspend fun optimize(originLat: Double, originLng: Double, waypoints: List<Pair<Double, Double>>, mode: String): RouteOptimizeResponse {
        val origin = CoordinateDto(originLat, originLng)
        val wpts = waypoints.map { WaypointDto(coordinate = CoordinateDto(it.first, it.second)) }
        val req = RouteOptimizeRequestDto(origin = origin, waypoints = wpts, mode = mode)
        val res = api.optimize(req)
        return RouteOptimizeResponse(
            polyline = res.polyline,
            totalDistanceMeters = res.totalDistanceMeters,
            totalDurationSeconds = res.totalDurationSeconds,
            steps = res.steps.map { s ->
                RouteStep(
                    distanceMeters = s.distanceMeters,
                    durationSeconds = s.durationSeconds,
                    instruction = s.instruction,
                    lat = s.lat,
                    lng = s.lng
                )
            }
        )
    }
}
