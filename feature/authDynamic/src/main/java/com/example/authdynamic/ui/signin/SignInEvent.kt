package com.example.authdynamic.ui.signin

sealed interface SignInEvent {
    object GoToHomeScreen : SignInEvent
    data class ShowErrorMessage(val error: String) : SignInEvent
}
