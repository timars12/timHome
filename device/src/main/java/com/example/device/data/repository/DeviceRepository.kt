package com.example.device.data.repository

import com.example.core.data.AppDatabase
import com.example.device.data.mappers.DeviceMapper
import com.example.device.domain.IDeviceRepository
import com.example.device.domain.models.DeviceModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class DeviceRepository @Inject constructor(
    private val database: AppDatabase,
    private val mapper: dagger.Lazy<DeviceMapper>
) : IDeviceRepository {

    override suspend fun getAllDevices(): Flow<List<DeviceModel>> {
        saveDevicesToDB(generateItems())
        return flow {
            database.deviceDao().getAllDevices().collect { entityList ->
                emit(entityList.map { mapper.get().convertEntityToModel(it) })
            }
        }
    }

    override suspend fun getSelectedDeviceById(deviceId: Int): DeviceModel? {
        return database.deviceDao().getSelectedDeviceById(deviceId)?.let {
            mapper.get().convertEntityToModel(it)
        }
    }

    private suspend fun saveDevicesToDB(list: List<DeviceModel>) {
        list.map { model ->
            mapper.get().convertToEntityModel(model).also { entity ->
                database.deviceDao().saveDeviceToDB(entity)
            }
        }
    }

    // todo just for test it is butter to use flow then this
    private suspend fun generateItems(): List<DeviceModel> {
        return suspendCancellableCoroutine { continuation ->
            val list = mutableListOf<DeviceModel>()
            val item1 = DeviceModel(
                id = 0,
                image = "https://cdn.lifehacker.ru/wp-content/uploads/2020/09/CHto-takoe-Arduino-i-pochemu-vam-nado-ego-kupit_1600809645.jpg",
                title = "Arduino main home module",
                totalPrice = "18.81$",
                description = "Arduino project that can measure temperature, co2 and pass this value between some other module by wi-fy",
                dateCreated = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                isFavorite = true,
                rating = 10f
            )
            val item2 = DeviceModel(
                id = 1,
                image = "https://knowhow.distrelec.com/wp-content/uploads/2021/06/iStock-1169550547-1-1920x800.jpg",
                title = "Arduino measure module",
                totalPrice = "10.21$",
                description = "Arduino project that can measure temperature, co2 and pass this value between some other module by wi-fy",
                dateCreated = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                isFavorite = false,
                rating = 5.9f
            )
            val item3 = DeviceModel(
                id = 2,
                image = "https://content1.rozetka.com.ua/goods/images/big/300749078.png",
                title = "Arduino test module",
                totalPrice = "9.81$",
                description = "Arduino project that can measure temperature, co2 and pass this value between some other module by wi-fy",
                dateCreated = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
            )
            continuation.resumeWith(
                Result.success(
                    list.apply {
                        add(item1)
                        add(item2)
                        add(item3)
                    }
                )
            )
        }
    }
}
