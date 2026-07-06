package com.timhome.auth.data.api

import com.timhome.auth.data.api.request.NewUserRequest
import com.timhome.auth.data.api.request.UserLoginRequest
import com.timhome.auth.data.api.response.LoginResponse
import com.timhome.auth.data.api.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

internal interface AuthApi {
    @POST("auth/login")
    suspend fun loginByEmail(
        @Body body: UserLoginRequest,
    ): Response<LoginResponse>

    fun newAccount(body: NewUserRequest): UserResponse
}
