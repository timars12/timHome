package com.example.settings.presentation

import androidx.lifecycle.ViewModel
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.core.utils.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface SettingModule {

    @Binds
    @IntoMap
    @ViewModelKey(SettingViewModel::class)
    fun bindVMFactory(f: SettingViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}