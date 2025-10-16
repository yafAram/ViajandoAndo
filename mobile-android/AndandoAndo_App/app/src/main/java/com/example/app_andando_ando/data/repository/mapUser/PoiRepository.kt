package com.example.app_andando_ando.domain.repository

import com.example.app_andando_ando.domain.model.mapUser.Poi


interface PoiRepository {
    suspend fun getPois(lat: Double, lng: Double, radius: Int, category: String?, name: String?): List<Poi>
    suspend fun getPoiById(id: String): Poi
}
