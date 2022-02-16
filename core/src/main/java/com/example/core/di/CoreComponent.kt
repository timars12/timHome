package com.example.core.di

import dagger.Component
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import javax.inject.Singleton

@Singleton
@Component(modules = [NetworkModule::class])
interface CoreComponent {
    val retrofit: Retrofit
    val okHttpClient: OkHttpClient
}