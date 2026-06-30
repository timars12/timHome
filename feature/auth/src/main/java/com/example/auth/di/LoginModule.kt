package com.example.auth.di

import com.example.auth.data.AuthorizationRepositoryImpl
import com.example.auth.data.api.AuthApi
import com.example.auth.domain.IAuthorizationRepository
import com.example.core.di.scope.FeatureScope
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
internal class LoginModule {
    @Provides
    @FeatureScope
    fun provideApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    fun provideAuthorizationRepository(repository: AuthorizationRepositoryImpl): IAuthorizationRepository =
        repository
}
