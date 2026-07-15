package com.timhome.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.timhome.core.database.dao.CarbonDioxideDao
import com.timhome.core.database.dao.DeviceDao
import com.timhome.core.database.dao.UserDao
import com.timhome.core.database.entity.CarbonDioxideEntity
import com.timhome.core.database.entity.DeviceEntity
import com.timhome.core.database.entity.ModuleEntity
import com.timhome.core.database.entity.UserEntity
import com.timhome.core.common.Constant.APP_DATABASE

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
