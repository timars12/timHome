package com.example.core.utils.mvi

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.onEach

abstract class Reducer<S : MviViewState, E : MviIntent>(initialVal: S) {

    private val _state: MutableStateFlow<S> = MutableStateFlow(initialVal)
    val state: StateFlow<S> get() = _state
    val singleEvent: Channel<S> = Channel(Channel.UNLIMITED)

    suspend fun sendEvent(event: E) = reduce(_state.value, event)

    suspend fun setState(newState: Flow<S>) {
        _state.emitAll(
            newState.onEach {
                if (it.error != null) setSingleEvent(newState.last())
            }
        )
    }

    suspend fun setState(newState: S) = _state.emit(newState)

    private fun setSingleEvent(event: S) {
        singleEvent.trySend(event)
    }

    abstract suspend fun reduce(oldState: S, event: E)
}
