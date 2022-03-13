package com.example.authdynamic.data.api.response

import com.example.core.data.db.entity.UserEntity

data class UserResponse(val id: Int, val email: String, val userName: String, val avatar: String?) {

    fun convertToUserEntity(): UserEntity {
        return UserEntity(id = 1, email = email, userName = userName, avatar = null)
    }
}