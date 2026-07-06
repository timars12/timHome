package com.timhome.auth.di

import com.timhome.auth.data.AuthorizationRepositoryImpl
import com.timhome.auth.data.api.AuthApi
import com.timhome.auth.domain.IAuthorizationRepository
import com.timhome.core.di.scope.FeatureScope
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
