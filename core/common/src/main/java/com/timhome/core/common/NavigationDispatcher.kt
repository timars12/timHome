package com.timhome.core.common

import androidx.navigation.NavController
import kotlinx.coroutines.channels.Channel

typealias NavigationCommand = (NavController) -> Unit

class NavigationDispatcher {
    val navigationEmitter = Channel<NavigationCommand>(Channel.UNLIMITED)

    fun emit(navigationCommand: NavigationCommand) = navigationEmitter.trySend(navigationCommand)
}
