package com.example.core.utils.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
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
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(
            modelClass: Class<T>,
            extras: CreationExtras,
        ): T {
            // Create a SavedStateHandle for this ViewModel from extras
            val savedStateHandle = extras.createSavedStateHandle()
            return factories[modelClass]?.create(savedStateHandle)!! as T
        }
    }
