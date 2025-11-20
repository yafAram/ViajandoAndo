package com.example.app_andando_ando.domain.usecase

import com.example.app_andando_ando.domain.model.*
import com.example.app_andando_ando.data.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(request: LoginRequest): ResponseDto<LoginResponse> {
        return repository.login(request)
    }
}
