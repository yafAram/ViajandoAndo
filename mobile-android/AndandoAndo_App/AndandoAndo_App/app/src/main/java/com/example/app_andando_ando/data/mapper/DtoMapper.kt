package com.example.app_andando_ando.data.mapper

import com.example.app_andando_ando.data.dto.*
import com.example.app_andando_ando.domain.model.*


fun UserDto.toDomain() = User(id = id, email = email, name = name, phoneNumber = phoneNumber)
fun LoginResponseDto.toDomain() = LoginResponse(user = user.toDomain(), token = token)