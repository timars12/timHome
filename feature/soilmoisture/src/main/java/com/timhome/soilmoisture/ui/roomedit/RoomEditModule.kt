package com.timhome.soilmoisture.ui.roomedit

import androidx.lifecycle.ViewModel
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.timhome.core.ui.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface RoomEditModule {
    @Binds
    @IntoMap
    @ViewModelKey(RoomEditViewModel::class)
    fun bindVMFactory(f: RoomEditViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}
