package com.example.core.utils

import androidx.navigation.NavController
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.onSuccess

typealias NavigationCommand = (NavController) -> Unit

class NavigationDispatcher {
    val navigationEmitter = Channel<NavigationCommand>(Channel.UNLIMITED)

    fun emit(navigationCommand: NavigationCommand) {
        navigationEmitter.trySend(navigationCommand).onSuccess {
            println("1111111111111111111111") // TODO
        }
    }
}
