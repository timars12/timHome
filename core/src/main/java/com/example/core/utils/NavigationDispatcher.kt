package com.example.core.utils

import androidx.navigation.NavController
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

typealias NavigationCommand = (NavController) -> Unit

class NavigationDispatcher @Inject constructor() {
    private val _emitter = Channel<NavigationCommand>(Channel.UNLIMITED)
    val emitter = _emitter.receiveAsFlow()

    fun emit(navigationEvent: NavigationCommand) = _emitter.trySend(navigationEvent)
}
