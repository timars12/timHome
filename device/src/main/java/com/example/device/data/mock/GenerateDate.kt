package com.example.device.data.mock

import com.example.core.di.scope.FeatureScope
import com.example.device.data.model.Device
import com.example.device.data.model.ModuleModel
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@FeatureScope
class GenerateDate @Inject constructor() {

    // todo just for test it is butter to use flow then this
    @Suppress("LongMethod")
    suspend fun generateItems(): List<Device> {
        return suspendCancellableCoroutine { continuation ->
            val list = mutableListOf<Device>()
            val item1 = Device(
                id = 0,
                image = "https://cdn.lifehacker.ru/wp-content/uploads/2020/09/CHto-takoe-Arduino-i-pochemu-vam-nado-ego-kupit_1600809645.jpg",
                ownerName = "Ruslan Timkov",
                title = "Arduino main home module",
                totalPrice = "18.81$",
                description = "Arduino project that can measure temperature, co2 and pass this value between some other module by wi-fy",
                dateCreated = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                isFavorite = true,
                rating = 10f,
                modules = listOf(
                    ModuleModel(
                        id = 0,
                        image = "https://ae01.alicdn.com/kf/H12e6cf8d37394657879c1b62b49aaa52Y/1Set-UNO-R3-Official-Box-ATMEGA16U2-UNO-WiFi-R3-MEGA328P-Chip-CH340G-For-Arduino-UNO-R3.jpg_640x640.jpg",
                        title = "UNO R3 ATMEGA328P",
                        price = "3.22$",
                        link = "https://www.aliexpress.com/item/1005002997846504.html?spm=a2g0o.productlist.main.83.1d2fc095KXRjER&algo_pvid=e0e5cf40-9f1a-4eba-803c-a6dd250a813b&algo_exp_id=e0e5cf40-9f1a-4eba-803c-a6dd250a813b-41&pdp_ext_f=%7B\"sku_id\"%3A\"12000023136335077\"%7D&pdp_npi=2%40dis%21USD%214.0%213.56%21%21%21%21%21%402100b20d16733079013583900d06ef%2112000023136335077%21sea&curPageLogUid=LRcHs0bQN1qI"
                    ),
                    ModuleModel(
                        id = 1,
                        image = "https://ae01.alicdn.com/kf/Hdd04126ddfcd4d94a429febe7c18b373X/0-91-inch-OLED-Module-White-Blue-OLED-128X32-OLED-LCD-LED-Display-Module-0-91.jpg_50x50.jpg_.webp",
                        title = "OLED Module 128X32",
                        price = "1.22$",
                        link = "https://www.aliexpress.com/item/32777216785.html?spm=a2g0o.productlist.main.95.1d2fc095KXRjER&algo_pvid=e0e5cf40-9f1a-4eba-803c-a6dd250a813b&algo_exp_id=e0e5cf40-9f1a-4eba-803c-a6dd250a813b-47&pdp_ext_f=%7B\"sku_id\"%3A\"10000006058755365\"%7D&pdp_npi=2%40dis%21USD%211.57%211.38%21%21%21%21%21%402100b20d16733079013583900d06ef%2110000006058755365%21sea&curPageLogUid=jtMeWrmIM81D"
                    ),
                    ModuleModel(
                        id = 2,
                        image = "",
                        title = "UNO R3 ATMEGA328P",
                        price = "3.22$",
                        link = ""
                    ),
                    ModuleModel(
                        id = 3,
                        image = "",
                        title = "UNO R3 ATMEGA328P",
                        price = "3.22$",
                        link = ""
                    ),
                    ModuleModel(
                        id = 4,
                        image = "",
                        title = "UNO R3 ATMEGA328P",
                        price = "3.22$",
                        link = ""
                    ),
                )
            )
            val item2 = Device(
                id = 1,
                image = "https://knowhow.distrelec.com/wp-content/uploads/2021/06/iStock-1169550547-1-1920x800.jpg",
                ownerName = "Ruslan Timkov",
                title = "Arduino measure module",
                totalPrice = "10.21$",
                description = "Arduino project that can measure temperature, co2 and pass this value between some other module by wi-fy",
                dateCreated = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                isFavorite = false,
                rating = 5.9f,
                modules = emptyList()
            )
            val item3 = Device(
                id = 2,
                image = "https://content1.rozetka.com.ua/goods/images/big/300749078.png",
                ownerName = "Ruslan Timkov",
                title = "Arduino test module",
                totalPrice = "9.81$",
                description = "Arduino project that can measure temperature, co2 and pass this value between some other module by wi-fy",
                dateCreated = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                modules = emptyList()
            )
            val item4 = Device(
                id = 3,
                image = "https://cdn.lifehacker.ru/wp-content/uploads/2020/09/CHto-takoe-Arduino-i-pochemu-vam-nado-ego-kupit_1600809645.jpg",
                ownerName = "Ruslan Timkov",
                title = "Arduino main home module",
                totalPrice = "18.81$",
                description = "Arduino project that can measure temperature, co2 and pass this value between some other module by wi-fy",
                dateCreated = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                isFavorite = true,
                rating = 10f,
                modules = emptyList()
            )
            val item5 = Device(
                id = 4,
                image = "https://knowhow.distrelec.com/wp-content/uploads/2021/06/iStock-1169550547-1-1920x800.jpg",
                ownerName = "Ruslan Timkov",
                title = "Arduino measure module",
                totalPrice = "10.21$",
                description = "Arduino project that can measure temperature, co2 and pass this value between some other module by wi-fy",
                dateCreated = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                isFavorite = false,
                rating = 5.9f,
                modules = emptyList()
            )
            val item6 = Device(
                id = 5,
                image = "https://content1.rozetka.com.ua/goods/images/big/300749078.png",
                ownerName = "Ruslan Timkov",
                title = "Arduino test module",
                totalPrice = "9.81$",
                description = "Arduino project that can measure temperature, co2 and pass this value between some other module by wi-fy",
                dateCreated = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                modules = emptyList()
            )
            continuation.resumeWith(
                Result.success(
                    list.apply {
                        add(item1)
                        add(item2)
                        add(item3)
                        add(item4)
                        add(item5)
                        add(item6)
                    }
                )
            )
        }
    }
}
