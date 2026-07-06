package com.timhome.settings.ui

import androidx.lifecycle.ViewModel
import com.timhome.core.utils.viewmodel.ViewModelAssistedFactory
import com.timhome.core.utils.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface SettingModule {
    @Binds
    @IntoMap
    @ViewModelKey(SettingViewModel::class)
    fun bindVMFactory(f: SettingViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}
