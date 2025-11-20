package com.example.app_andando_ando.data.dto

data class RegisterRequestDto(
    val email: String,
    val name: String,
    val phoneNumber: String,
    val password: String,
    val Role: String = "USER" // NOTE: API uses capital Role in example
)