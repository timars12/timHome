package com.example.authdynamic.presentation.signin

import androidx.lifecycle.*
import com.example.authdynamic.domain.IAuthorizationRepository
import com.example.core.utils.CallStatus
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

class SignInViewModel @AssistedInject constructor(
    private val repository: IAuthorizationRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    val events = Channel<SignInEvent>(Channel.UNLIMITED)

    private var emailValue: String? = savedStateHandle.get<String>("email")
        set(value) {
            field = value
            savedStateHandle.set("email", value)
            _email.value = value ?: ""
        }

    private var passwordValue: String? = savedStateHandle.get<String>("password")
        set(value) {
            field = value
            savedStateHandle.set("password", value)
            _password.value = value ?: ""
        }

    private val _email = MutableLiveData<String>(emailValue)
    val email: LiveData<String> = _email

    private val _password = MutableLiveData<String>(passwordValue)
    val password: LiveData<String> = _password

    fun onEnterEmail(email: String) {
        emailValue = email
    }

    fun onEnterPassword(email: String) {
        passwordValue = email
    }

    fun onSignInByEmail() {
        if (emailValue == null || passwordValue == null) return
        viewModelScope.launch {
            when (val result = repository.loginByEmail(emailValue!!, passwordValue!!)) {
                is CallStatus.Error -> events.trySend(SignInEvent.ShowErrorMessage(result.error ?: "error"))
                is CallStatus.Success -> events.trySend(SignInEvent.GoToHomeScreen)
            }
        }
    }

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<SignInViewModel> {
        override fun create(handle: SavedStateHandle): SignInViewModel
    }

}