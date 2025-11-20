package com.example.app_andando_ando.data.api

import com.example.app_andando_ando.data.dto.mapUser.RouteOptimizeRequestDto
import com.example.app_andando_ando.data.dto.mapUser.RouteOptimizeResponseDto
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface RoutesApi {
    @POST("api/Routes/optimize")
    suspend fun optimize(@Body request: RouteOptimizeRequestDto): RouteOptimizeResponseDto

    @POST("api/Routes/recalculate/{routeId}")
    suspend fun recalculate(@Path("routeId") routeId: String, @Body request: RouteOptimizeRequestDto): RouteOptimizeResponseDto

    @POST("api/Routes")
    suspend fun saveRoute(@Body body: Any): Any
}
