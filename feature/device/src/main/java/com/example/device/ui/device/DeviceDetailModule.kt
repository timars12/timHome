package com.example.device.ui.device

import androidx.lifecycle.ViewModel
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.core.utils.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface DeviceDetailModule {
    @Binds
    @IntoMap
    @ViewModelKey(DeviceDetailViewModel::class)
    fun bindVMFactory(f: DeviceDetailViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}
