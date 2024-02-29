package com.example.core.data.repository

import com.example.core.data.DataStoreManager
import javax.inject.Inject

class SettingRepository
    @Inject
    constructor(private val dataStore: DataStoreManager) {
        suspend fun getHomeIpAddress(): String? = dataStore.getHomeIpAddress()

        suspend fun setHomeIpAddress(value: String) = dataStore.setHomeIpAddress(value)

        fun checkIsUseMock(): Boolean = dataStore.isUseMockDate()

        suspend fun setUseMock(isUseMock: Boolean) = dataStore.setUseMockDate(isUseMock)
    }
