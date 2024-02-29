package com.example.authdynamic.data

import com.example.authdynamic.data.api.AuthApi
import com.example.authdynamic.data.api.request.UserLoginRequest
import com.example.authdynamic.domain.IAuthorizationRepository
import com.example.authdynamic.ui.signin.LoginViewState
import com.example.core.data.AppDatabase
import com.example.core.utils.Constant.CODE_200
import com.example.core.utils.mvi.ErrorType
import com.example.core.utils.mvi.MviError
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

internal class AuthorizationRepositoryImpl
    @Inject
    constructor(
        private val apiService: AuthApi,
        private val database: AppDatabase,
    ) : IAuthorizationRepository {
        override fun loginByEmail(
            oldState: LoginViewState,
            email: String,
            password: String,
        ): Flow<LoginViewState> =
            flow {
                val body = UserLoginRequest(email, password)
                try {
                    val response = apiService.loginByEmail(body)
                    if (response.code() == CODE_200) {
                        response.body()?.user?.convertToUserEntity()
                            .also {
                                database.userDao().saveUserToDB(it!!)
                            }.also {
                                emit(oldState.copy(isLoading = false, error = null, isLoginSuccess = true))
                            }
                    } else {
                        emit(
                            oldState.copy(
                                isLoading = false,
                                error = MviError(type = ErrorType.TOAST, errorMessage = "loginByEmail"),
                            ),
                        )
                    }
                } catch (e: Exception) {
                    emit(
                        oldState.copy(
                            isLoading = false,
                            error =
                                MviError(
                                    type = ErrorType.TOAST,
                                    errorMessage = e.message ?: "loginByEmail",
                                ),
                        ),
                    )
                }
            }.onStart {
                emit(oldState.copy(isLoading = true, error = null))
            }
    }
