package com.timhome.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.timhome.core.common.navigation.ChannelNavigator
import dagger.Reusable
import javax.inject.Inject

@Reusable
class ViewModelFactory
    @Inject
    constructor(
        private val factories: Map<
            Class<out ViewModel>,
            @JvmSuppressWildcards ViewModelAssistedFactory<out ViewModel>,
            >,
        private val channelNavigator: ChannelNavigator,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras,
        ): T {
            val route = checkNotNull(extras[RouteExtrasKey]) { "ViewModel created outside a navigation entry" }
            // Create a SavedStateHandle for this ViewModel from extras
            val savedStateHandle = extras.createSavedStateHandle()
            return factories[modelClass]?.create(savedStateHandle, route, channelNavigator.issuedFrom(route))!! as T
        }
    }
