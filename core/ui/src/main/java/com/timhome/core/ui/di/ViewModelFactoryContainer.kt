package com.timhome.core.ui.di

import androidx.compose.runtime.Stable
import com.timhome.core.ui.viewmodel.ViewModelFactory
import javax.inject.Inject

@Stable
class ViewModelFactoryContainer {
    @Inject
    lateinit var viewModelFactory: dagger.Lazy<ViewModelFactory>
}
