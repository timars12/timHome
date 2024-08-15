package com.example.authdynamic.ui.signin

import com.example.core.utils.mvi.MviIntent

sealed interface LoginViewIntent : MviIntent {
    data object ForgotPassword : LoginViewIntent

    data class EnterEmail(val email: String?) : LoginViewIntent

    data class EnterPassword(val password: String?) : LoginViewIntent

    data object EmailSignIn : LoginViewIntent

    data object SignInWithoutField : LoginViewIntent
}
