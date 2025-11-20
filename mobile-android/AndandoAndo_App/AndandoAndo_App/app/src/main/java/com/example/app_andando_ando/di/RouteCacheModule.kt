package com.example.app_andando_ando.di

import android.content.Context
import com.example.app_andando_ando.utils.RouteCache
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RouteCacheModule {
    @Provides
    @Singleton
    fun provideRouteCache(@ApplicationContext context: Context, gson: Gson): RouteCache =
        RouteCache(context, gson)
}
