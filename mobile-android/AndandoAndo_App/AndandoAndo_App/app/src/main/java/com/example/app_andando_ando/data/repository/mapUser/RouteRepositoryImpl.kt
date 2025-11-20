package com.example.app_andando_ando.data.repository.mapUser

import android.util.Log
import com.example.app_andando_ando.data.api.RoutesApi
import com.example.app_andando_ando.data.dto.mapUser.CoordinateDto
import com.example.app_andando_ando.data.dto.mapUser.RouteOptimizeRequestDto
import com.example.app_andando_ando.data.dto.mapUser.WaypointDto
import com.example.app_andando_ando.domain.model.mapUser.RouteOptimizeResponse
import com.example.app_andando_ando.domain.model.mapUser.RouteStep
import com.google.gson.Gson
import retrofit2.HttpException
import javax.inject.Inject

private const val TAG = "RouteRepository"

class RouteRepositoryImpl @Inject constructor(
    private val api: RoutesApi,
    private val gson: Gson
) : RouteRepository {
    override suspend fun optimize(originLat: Double, originLng: Double, waypoints: List<Pair<Double, Double>>, mode: String): RouteOptimizeResponse {
        val origin = CoordinateDto(originLat, originLng)
        val wpts = waypoints.map { WaypointDto(coordinate = CoordinateDto(it.first, it.second)) }
        val req = RouteOptimizeRequestDto(origin = origin, waypoints = wpts, mode = mode)

        // Log JSON del request (preview)
        try {
            Log.d(TAG, "➡️ optimize -> request JSON: ${gson.toJson(req)}")
        } catch (t: Throwable) {
            Log.e(TAG, "Error serializando request para logging", t)
        }

        return try {
            val res = api.optimize(req)

            // Log respuesta mapeada y JSON crudo (usando gson)
            try {
                Log.d(TAG, "⬅️ optimize -> response JSON: ${gson.toJson(res)}")
            } catch (t: Throwable) {
                Log.w(TAG, "No se pudo serializar response para log", t)
            }

            RouteOptimizeResponse(
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
        } catch (he: HttpException) {
            val errBody = try { he.response()?.errorBody()?.string() } catch (_: Throwable) { null }
            Log.e(TAG, "optimize -> HttpException code=${he.code()} errBody=$errBody", he)
            throw he
        } catch (t: Throwable) {
            Log.e(TAG, "optimize -> error llamando API", t)
            throw t
        }
    }
}

