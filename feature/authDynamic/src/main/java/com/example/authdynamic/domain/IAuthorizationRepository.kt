package com.example.authdynamic.domain

import com.example.authdynamic.ui.signin.LoginViewState
import kotlinx.coroutines.flow.Flow

internal interface IAuthorizationRepository {
    fun loginByEmail(oldState: LoginViewState, email: String, password: String): Flow<LoginViewState>
}
