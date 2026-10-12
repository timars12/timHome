package com.timhome.modularizationtest.ui.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.timhome.core.common.navigation.NavigationCommand
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the navigation state for the activity.
 *
 * It starts in two phases, so it can exist before its state does: the state is `null` until
 * [start] is called, unless a signed-in state was restored.
 */
internal class NavigationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    /** The state saved before process death, or `null` when nothing was saved. */
    val restored: NavigationState? = null

    private val _state = MutableStateFlow(NavigationReducer.startState(restored, isSignedIn = null))
    val state: StateFlow<NavigationState?> = _state.asStateFlow()

    /** Sets the start state. Does nothing once a state exists. */
    fun start(state: NavigationState) {
        _state.compareAndSet(null, state)
    }

    /**
     * Applies [command] to the state. Returns `true` when the activity should finish. Does
     * nothing while there is no state yet.
     */
    fun dispatch(command: NavigationCommand): Boolean {
        val current = _state.value ?: return false
        return when (val transition = NavigationReducer.reduce(current, command)) {
            is Transition.Update -> {
                _state.value = transition.state
                false
            }
            Transition.Exit -> true
            Transition.Ignore -> false
        }
    }

    companion object {
        const val KEY_STATE = "navigation_state"

        // Not created through Dagger or the features' ViewModelFactory, which requires a route.
        val Factory = viewModelFactory { initializer { NavigationViewModel(createSavedStateHandle()) } }
    }
}
