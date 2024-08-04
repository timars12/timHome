package com.example.authdynamic.ui.signin.di

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.example.authdynamic.ui.signin.SignInViewModel
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.example.core.utils.viewmodel.ViewModelFactory
import com.example.core.utils.viewmodel.ViewModelKey
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Inject

@Module
internal interface SignInModule {
    @Binds
    @IntoMap
    @ViewModelKey(SignInViewModel::class)
    fun bindVMFactory(f: SignInViewModel.Factory): ViewModelAssistedFactory<out ViewModel>
}

@Stable
internal class SignInContainer {
    @Inject lateinit var viewModelFactory: dagger.Lazy<ViewModelFactory>
}
