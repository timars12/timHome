package com.example.authdynamic.ui.signin

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.authdynamic.domain.IAuthorizationRepository
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow

class SignInViewModel @AssistedInject constructor(
    private val repository: IAuthorizationRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val events = Channel<SignInEvent>(Channel.UNLIMITED)

    val email = MutableStateFlow(savedStateHandle["email"] ?: "")
    val password = MutableStateFlow(savedStateHandle["password"] ?: "")

    fun onEnterEmail(value: String) {
        savedStateHandle["email"] = value
    }

    fun onEnterPassword(value: String) {
        savedStateHandle["password"] = value
    }

    fun onSignInByEmail() {
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
