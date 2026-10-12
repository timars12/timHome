package com.timhome.auth.ui.signin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timhome.auth.domain.IAuthorizationRepository
import com.timhome.core.common.mvi.MviViewModel
import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.Navigator
import com.timhome.core.common.navigation.Route
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val LOGIN_VIEW_STATE = "loginViewState"

internal class SignInViewModel
    @AssistedInject
    constructor(
        repository: IAuthorizationRepository,
        firebaseAnalytics: FirebaseAnalytics,
        @Assisted private val navigator: Navigator,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) : ViewModel(), MviViewModel<LoginViewIntent, LoginViewState> {
        private val reducer = LoginReducer(firebaseAnalytics, repository, savedStateHandle)
        override val viewState: StateFlow<LoginViewState> =
            reducer.state
                .onEach {
                    if (it.isLoginSuccess) goHomeScreen() else savedStateHandle[LOGIN_VIEW_STATE] = it
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), LoginViewState.initial())

        override val singleEvent: Channel<LoginViewState>
            get() = reducer.singleEvent

        fun sendEvent(event: LoginViewIntent) {
            viewModelScope.launch {
                reducer.sendEvent(event)
            }
        }

        private fun goHomeScreen() {
            navigator.replaceAll(Home)
        }

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<SignInViewModel> {
            fun createFor(
                handle: SavedStateHandle,
                navigator: Navigator,
            ): SignInViewModel

            override fun create(
                handle: SavedStateHandle,
                route: Route,
                navigator: Navigator,
            ): SignInViewModel = createFor(handle, navigator)

            override fun create(handle: SavedStateHandle): SignInViewModel = error("needs a route and a navigator")
        }
    }
