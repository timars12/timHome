package com.timhome.core.common.navigation

/**
 * The only way screen logic causes navigation. It has no navigation library types, so it can be
 * checked in plain unit tests.
 */
interface Navigator {
    /**
     * Push [route] on the current tab, or switch tab when [route] is a [TopLevelRoute]. There is
     * no separate "switch tab" call.
     */
    fun navigate(route: Route)

    /** Same as system back: one screen back, or to the previous tab, or leave the app. */
    fun back()

    /**
     * Drop all history and start fresh at [route] (used after sign-in: `replaceAll(Home)`).
     * Only [Home] and [SignIn] are supported; any other route is ignored.
     */
    fun replaceAll(route: Route)
}

sealed interface NavigationCommand {
    data class GoTo(val route: Route) : NavigationCommand

    data object Back : NavigationCommand

    data class ReplaceAll(val route: Route) : NavigationCommand
}

/**
 * A [command] plus who issued it. [origin] is the route of the screen whose ViewModel issued it,
 * or `null` for the bottom bar.
 */
data class IssuedCommand(val command: NavigationCommand, val origin: Route?)
