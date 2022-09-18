package com.example.device.presentation.listDevice

import androidx.lifecycle.ViewModel
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.core.utils.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface DeviceListModule {

    @Binds
    @IntoMap
    @ViewModelKey(DeviceListViewModel::class)
    fun bindVMFactory(f: DeviceListViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}
