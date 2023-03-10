package com.example.home.presentation.home

import androidx.lifecycle.ViewModel
import com.example.core.di.scope.FeatureScope
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.core.utils.viewmodel.ViewModelKey
import com.example.home.data.api.WeatherApi
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
class HomeModule {

    @Provides
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    fun bindVMFactory(f: HomeViewModel.Factory): ViewModelAssistedFactory<out ViewModel> = f

    @Provides
    @FeatureScope
    fun provideApi(retrofit: Retrofit): WeatherApi = retrofit.create(WeatherApi::class.java)
}
