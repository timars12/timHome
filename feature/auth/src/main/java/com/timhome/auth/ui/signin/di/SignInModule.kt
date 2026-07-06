package com.timhome.auth.ui.signin.di

import androidx.lifecycle.ViewModel
import com.timhome.auth.ui.signin.SignInViewModel
import com.timhome.core.utils.viewmodel.ViewModelAssistedFactory
import com.timhome.core.utils.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
internal interface SignInModule {
    @Binds
    @IntoMap
    @ViewModelKey(SignInViewModel::class)
    fun bindVMFactory(f: SignInViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}
