package com.example.authdynamic.data

import com.example.authdynamic.data.api.AuthApi
import com.example.authdynamic.data.api.request.UserLoginRequest
import com.example.authdynamic.data.mapper.User
import com.example.authdynamic.domain.IAuthorizationRepository
import com.example.core.data.AppDatabase
import com.example.core.utils.CallStatus
import javax.inject.Inject


class AuthorizationRepositoryImpl @Inject constructor(
    private val apiService: AuthApi,
    private val database: AppDatabase
) : IAuthorizationRepository {

    override suspend fun loginByEmail(email: String, password: String): CallStatus<User> {
        val body = UserLoginRequest(email, password)
        try {
            val response = apiService.loginByEmail(body)
            if (response.isSuccessful) {
                response.body()?.user?.convertToUserEntity()
                    .also {
                        database.userDao().saveUserToDB(it!!)
                    }.also {
                        return CallStatus.Success(User(it!!.id, it.email, it.userName, it.avatar))
                    }
            } else {
                return CallStatus.Error()
            }
        } catch (e: Exception) {
            return CallStatus.Error()
        }
    }
}