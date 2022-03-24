package com.example.authdynamic.data.api

import com.example.authdynamic.data.api.request.NewUserRequest
import com.example.authdynamic.data.api.request.UserLoginRequest
import com.example.authdynamic.data.api.response.LoginResponse
import com.example.authdynamic.data.api.response.UserResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun loginByEmail(@Body body: UserLoginRequest): LoginResponse

    fun newAccount(body: NewUserRequest): UserResponse
}