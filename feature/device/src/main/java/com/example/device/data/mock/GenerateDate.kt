package com.example.device.data.mock

import com.example.core.di.scope.FeatureScope
import com.example.device.data.model.Device
import com.example.device.data.model.ModuleModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@FeatureScope
class GenerateDate @Inject constructor() {

    @Suppress("LongMethod")
    fun generateItems(): Flow<List<Device>> {
        val list = mutableListOf<Device>()
        return flow {
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
                        image = "https://content1.rozetka.com.ua/goods/images/big/167444178.jpg",
                        title = "OLED Module 128X32",
                        price = "1.22$",
                        link = "https://www.aliexpress.com/item/32777216785.html?spm=a2g0o.productlist.main.95.1d2fc095KXRjER&algo_pvid=e0e5cf40-9f1a-4eba-803c-a6dd250a813b&algo_exp_id=e0e5cf40-9f1a-4eba-803c-a6dd250a813b-47&pdp_ext_f=%7B\"sku_id\"%3A\"10000006058755365\"%7D&pdp_npi=2%40dis%21USD%211.57%211.38%21%21%21%21%21%402100b20d16733079013583900d06ef%2110000006058755365%21sea&curPageLogUid=jtMeWrmIM81D"
                    ),
                    ModuleModel(
                        id = 2,
                        image = "https://ae01.alicdn.com/kf/Hb7a8f3cbd3634a46b40dd75dfed6b8cc5.jpg",
                        title = "Tactile Push Button Switch for Arduino",
                        price = "0.82$",
                        link = "https://www.aliexpress.com/item/4000271223861.html?spm=a2g0o.productlist.main.1.55739265HUCNIm&algo_pvid=9e4864bc-eb7b-447a-9f76-7ab4f4571de1&algo_exp_id=9e4864bc-eb7b-447a-9f76-7ab4f4571de1-0&pdp_ext_f=%7B\"sku_id\"%3A\"10000001104188457\"%7D&pdp_npi=3%40dis%21USD%210.96%210.82%21%21%21%21%21%4021135cc216773400262364043d06f1%2110000001104188457%21sea%21UA%21722964942&curPageLogUid=7bbCb5kfyMZN"
                    ),
                    ModuleModel(
                        id = 3,
                        image = "https://ae01.alicdn.com/kf/H469ac42f4eb64cca947f364c18f34175o.jpg",
                        title = "NRF24L01+ 2.4G wireless data transmission module ",
                        price = "0.68$",
                        link = "https://www.aliexpress.com/item/1005001621991136.html?spm=a2g0o.productlist.main.29.20534350c84LZq&algo_pvid=e69dd9c6-a717-40c5-b2d6-406c314dcc09&aem_p4p_detail=202302250750227415309557171200001471639&algo_exp_id=e69dd9c6-a717-40c5-b2d6-406c314dcc09-14&pdp_ext_f=%7B\"sku_id\"%3A\"12000024096048689\"%7D&pdp_npi=3%40dis%21USD%210.81%210.68%21%21%21%21%21%4021209f9e16773402224721952d06d8%2112000024096048689%21sea%21UA%21722964942&curPageLogUid=LpAqhVKRqml1&ad_pvid=202302250750227415309557171200001471639_3&ad_pvid=202302250750227415309557171200001471639_3"
                    ),
                    ModuleModel(
                        id = 4,
                        image = "https://ae01.alicdn.com/kf/Se0229ea8ea7046f4a52f01f34f82249bX.jpg",
                        title = "PL2303HX+1PCS CP2102",
                        price = "0.63$",
                        link = "https://www.aliexpress.com/item/1005005104583795.html?spm=a2g0o.productlist.main.31.20534350c84LZq&algo_pvid=e69dd9c6-a717-40c5-b2d6-406c314dcc09&algo_exp_id=e69dd9c6-a717-40c5-b2d6-406c314dcc09-15&pdp_ext_f=%7B\"sku_id\"%3A\"12000031674762311\"%7D&pdp_npi=3%40dis%21USD%210.97%210.63%21%21%21%21%21%4021209f9e16773402224721952d06d8%2112000031674762311%21sea%21UA%21722964942&curPageLogUid=fGgOQczaERB9"
                    )
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
            list.apply {
                add(item1)
                add(item2)
                add(item3)
                add(item4)
                add(item5)
                add(item6)
            }
            emit(list)
        }
    }
}
