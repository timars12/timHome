package com.example.authdynamic.data

import com.example.authdynamic.data.api.AuthApi
import com.example.authdynamic.data.api.response.LoginResponse
import com.example.authdynamic.data.api.response.UserResponse
import com.example.authdynamic.ui.signin.LoginViewState
import com.example.core.data.AppDatabase
import com.example.core.utils.Constant.CODE_200
import com.example.core.utils.mvi.ErrorType
import com.example.core.utils.mvi.MviError
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import retrofit2.Response

class AuthorizationRepositoryImplTest {
    private val apiService = mockk<AuthApi>()
    private val database = mockk<AppDatabase>()
    private val repository = AuthorizationRepositoryImpl(apiService, database)
    private val initialState = LoginViewState.initial()
    private val email = "valid@email.com"
    private val password = "password"

    @Test
    fun `loginByEmail should emit success state when login is successful`() =
        runTest {
            val onStartState = initialState.copy(isLoading = true)
            val expectedState =
                initialState.copy(
                    isLoading = false,
                    error = null,
                    isLoginSuccess = true,
                )

            val response = mockk<Response<LoginResponse>>()
            coEvery { apiService.loginByEmail(any()) } returns response
            coEvery { response.code() } returns CODE_200
            coEvery { response.body() } returns
                LoginResponse(
                    code = 20,
                    user = UserResponse("username", "token", "", ""),
                )
            coEvery { database.userDao().saveUserToDB(any()) } just Runs

            val flow = repository.loginByEmail(initialState, email, password)
            val firstState = flow.first()
            assertEquals(onStartState, firstState)
            val finalState = flow.last()
            assertEquals(expectedState, finalState)

            coVerify(exactly = 1) { apiService.loginByEmail(any()) }
            coVerify(exactly = 1) { database.userDao().saveUserToDB(any()) }
        }

    @Test
    fun `loginByEmail should emit error state when API returns non-200 response code`() =
        runTest {
            val expectedState =
                initialState.copy(
                    isLoading = false,
                    error = MviError(type = ErrorType.TOAST, errorMessage = "loginByEmail"),
                )

            val response = mockk<Response<LoginResponse>>()
            coEvery { apiService.loginByEmail(any()) } returns response
            coEvery { response.code() } returns 401
            coEvery { database.userDao().saveUserToDB(any()) } just Runs

            val flow = repository.loginByEmail(initialState, email, password)

            val actualState = flow.last()

            assertEquals(expectedState, actualState)
            coVerify { apiService.loginByEmail(any()) }
            coVerify { database.userDao() wasNot Called }
        }

    @Test
    fun `loginByEmail should emit error state when an exception occurs`() =
        runTest {
            val expectedState =
                initialState.copy(
                    isLoading = false,
                    error = MviError(type = ErrorType.TOAST, errorMessage = "No Internet connection"),
                )

            coEvery { apiService.loginByEmail(any()) } throws IOException("No Internet connection")

            val flow = repository.loginByEmail(initialState, email, password)
            val actualState = flow.last()

            assertEquals(expectedState, actualState)
            coVerify { apiService.loginByEmail(any()) }
            coVerify { database.userDao() wasNot Called }
        }
}
