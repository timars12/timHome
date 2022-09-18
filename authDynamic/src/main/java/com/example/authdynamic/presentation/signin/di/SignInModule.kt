package com.example.authdynamic.presentation.signin.di

import androidx.lifecycle.ViewModel
import com.example.authdynamic.presentation.signin.SignInViewModel
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.core.utils.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface SignInModule {

    @Binds
    @IntoMap
    @ViewModelKey(SignInViewModel::class)
    fun bindVMFactory(f: SignInViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}
