package com.example.authdynamic.data

import com.example.authdynamic.data.api.AuthApi
import com.example.authdynamic.data.api.request.UserLoginRequest
import com.example.authdynamic.data.mapper.User
import com.example.authdynamic.domain.IAuthorizationRepository
import com.example.core.data.AppDatabase
import javax.inject.Inject


class AuthorizationRepositoryImpl @Inject constructor(
    private val apiService: AuthApi,
    private val database: AppDatabase
) : IAuthorizationRepository {

     override suspend fun loginByEmail(email: String, password: String): User {
         val body = UserLoginRequest(email, password)
        apiService.loginByEmail(body).user.convertToUserEntity()
            .also {
                database.userDao().saveUserToDB(it)
            }.also {
                return User(it.id, it.email, it.userName, it.avatar)
            }
    }
}