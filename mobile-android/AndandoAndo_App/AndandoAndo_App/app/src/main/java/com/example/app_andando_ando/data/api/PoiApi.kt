package com.example.app_andando_ando.data.api

import com.example.app_andando_ando.data.dto.mapUser.PoiDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PoiApi {
    @GET("api/Pois")
    suspend fun getPois(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Int,
        @Query("category") category: String? = null,
        @Query("name") name: String? = null
    ): List<PoiDto>

    @GET("api/Pois/{id}")
    suspend fun getPoiById(@Path("id") id: String): PoiDto
}
