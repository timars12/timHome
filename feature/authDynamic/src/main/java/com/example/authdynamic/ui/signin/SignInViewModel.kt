package com.example.authdynamic.ui.signin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.authdynamic.domain.IAuthorizationRepository
import com.example.core.utils.NavigationDispatcher
import com.example.core.utils.mvi.MviViewModel
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
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
        private val navigationDispatcher: NavigationDispatcher,
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
            navigationDispatcher.emit {
                it.navigate("homeScreen") {
                    popUpTo("signInScreen") { inclusive = true }
                }
            }
        }

        @AssistedFactory
        interface Factory : ViewModelAssistedFactory<SignInViewModel> {
            override fun create(handle: SavedStateHandle): SignInViewModel
        }
    }
