package com.example.authdynamic.data.api.response

import com.example.core.data.db.entity.UserEntity

data class LoginResponse(val code: Int, val user: UserResponse)
data class UserResponse(
    val uuid: String,
    val name: String,
    val email: String,
    val phone_number: String?
) {

    fun convertToUserEntity(): UserEntity {
        return UserEntity(id = 1, email = name, userName = email, avatar = null)
    }
}
