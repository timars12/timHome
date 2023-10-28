package com.example.authdynamic.di

import com.example.authdynamic.data.AuthorizationRepositoryImpl
import com.example.authdynamic.data.api.AuthApi
import com.example.authdynamic.domain.IAuthorizationRepository
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
