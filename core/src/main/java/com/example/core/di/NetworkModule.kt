package com.example.core.di

import com.example.core.data.api.ArduinoApi
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {
    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder().apply {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        addInterceptor(loggingInterceptor)
        connectTimeout(timeout = 1, TimeUnit.MINUTES)
        readTimeout(timeout = 1, TimeUnit.MINUTES)
        writeTimeout(timeout = 1, TimeUnit.MINUTES)
    }.build()


    @Singleton
    @Provides
    fun provideRetrofitClient(httpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://demo.treblle.com/api/v1/")
            .client(httpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .build()
    }

    @Singleton
    @Provides
    fun provideApi(retrofit: Retrofit): ArduinoApi {
        return retrofit.create(ArduinoApi::class.java)
    }
}
