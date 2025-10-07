package com.example.app_andando_ando.domain.model

import com.example.app_andando_ando.domain.model.User

data class LoginRequest(
    val userName: String,
    val password: String
)


data class LoginResponse(
    val user: User,
    val token: String
)


data class RegisterRequest(
    val email: String,
    val name: String,
    val phoneNumber: String,
    val password: String,
    val role: String = "USER"
)


// Generic wrapper to map API response
data class ResponseDto<T>(
    val result: T?,
    val isSuccess: Boolean,
    val message: String
)