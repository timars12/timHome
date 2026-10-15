package com.timhome.core.data.repository

import kotlinx.coroutines.flow.Flow

/** The only access to the remembered signed-in status. */
interface SessionRepository {
    /** Emits the stored status; `false` until the first successful sign-in on this device. */
    val isSignedIn: Flow<Boolean>

    /** Remembers that the user has signed in. Idempotent. */
    suspend fun markSignedIn()
}
