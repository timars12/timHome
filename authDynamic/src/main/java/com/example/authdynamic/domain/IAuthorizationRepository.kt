package com.example.authdynamic.domain

import com.example.authdynamic.data.mapper.User

interface IAuthorizationRepository {
    fun loginByEmail(email: String, password: String): User
}