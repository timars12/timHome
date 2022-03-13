package com.example.authdynamic.di

import com.example.authdynamic.data.AuthorizationRepositoryImpl
import com.example.authdynamic.data.api.AuthApi
import com.example.authdynamic.domain.IAuthorizationRepository
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
class LoginModule {

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    fun provideAuthorizationRepository(repository: AuthorizationRepositoryImpl): IAuthorizationRepository =
        repository
}