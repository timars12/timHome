package com.example.authdynamic.ui.signin

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import com.example.authdynamic.data.AuthorizationRepositoryImpl
import com.example.core.data.models.FieldText
import com.example.core.utils.mvi.ErrorType
import com.example.core.utils.mvi.MviError
import com.google.firebase.analytics.FirebaseAnalytics
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class LoginReducerTest {
    private val firebaseAnalytics: FirebaseAnalytics = mockk()
    private val repository: AuthorizationRepositoryImpl = mockk()
    private val savedStateHandle: SavedStateHandle =
        SavedStateHandle(mapOf(LOGIN_VIEW_STATE to LoginViewState.initial()))
    private val reducer = LoginReducer(firebaseAnalytics, repository, savedStateHandle)

    @Test
    fun `reduce should update state with error when email is empty`() =
        runTest {
            val initialState = LoginViewState.initial()
            val event = LoginViewIntent.EnterEmail("")

            val expectedState =
                initialState.copy(
                    email =
                        initialState.email.copy(
                            data = "",
                            error =
                                MviError(
                                    ErrorType.FIELD,
                                    "This field could not be empty",
                                ),
                        ),
                )

            reducer.reduce(initialState, event)
            assertEquals(expectedState, reducer.state.value)
        }

    @Test
    fun `reduce should update state with error when password is invalid`() =
        runTest {
            val initialState =
                LoginViewState.initial().copy(
                    email = FieldText("valid@email.com", null),
                )
            val event = LoginViewIntent.EnterPassword("123")

            val expectedState =
                initialState.copy(
                    password =
                        initialState.password.copy(
                            data = "123",
                            error = MviError(ErrorType.FIELD, "Invalid value"),
                        ),
                )

            reducer.reduce(initialState, event)
            assertEquals(expectedState, reducer.state.value)
        }

    @Test
    fun `reduce should call repository loginByEmail when EmailSignIn Intent is received`() =
        runTest {
            mockkConstructor(Bundle::class)
            val initialState =
                LoginViewState.initial().copy(
                    email = FieldText("valid@email.com", null),
                    password = FieldText("Qwerty12#", null),
                )
            val event = LoginViewIntent.EmailSignIn
            val expectedState =
                initialState.copy(isLoading = false, isLoginSuccess = true, error = null)
            val flow =
                flowOf(
                    initialState.copy(
                        isLoading = false,
                        error = null,
                        isLoginSuccess = true,
                    ),
                )
            coEvery {
                repository.loginByEmail(
                    initialState,
                    initialState.email.data!!,
                    initialState.password.data!!,
                )
            } returns flow

            justRun { firebaseAnalytics.logEvent(any(), any()) }
            justRun { anyConstructed<Bundle>().putCharSequence(any(), any()) }

            reducer.reduce(initialState, event)

            assertEquals(expectedState, reducer.state.value)
            coVerify(exactly = 1) { repository.loginByEmail(any(), any(), any()) }
        }
}
