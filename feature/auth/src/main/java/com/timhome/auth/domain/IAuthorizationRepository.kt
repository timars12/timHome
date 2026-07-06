package com.timhome.auth.domain

import com.timhome.auth.ui.signin.LoginViewState
import kotlinx.coroutines.flow.Flow

internal interface IAuthorizationRepository {
    fun loginByEmail(
        oldState: LoginViewState,
        email: String,
        password: String,
    ): Flow<LoginViewState>
}
