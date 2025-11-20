package com.example.app_andando_ando.data.dto

data class LoginResponseDto(
    val user: UserDto,
    val token: String
)