package com.timhome.auth.ui.signin

internal sealed interface SignInEvent {
    object GoToHomeScreen : SignInEvent

    data class ShowErrorMessage(val error: String) : SignInEvent
}
