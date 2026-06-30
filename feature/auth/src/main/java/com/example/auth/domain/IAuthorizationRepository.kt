package com.example.auth.domain

import com.example.auth.ui.signin.LoginViewState
import kotlinx.coroutines.flow.Flow

internal interface IAuthorizationRepository {
    fun loginByEmail(
        oldState: LoginViewState,
        email: String,
        password: String,
    ): Flow<LoginViewState>
}
