package com.example.core.di

import androidx.compose.runtime.Stable
import com.example.core.utils.viewmodel.ViewModelFactory
import javax.inject.Inject

@Stable
class ViewModelFactoryContainer {
    @Inject
    lateinit var viewModelFactory: dagger.Lazy<ViewModelFactory>
}
