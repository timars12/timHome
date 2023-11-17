package com.example.authdynamic.domain

import com.example.authdynamic.data.mapper.User
import com.example.core.utils.CallStatus

internal interface IAuthorizationRepository {
    suspend fun loginByEmail(email: String, password: String): CallStatus<User>
}
