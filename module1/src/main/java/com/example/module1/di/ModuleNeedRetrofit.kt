package com.example.module1.di

import com.example.core.di.scope.FeatureScope
import dagger.Module
import dagger.Provides
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

@Module
internal class ModuleNeedRetrofit {

    @FeatureScope
    @Provides
    fun needRetrofit(retrofit: Retrofit): SomeApi{
        return retrofit.create(SomeApi::class.java)
    }
}


interface SomeApi{
    @GET("character/{characterId}")
    fun getCharacter(@Path("characterId") characterId: Int): Response<Void>
}