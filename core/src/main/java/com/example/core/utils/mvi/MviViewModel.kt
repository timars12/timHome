package com.example.core.utils.mvi

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow

interface MviViewModel<I : MviIntent, S : MviViewState> {
    val viewState: StateFlow<S>
    val singleEvent: Channel<S>
}
