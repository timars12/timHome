package com.example.device.ui.device

import androidx.lifecycle.ViewModel
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.core.utils.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface DeviceDetailModel {
    @Binds
    @IntoMap
    @ViewModelKey(DeviceDetailViewMode::class)
    fun bindVMFactory(f: DeviceDetailViewMode.Factory): ViewModelAssistedFactory<out ViewModel>
}
