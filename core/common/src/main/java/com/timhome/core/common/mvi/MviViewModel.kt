package com.timhome.core.common.mvi
// TODO(Task 05): dead MVI base parked here; remove when features move to MVVM.

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow

interface MviViewModel<I : MviIntent, S : MviViewState> {
    val viewState: StateFlow<S>
    val singleEvent: Channel<S>
}
