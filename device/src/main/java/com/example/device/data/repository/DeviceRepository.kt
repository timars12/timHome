package com.example.device.data.repository

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

interface IDeviceRepository

class DeviceRepository @Inject constructor(): IDeviceRepository {

    suspend fun getAllProjects(): List<DeviceModel> = generateItems()

    private fun generateItems(): List<DeviceModel>{
        val list = mutableListOf<DeviceModel>()
        val item1 = DeviceModel(
            id = 0,
            image = "https://cdn.lifehacker.ru/wp-content/uploads/2020/09/CHto-takoe-Arduino-i-pochemu-vam-nado-ego-kupit_1600809645.jpg",
            title = "Arduino main home module",
            totalPrice = 18.81f,
            description = "Arduino project that can measure temperature, co2 and pass this value between some other module by wi-fy",
            dateCreated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
            isFavorite = true,
            rating = 10f
        )
        val item2 = DeviceModel(
            id = 1,
            image = "https://knowhow.distrelec.com/wp-content/uploads/2021/06/iStock-1169550547-1-1920x800.jpg",
            title = "Arduino measure module",
            totalPrice = 10.21f,
            description = "Arduino project that can measure temperature, co2 and pass this value between some other module by wi-fy",
            dateCreated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
            isFavorite = false,
            rating = 5.9f
        )
        val item3 = DeviceModel(
            id = 2,
            image = "https://content1.rozetka.com.ua/goods/images/big/300749078.png",
            title = "Arduino test module",
            totalPrice = 9.81f,
            description = "Arduino project that can measure temperature, co2 and pass this value between some other module by wi-fy",
            dateCreated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"))
        )
        return list.apply {
            add(item1)
            add(item2)
            add(item3)
        }
    }
}

data class DeviceModel(
    val id: Int,
    val image: String? = null,
    val title: String,
    val description: String? = null,
    val totalPrice: Float,
    val rating: Float = 0f,
    val isFavorite: Boolean = false,
    val dateCreated: String
)