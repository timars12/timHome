package com.example.core.data.repository

import com.example.core.data.DataStoreManager
import javax.inject.Inject

class SettingRepository @Inject constructor(private val dataStore: DataStoreManager) {

    suspend fun getHomeIpAddress(): String? {
        return dataStore.getHomeIpAddress()
    }

    suspend fun setHomeIpAddress(value: String) {
        dataStore.setHomeIpAddress(value)
    }
}
