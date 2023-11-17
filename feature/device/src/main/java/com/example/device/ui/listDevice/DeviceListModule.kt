package com.example.device.ui.listDevice

import androidx.lifecycle.ViewModel
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.core.utils.viewmodel.ViewModelKey
import com.example.device.data.repository.DeviceRepository
import com.example.device.domain.IDeviceRepository
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface DeviceListModule {

    @Binds
    @IntoMap
    @ViewModelKey(DeviceListViewModel::class)
    fun bindVMFactory(f: DeviceListViewModel.Factory): ViewModelAssistedFactory<out ViewModel>

    @Binds
    fun provideDeviceRepository(repository: DeviceRepository): IDeviceRepository
}
