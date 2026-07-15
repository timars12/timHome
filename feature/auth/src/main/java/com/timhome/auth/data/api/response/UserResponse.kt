package com.timhome.auth.data.api.response

import com.timhome.core.database.entity.UserEntity
import com.squareup.moshi.Json

internal data class LoginResponse(val code: Int, val user: UserResponse)

internal data class UserResponse(
    val uuid: String,
    val name: String,
    val email: String,
    @field:Json(name = "phone_number") val phoneNumber: String?,
) {
    fun convertToUserEntity(): UserEntity {
        return UserEntity(id = 1, email = name, userName = email, avatar = null)
    }
}
