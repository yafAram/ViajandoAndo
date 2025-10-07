package com.example.app_andando_ando.data.repository

import com.example.app_andando_ando.data.api.AuthApi
import com.example.app_andando_ando.data.dto.LoginRequestDto
import com.example.app_andando_ando.data.dto.RegisterRequestDto
import com.example.app_andando_ando.data.mapper.toDomain
import com.example.app_andando_ando.domain.model.*


class AuthRepositoryImpl(private val api: AuthApi) : AuthRepository {
    override suspend fun login(request: LoginRequest): ResponseDto<LoginResponse> {
        val dto = LoginRequestDto(userName = request.userName, password = request.password)
        val apiResp = api.login(dto)
        return ResponseDto(
            result = apiResp.result?.let { it.toDomain() },
            isSuccess = apiResp.isSuccess,
            message = apiResp.message
        )
    }


    override suspend fun register(request: RegisterRequest): ResponseDto<Any> {
        val dto = RegisterRequestDto(
            email = request.email,
            name = request.name,
            phoneNumber = request.phoneNumber,
            password = request.password,
            Role = request.role
        )
        return api.register(dto)
    }
}