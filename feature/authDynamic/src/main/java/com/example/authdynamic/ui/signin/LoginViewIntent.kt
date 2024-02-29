package com.example.authdynamic.ui.signin

import com.example.core.utils.mvi.MviIntent

sealed interface LoginViewIntent : MviIntent {
    object ForgotPassword : LoginViewIntent

    data class EnterEmail(val email: String?) : LoginViewIntent

    data class EnterPassword(val password: String?) : LoginViewIntent

    object EmailSignIn : LoginViewIntent

    object SignInWithoutField : LoginViewIntent
}
