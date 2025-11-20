package com.example.app_andando_ando.di

import com.example.app_andando_ando.data.api.AuthApi
import com.example.app_andando_ando.data.api.PoiApi
import com.example.app_andando_ando.data.api.RoutesApi
import com.example.app_andando_ando.data.repository.AuthRepositoryImpl
import com.example.app_andando_ando.data.repository.mapUser.PoiRepositoryImpl
import com.example.app_andando_ando.data.repository.mapUser.RouteRepositoryImpl
import com.example.app_andando_ando.data.repository.AuthRepository
import com.example.app_andando_ando.data.repository.mapUser.PoiRepository
import com.example.app_andando_ando.data.repository.mapUser.RouteRepository
import com.example.app_andando_ando.utils.SecureStorage
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val AUTH_BASE_URL = "https://viajandoandoauth.runasp.net/"
    private const val MAPS_BASE_URL = "https://viajandoandomapuserpoint.runasp.net/"

    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()

    @Provides
    @Singleton
    @Named("authInterceptor")
    fun provideAuthInterceptor(storage: SecureStorage): Interceptor {
        return Interceptor { chain ->
            val reqBuilder = chain.request().newBuilder()
            val token = storage.getToken()
            if (!token.isNullOrBlank()) {
                reqBuilder.addHeader("Authorization", "Bearer $token")
            }
            reqBuilder.addHeader("Accept", "application/json")
            reqBuilder.addHeader("Content-Type", "application/json")
            chain.proceed(reqBuilder.build())
        }
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides
    @Singleton
    @Named("okHttpAuth")
    fun provideOkHttpAuth(
        @Named("authInterceptor") authInterceptor: Interceptor,
        logger: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(logger)
        .build()

    @Provides
    @Singleton
    @Named("okHttpNoAuth")
    fun provideOkHttpNoAuth(logger: HttpLoggingInterceptor): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logger)
        .build()

    @Provides
    @Singleton
    @Named("authRetrofit")
    fun provideAuthRetrofit(gson: Gson, @Named("okHttpAuth") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @Singleton
    @Named("mapsAuthRetrofit")
    fun provideMapsAuthRetrofit(gson: Gson, @Named("okHttpAuth") client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(MAPS_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    // --- APIs ---
    @Provides
    @Singleton
    fun provideAuthApi(@Named("authRetrofit") retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun providePoiApi(@Named("mapsAuthRetrofit") retrofit: Retrofit): PoiApi =
        retrofit.create(PoiApi::class.java)

    @Provides
    @Singleton
    fun provideRoutesApi(@Named("mapsAuthRetrofit") retrofit: Retrofit): RoutesApi =
        retrofit.create(RoutesApi::class.java)

    // --- Repositories ---
    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi): AuthRepository = AuthRepositoryImpl(api)

    @Provides
    @Singleton
    fun providePoiRepository(api: PoiApi): PoiRepository = PoiRepositoryImpl(api)

    // Aquí pasamos el gson al constructor de RouteRepositoryImpl (corrección)
    @Provides
    @Singleton
    fun provideRouteRepository(api: RoutesApi, gson: Gson): RouteRepository = RouteRepositoryImpl(api, gson)
}



