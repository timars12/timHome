package com.timhome.core.common.navigation

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Delivers navigation commands over an unlimited channel, so a command issued while nothing is
 * collecting is kept until collection starts.
 *
 * Called directly, it issues commands with no origin (the bottom bar). Screen logic gets a
 * navigator from [issuedFrom] instead, so its commands carry the screen's route.
 */
class ChannelNavigator : Navigator {
    private val channel = Channel<IssuedCommand>(Channel.UNLIMITED)
    private val held = mutableListOf<IssuedCommand>()

    /** Each command is delivered to exactly one collector, exactly once. */
    val commands: Flow<IssuedCommand> = channel.receiveAsFlow()

    override fun navigate(route: Route) = send(NavigationCommand.GoTo(route), origin = null)

    override fun back() = send(NavigationCommand.Back, origin = null)

    override fun replaceAll(route: Route) = send(NavigationCommand.ReplaceAll(route), origin = null)

    /** A navigator whose commands carry [origin]. */
    fun issuedFrom(origin: Route): Navigator = ScopedNavigator(origin)

    /** Parks a command collected during a predictive back gesture. */
    fun hold(issued: IssuedCommand) {
        synchronized(held) { held += issued }
    }

    /** Returns the parked commands in the order they were held, and empties the list. */
    fun takeHeld(): List<IssuedCommand> = synchronized(held) { held.toList().also { held.clear() } }

    /** Drops every buffered and every parked command, and returns how many were dropped. */
    fun discardPending(): Int {
        var dropped = 0
        while (channel.tryReceive().isSuccess) dropped++
        return dropped + takeHeld().size
    }

    private fun send(
        command: NavigationCommand,
        origin: Route?,
    ) {
        channel.trySend(IssuedCommand(command, origin))
    }

    private inner class ScopedNavigator(private val origin: Route) : Navigator {
        override fun navigate(route: Route) = send(NavigationCommand.GoTo(route), origin)

        override fun back() = send(NavigationCommand.Back, origin)

        override fun replaceAll(route: Route) = send(NavigationCommand.ReplaceAll(route), origin)
    }
}
