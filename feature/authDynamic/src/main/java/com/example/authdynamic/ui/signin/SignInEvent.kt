package com.example.authdynamic.ui.signin

internal sealed interface SignInEvent {
    object GoToHomeScreen : SignInEvent

    data class ShowErrorMessage(val error: String) : SignInEvent
}
