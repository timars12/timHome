package com.example.home.di

import androidx.lifecycle.ViewModel
import com.example.base.BaseScope
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.core.utils.viewmodel.ViewModelKey
import com.example.home.data.api.WeatherApi
import com.example.home.ui.HomeViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import retrofit2.Retrofit

@Module
internal class HomeModule {
    @Provides
    @IntoMap
    @ViewModelKey(HomeViewModel::class)
    fun bindVMFactory(f: HomeViewModel.Factory): ViewModelAssistedFactory<out ViewModel> = f

    @Provides
    @BaseScope
    fun provideApi(retrofit: Retrofit): WeatherApi = retrofit.create(WeatherApi::class.java)
}
