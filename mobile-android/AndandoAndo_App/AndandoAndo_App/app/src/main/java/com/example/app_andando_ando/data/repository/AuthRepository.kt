package com.example.app_andando_ando.data.repository

import com.example.app_andando_ando.domain.model.*


interface AuthRepository {
    suspend fun login(request: LoginRequest): ResponseDto<LoginResponse>
    suspend fun register(request: RegisterRequest): ResponseDto<Any>
}