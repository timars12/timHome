package com.example.authdynamic.data.api

import com.example.authdynamic.data.api.request.NewUserRequest
import com.example.authdynamic.data.api.response.UserResponse

interface AuthApi {
    fun loginByEmail(email: String, password: String): UserResponse

    fun newAccount(body: NewUserRequest): UserResponse
}