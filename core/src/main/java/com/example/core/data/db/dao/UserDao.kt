package com.example.core.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import com.example.core.data.db.entity.UserEntity

@Dao
interface UserDao {
    @Insert
    fun saveUserToDB(user: UserEntity)
}