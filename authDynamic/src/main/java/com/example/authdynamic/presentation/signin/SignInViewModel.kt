package com.example.authdynamic.presentation.signin

import androidx.lifecycle.ViewModel
import com.example.authdynamic.domain.IAuthorizationRepository
import javax.inject.Inject

class SignInViewModel @Inject constructor(private val repository: IAuthorizationRepository) :
    ViewModel() {

    fun onSignInByEmail(email: String, password: String) {
        repository.loginByEmail(email, password)
    }
}