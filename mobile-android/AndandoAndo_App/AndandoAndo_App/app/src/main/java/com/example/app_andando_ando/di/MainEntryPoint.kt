package com.example.app_andando_ando.di

import com.example.app_andando_ando.auth.SessionManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * EntryPoint para obtener beans Hilt desde Activity (EntryPointAccessors).
 * Lo usamos para obtener SessionManager en MainActivity sin viewModel.
 */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface MainActivityEntryPoint {
    fun sessionManager(): SessionManager
}

