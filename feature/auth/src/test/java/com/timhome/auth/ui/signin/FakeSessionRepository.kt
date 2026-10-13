package com.timhome.auth.ui.signin

import com.timhome.core.data.repository.SessionRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeSessionRepository(initial: Boolean = false) : SessionRepository {
    private val signedIn = MutableStateFlow(initial)
    override val isSignedIn: Flow<Boolean> = signedIn

    var markSignedInCalls = 0
        private set

    /** When set, [markSignedIn] throws it instead of storing the status. */
    var failure: Exception? = null

    /** When set, [markSignedIn] suspends until it completes. */
    var gate: CompletableDeferred<Unit>? = null

    override suspend fun markSignedIn() {
        markSignedInCalls++
        gate?.await()
        failure?.let { throw it }
        signedIn.value = true
    }
}
