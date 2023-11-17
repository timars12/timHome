package com.example.authdynamic.ui.signin

import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.authdynamic.domain.IAuthorizationRepository
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel

internal class SignInViewModel @AssistedInject constructor(
    private val repository: IAuthorizationRepository,
    private val firebaseAnalytics: FirebaseAnalytics,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val events = Channel<SignInEvent>(Channel.UNLIMITED)

    val email = savedStateHandle.getStateFlow("email", "")
    val password = savedStateHandle.getStateFlow("password", "")

    fun onEnterEmail(value: String) {
        savedStateHandle["email"] = value
    }

    fun onEnterPassword(value: String) {
        savedStateHandle["password"] = value
    }

    fun onSignInByEmail() {
        firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.LOGIN,
            bundleOf(Pair(FirebaseAnalytics.Param.METHOD, "sign_in_by_email"))
        )
        events.trySend(SignInEvent.GoToHomeScreen)
//        if (emailValue == null || passwordValue == null) return
//        viewModelScope.launch {
//            when (val result = repository.loginByEmail(emailValue!!, passwordValue!!)) {
//                is CallStatus.Error -> events.trySend(SignInEvent.ShowErrorMessage(result.error ?: "error"))
//                is CallStatus.Success -> events.trySend(SignInEvent.GoToHomeScreen)
//            }
//        }
    }

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<SignInViewModel> {
        override fun create(handle: SavedStateHandle): SignInViewModel
    }
}
