package com.example.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.seconds

val Context.getDataStore: DataStore<Preferences> by preferencesDataStore(name = "timDataStore")

@Singleton
class DataStoreManager
    @Inject
    constructor(context: Context) {
        private val dataStore = context.getDataStore

        suspend fun setTokens(
            accessToken: String,
            refreshToken: String,
        ) {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.ACCESS_TOKEN] = "Bearer $accessToken"
                preferences[PreferencesKeys.REFRESH_TOKEN] = refreshToken
            }
        }

        suspend fun getAccessToken(): String? =
            dataStore.data.first()[PreferencesKeys.ACCESS_TOKEN]

        suspend fun getRefreshToken(): String? =
            dataStore.data.first()[PreferencesKeys.REFRESH_TOKEN]

        suspend fun getHomeIpAddress(): String? =
            dataStore.data.first()[PreferencesKeys.HOME_IP_ADDRESS]

        suspend fun setHomeIpAddress(ipAddress: String) =
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.HOME_IP_ADDRESS] = ipAddress
            }

        suspend fun setUseMockDate(isUseMock: Boolean) =
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.IS_USE_MOCK] = isUseMock
            }

        fun isUseMockDate(): Boolean =
            runBlocking {
                withTimeoutOrNull(1.seconds) {
                    !getHomeIpAddress().isNullOrBlank() || dataStore.data.first()[PreferencesKeys.IS_USE_MOCK] ?: true
                } ?: true
            }

        suspend fun clearData() {
            dataStore.edit { preferences -> preferences.clear() }
        }

        private object PreferencesKeys {
            val ACCESS_TOKEN = stringPreferencesKey("accessToken")
            val REFRESH_TOKEN = stringPreferencesKey("refreshToken")
            val HOME_IP_ADDRESS = stringPreferencesKey("homeIpAddress")
            val IS_USE_MOCK = booleanPreferencesKey("isUseMockDate")
        }
    }
