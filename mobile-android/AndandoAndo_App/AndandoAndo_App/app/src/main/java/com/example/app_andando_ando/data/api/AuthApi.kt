package com.example.app_andando_ando.data.api

import com.example.app_andando_ando.data.dto.*
import com.example.app_andando_ando.domain.model.ResponseDto
import retrofit2.http.Body
import retrofit2.http.POST


interface AuthApi {
    @POST("api/AuthApi/login")
    suspend fun login(@Body request: LoginRequestDto): ResponseDto<LoginResponseDto>


    @POST("api/AuthApi/register")
    suspend fun register(@Body request: RegisterRequestDto): ResponseDto<Any>
}