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

    var emailValue: String? = savedStateHandle.get<String>("email")
        set(value) {
            field = value
            savedStateHandle.set("email", value)
        }

    private val _email = MutableLiveData<String>(emailValue)
    val email: LiveData<String> = _email

    fun onEnterEmail(email: String) {
        emailValue = email
        _email.value = email
    }

    fun onSignInByEmail(email: String, password: String) {
        viewModelScope.launch {
            repository.loginByEmail(email, password)
        }
        Log.e("0707", "some text")
    }

    @AssistedFactory
    interface Factory : ViewModelAssistedFactory<SignInViewModel> {
        override fun create(handle: SavedStateHandle): SignInViewModel
    }
}