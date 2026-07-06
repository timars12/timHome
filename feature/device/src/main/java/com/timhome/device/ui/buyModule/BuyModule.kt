package com.timhome.device.ui.buyModule

import androidx.lifecycle.ViewModel
import com.timhome.core.utils.viewmodel.ViewModelAssistedFactory
import com.timhome.core.utils.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface BuyModule {
    @Binds
    @IntoMap
    @ViewModelKey(BuyModuleViewModel::class)
    fun bindVMFactory(f: BuyModuleViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}
