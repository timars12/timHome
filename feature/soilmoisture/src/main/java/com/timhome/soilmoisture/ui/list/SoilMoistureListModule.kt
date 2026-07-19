package com.timhome.soilmoisture.ui.list

import androidx.lifecycle.ViewModel
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.timhome.core.ui.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface SoilMoistureListModule {
    @Binds
    @IntoMap
    @ViewModelKey(SoilMoistureListViewModel::class)
    fun bindVMFactory(f: SoilMoistureListViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}
