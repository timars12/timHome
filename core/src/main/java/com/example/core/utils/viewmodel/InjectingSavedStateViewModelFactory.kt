package com.example.core.utils.viewmodel

import android.os.Bundle
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import dagger.Reusable
import javax.inject.Inject

@Reusable
class InjectingSavedStateViewModelFactory @Inject constructor(
    private val assistedFactories: Map<Class<out ViewModel>,
        @JvmSuppressWildcards ViewModelAssistedFactory<out ViewModel>>
) {
    fun create(owner: SavedStateRegistryOwner, defaultArgs: Bundle? = null): AbstractSavedStateViewModelFactory {
        return object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {

            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                key: String,
                modelClass: Class<T>,
                handle: SavedStateHandle
            ): T {
                // Attempt to get ViewModel from assisted inject factories
                assistedFactories[modelClass]?.let {
                    try {
                        return it.create(handle) as T
                    } catch (exception: Exception) {
                        throw RuntimeException(exception)
                    }
                } ?: throw IllegalArgumentException("Unknown model class $modelClass")
            }
        }
    }
}
