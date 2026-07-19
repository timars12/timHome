package com.timhome.soilmoisture.ui.potedit

import androidx.lifecycle.ViewModel
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.timhome.core.ui.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface PotEditModule {
    @Binds
    @IntoMap
    @ViewModelKey(PotEditViewModel::class)
    fun bindVMFactory(f: PotEditViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}
