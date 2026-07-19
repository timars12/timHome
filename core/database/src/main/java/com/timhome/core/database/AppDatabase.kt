package com.timhome.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.timhome.core.database.dao.CarbonDioxideDao
import com.timhome.core.database.dao.DeviceDao
import com.timhome.core.database.dao.PotDao
import com.timhome.core.database.dao.RoomClimateReadingDao
import com.timhome.core.database.dao.RoomDao
import com.timhome.core.database.dao.SoilMoistureReadingDao
import com.timhome.core.database.dao.UserDao
import com.timhome.core.database.dao.WateringEventDao
import com.timhome.core.database.entity.CarbonDioxideEntity
import com.timhome.core.database.entity.DeviceEntity
import com.timhome.core.database.entity.ModuleEntity
import com.timhome.core.database.entity.PotEntity
import com.timhome.core.database.entity.RoomClimateReadingEntity
import com.timhome.core.database.entity.RoomEntity
import com.timhome.core.database.entity.SoilMoistureReadingEntity
import com.timhome.core.database.entity.UserEntity
import com.timhome.core.database.entity.WateringEventEntity
import com.timhome.core.common.Constant.APP_DATABASE

@Database(
    entities = [
        UserEntity::class,
        DeviceEntity::class,
        ModuleEntity::class,
        CarbonDioxideEntity::class,
        RoomEntity::class,
        PotEntity::class,
        SoilMoistureReadingEntity::class,
        RoomClimateReadingEntity::class,
        WateringEventEntity::class,
    ],
    version = 7,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    abstract fun deviceDao(): DeviceDao

    abstract fun carbonDioxideDao(): CarbonDioxideDao

    abstract fun roomDao(): RoomDao

    abstract fun potDao(): PotDao

    abstract fun soilMoistureReadingDao(): SoilMoistureReadingDao

    abstract fun roomClimateReadingDao(): RoomClimateReadingDao

    abstract fun wateringEventDao(): WateringEventDao

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
