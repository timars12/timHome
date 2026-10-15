package com.timhome.core.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.timhome.core.common.navigation.Navigator
import com.timhome.core.common.navigation.Route

interface ViewModelAssistedFactory<T : ViewModel> {
    fun create(handle: SavedStateHandle): T

    /**
     * Called by [ViewModelFactory] with the [route] of the entry the ViewModel belongs to and a
     * [navigator] whose commands carry that route. ViewModels that navigate or take route
     * arguments override it; the others keep this default.
     */
    fun create(
        handle: SavedStateHandle,
        route: Route,
        navigator: Navigator,
    ): T = create(handle)
}
