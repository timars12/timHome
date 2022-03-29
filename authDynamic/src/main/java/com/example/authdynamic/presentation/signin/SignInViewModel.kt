package com.example.authdynamic.presentation.signin

import android.util.Log
import androidx.lifecycle.*
import com.example.authdynamic.domain.IAuthorizationRepository
import com.example.core.utils.viewmodel.ViewModelAssistedFactory
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.launch

class SignInViewModel @AssistedInject constructor(
    private val repository: IAuthorizationRepository,
    @Assisted private val savedStateHandle: SavedStateHandle
) : ViewModel() {

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
        viewModelScope.launch {
            repository.loginByEmail(emailValue!!, passwordValue!!)
        }
    }

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<SignInViewModel> {
        override fun create(handle: SavedStateHandle): SignInViewModel
    }
}