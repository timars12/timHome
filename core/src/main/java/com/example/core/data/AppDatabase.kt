package com.example.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.core.data.db.dao.CarbonDioxideDao
import com.example.core.data.db.dao.DeviceDao
import com.example.core.data.db.dao.UserDao
import com.example.core.data.db.entity.CarbonDioxideEntity
import com.example.core.data.db.entity.DeviceEntity
import com.example.core.data.db.entity.ModuleEntity
import com.example.core.data.db.entity.UserEntity
import com.example.core.utils.Constant.APP_DATABASE

@Database(
    entities = [
        UserEntity::class,
        DeviceEntity::class,
        ModuleEntity::class,
        CarbonDioxideEntity::class,
    ],
    version = 5,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun deviceDao(): DeviceDao

    abstract fun carbonDioxideDao(): CarbonDioxideDao

    companion object {
        @Volatile
        @Suppress("PropertyName")
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                APP_DATABASE,
            )
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
        }
    }
}
