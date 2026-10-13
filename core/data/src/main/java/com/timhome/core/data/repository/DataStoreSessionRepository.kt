package com.timhome.core.data.repository

import com.timhome.core.datastore.DataStoreManager
import kotlinx.coroutines.flow.Flow

internal class DataStoreSessionRepository(
    private val dataStore: DataStoreManager,
) : SessionRepository {
    override val isSignedIn: Flow<Boolean> = dataStore.isSignedIn()

    override suspend fun markSignedIn() = dataStore.setSignedIn(true)
}
