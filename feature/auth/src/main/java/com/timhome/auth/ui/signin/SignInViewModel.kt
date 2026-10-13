package com.timhome.auth.ui.signin

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.timhome.auth.domain.IAuthorizationRepository
import com.timhome.core.common.mvi.MviViewModel
import com.timhome.core.data.repository.SessionRepository
import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.Navigator
import com.timhome.core.common.navigation.Route
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

const val LOGIN_VIEW_STATE = "loginViewState"
private const val TAG = "SignInViewModel"

internal class SignInViewModel
    @AssistedInject
    constructor(
        repository: IAuthorizationRepository,
        firebaseAnalytics: FirebaseAnalytics,
        private val sessionRepository: SessionRepository,
        @Assisted private val navigator: Navigator,
        @Assisted private val savedStateHandle: SavedStateHandle,
    ) : ViewModel(), MviViewModel<LoginViewIntent, LoginViewState> {
        private val reducer = LoginReducer(firebaseAnalytics, repository, savedStateHandle)

        // The success state is emitted again whenever the screen resubscribes.
        private var isGoingHome = false

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
            if (isGoingHome) return
            isGoingHome = true
            viewModelScope.launch {
                try {
                    sessionRepository.markSignedIn()
                } catch (e: Exception) {
                    // Sign-in still succeeds; the next launch just shows Sign In again.
                    Log.e(TAG, "goHomeScreen() could not remember the sign-in", e)
                }
                navigator.replaceAll(Home)
            }
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
