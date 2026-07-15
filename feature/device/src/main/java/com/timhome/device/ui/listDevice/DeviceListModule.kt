package com.timhome.device.ui.listDevice

import androidx.lifecycle.ViewModel
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.timhome.core.ui.viewmodel.ViewModelKey
import com.timhome.device.data.repository.DeviceRepository
import com.timhome.device.domain.IDeviceRepository
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
