package com.example.authdynamic.presentation.signin

sealed interface SignInEvent {
    object GoToHomeScreen : SignInEvent
    data class ShowErrorMessage(val error: String) : SignInEvent
}
