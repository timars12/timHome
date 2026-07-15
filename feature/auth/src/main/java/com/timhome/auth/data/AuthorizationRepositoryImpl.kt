package com.timhome.auth.data

import com.timhome.auth.data.api.AuthApi
import com.timhome.auth.data.api.request.UserLoginRequest
import com.timhome.auth.domain.IAuthorizationRepository
import com.timhome.auth.ui.signin.LoginViewState
import com.timhome.core.data.AppDatabase
import com.timhome.core.common.Constant.CODE_200
import com.timhome.core.common.mvi.ErrorType
import com.timhome.core.common.mvi.MviError
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
