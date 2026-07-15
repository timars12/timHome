package com.timhome.core.data.repository

import com.timhome.core.database.AppDatabase
import com.timhome.core.datastore.DataStoreManager
import com.timhome.core.network.api.ArduinoApi
import com.timhome.core.network.api.response.ArduinoResponse
import com.timhome.core.database.entity.CarbonDioxideEntity
import com.timhome.core.common.CallStatus
import com.timhome.core.common.Constant.CODE_200
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ArduinoRepositoryImpl
    @Inject
    constructor(
        private val dataStore: DataStoreManager,
        private val database: AppDatabase,
        private val arduinoApi: ArduinoApi,
    ) : ArduinoRepository {
        override suspend fun getCo2AndTemperature(): CallStatus<ArduinoResponse> {
            return try {
                val response =
                    arduinoApi.getCo2AndTemperature(
                        "http://${dataStore.getHomeIpAddress()}/",
                    )
                when {
                    response.code() == CODE_200 -> CallStatus.Success(response.body())
                    else -> CallStatus.Error()
                }
            } catch (exception: Exception) {
                CallStatus.Error()
            }
        }

        override fun getCO2ValuesFromDB(): Flow<List<CarbonDioxideEntity>> {
            return database.carbonDioxideDao().getAllCO2Levels()
        }
    }
