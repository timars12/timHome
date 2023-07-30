package com.example.home.ui.home

import androidx.lifecycle.ViewModel
import com.example.base.BaseScope
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
    @BaseScope
    fun provideApi(retrofit: Retrofit): WeatherApi = retrofit.create(WeatherApi::class.java)
}
