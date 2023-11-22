package com.example.authdynamic.di

import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import com.example.authdynamic.domain.IAuthorizationRepository
import com.example.authdynamic.ui.signin.LOGIN_VIEW_STATE
import com.example.authdynamic.ui.signin.LoginViewIntent
import com.example.authdynamic.ui.signin.LoginViewState
import com.example.core.utils.isEmail
import com.example.core.utils.isPassword
import com.example.core.utils.mvi.ErrorType
import com.example.core.utils.mvi.MviError
import com.example.core.utils.mvi.Reducer
import com.google.firebase.analytics.FirebaseAnalytics
import kotlinx.coroutines.flow.Flow

internal class LoginReducer(
    private val firebaseAnalytics: FirebaseAnalytics,
    private val repository: IAuthorizationRepository,
    savedStateHandle: SavedStateHandle
) : Reducer<LoginViewState, LoginViewIntent>(
    savedStateHandle.get<LoginViewState>(LOGIN_VIEW_STATE) ?: LoginViewState.initial()
) {
    override suspend fun reduce(oldState: LoginViewState, event: LoginViewIntent) {
        when (event) {
            is LoginViewIntent.ForgotPassword -> Unit
            is LoginViewIntent.EmailSignIn -> {
                val state = onSignInByEmail(
                    oldState,
                    oldState.email.data!!,
                    oldState.password.data!!
                )
                setState(state)
            }

            is LoginViewIntent.EnterEmail -> {
                val error = checkField(LoginFieldType.EMAIL, event.email)
                val state = oldState.email.copy(data = event.email, error = error)
                setState(
                    oldState.copy(
                        email = state,
                        isLoginBtnEnable = state.error == null && !oldState.password.data.isNullOrEmpty() && oldState.password.error == null
                    )
                )
            }

            is LoginViewIntent.EnterPassword -> {
                val error = checkField(LoginFieldType.PASSWORD, event.password)
                val state = oldState.password.copy(data = event.password, error = error)
                setState(
                    oldState.copy(
                        password = state,
                        isLoginBtnEnable = state.error == null && !oldState.email.data.isNullOrEmpty() && oldState.email.error == null
                    )
                )
            }
        }
    }

    private fun onSignInByEmail(
        oldState: LoginViewState,
        email: String,
        password: String
    ): Flow<LoginViewState> {
        firebaseAnalytics.logEvent(
            FirebaseAnalytics.Event.LOGIN,
            bundleOf(Pair(FirebaseAnalytics.Param.METHOD, "sign_in_by_email"))
        )
        return repository.loginByEmail(oldState, email, password)
    }

    private fun checkField(type: LoginFieldType, data: String?): MviError? {
        val error = when {
            data.isNullOrEmpty() -> MviError(
                type = ErrorType.FIELD,
                errorMessage = "This field could not be empty"
            )
            type == LoginFieldType.EMAIL && data.isEmail -> null
            type == LoginFieldType.PASSWORD && data.isPassword -> null
            else -> MviError(
                type = ErrorType.FIELD,
                errorMessage = "Invalid value"
            )
        }
        return error
    }

    private enum class LoginFieldType {
        EMAIL, PASSWORD
    }
}
