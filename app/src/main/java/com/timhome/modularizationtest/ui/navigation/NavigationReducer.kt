package com.timhome.modularizationtest.ui.navigation

import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.IssuedCommand
import com.timhome.core.common.navigation.NavigationCommand
import com.timhome.core.common.navigation.Route
import com.timhome.core.common.navigation.SignIn
import com.timhome.core.common.navigation.TopLevelRoute
import com.timhome.modularizationtest.ui.navigation.NavigationState.SignedIn
import com.timhome.modularizationtest.ui.navigation.NavigationState.SignedOut

/** The result of applying one command to a [NavigationState]. */
internal sealed interface Transition {
    data class Update(val state: NavigationState) : Transition

    /** Nothing is left to go back to: the activity finishes. */
    data object Exit : Transition

    data object Ignore : Transition
}

/**
 * The result of releasing the commands held during a predictive back gesture.
 *
 * @param state the state after every applied command; the same object when nothing changed.
 * @param exit an applied command left nothing to go back to. Later commands were not looked at.
 * @param applied how many commands were applied.
 * @param dropped how many were dropped because their origin was no longer shown.
 */
internal data class HeldResult(val state: NavigationState, val exit: Boolean, val applied: Int, val dropped: Int)

/** The navigation rules, as pure functions. */
internal object NavigationReducer {
    fun initialState(isSignedIn: Boolean): NavigationState = if (isSignedIn) FreshTabs else SignedOut(listOf(SignIn))

    /**
     * The state to start with, or `null` while it cannot be chosen yet: [isSignedIn] is `null`
     * until the stored flag has been read.
     *
     * A restored signed-in state is used as it was, without the flag. A restored Sign In history
     * with the flag set means the sign-in finished just before the process died, so it gives
     * fresh tabs.
     */
    fun startState(
        restored: NavigationState?,
        isSignedIn: Boolean?,
    ): NavigationState? =
        when {
            restored is SignedIn -> restored
            isSignedIn == null -> null
            restored is SignedOut -> if (isSignedIn) FreshTabs else restored
            else -> initialState(isSignedIn)
        }

    fun reduce(
        state: NavigationState,
        command: NavigationCommand,
    ): Transition =
        when (command) {
            is NavigationCommand.GoTo -> goTo(state, command.route)
            NavigationCommand.Back -> back(state)
            is NavigationCommand.ReplaceAll -> replaceAll(command.route)
        }

    /**
     * Releases the commands [held] during a predictive back gesture, in order. A command is
     * applied only when the screen that issued it is the one shown at that step. A command from
     * the bottom bar (no origin) is never applied.
     */
    fun applyHeld(
        state: NavigationState,
        held: List<IssuedCommand>,
    ): HeldResult {
        var current = state
        var applied = 0
        var dropped = 0
        for (issued in held) {
            // A `null` origin never equals the current route, so the bottom bar is dropped here too.
            if (issued.origin != current.currentRoute) {
                dropped++
            } else {
                applied++
                when (val transition = reduce(current, issued.command)) {
                    is Transition.Update -> current = transition.state
                    Transition.Exit -> return HeldResult(current, exit = true, applied = applied, dropped = dropped)
                    Transition.Ignore -> Unit
                }
            }
        }
        return HeldResult(current, exit = false, applied = applied, dropped = dropped)
    }

    private fun goTo(
        state: NavigationState,
        route: Route,
    ): Transition =
        if (route::class == state.currentRoute::class) {
            // A screen is never stacked on another of its kind, whatever its arguments (FR-026).
            Transition.Ignore
        } else {
            when (state) {
                // The tabs are reachable only through ReplaceAll after sign-in.
                is SignedOut -> Transition.Ignore
                is SignedIn ->
                    when {
                        // Reselecting the current tab does nothing on any of its screens (FR-007).
                        route == state.currentTab -> Transition.Ignore
                        // The tab becomes the most recent one. Its history is kept.
                        route is TopLevelRoute -> Transition.Update(state.copy(visitOrder = state.visitOrder - route + route))
                        // Sign In is reachable only through ReplaceAll.
                        route == SignIn -> Transition.Ignore
                        else -> Transition.Update(state.withCurrentStack(state.currentStack + route))
                    }
            }
        }

    private fun back(state: NavigationState): Transition =
        when (state) {
            is SignedOut -> Transition.Exit
            is SignedIn ->
                when {
                    state.currentStack.size > 1 -> Transition.Update(state.withCurrentStack(state.currentStack.dropLast(1)))
                    // On a tab's first screen: leave the tab for the previously visited one. Its
                    // stack is already `[tab]`, so nothing else is released.
                    state.visitOrder.size > 1 -> Transition.Update(state.copy(visitOrder = state.visitOrder.dropLast(1)))
                    else -> Transition.Exit
                }
        }

    private fun replaceAll(route: Route): Transition =
        when (route) {
            Home -> Transition.Update(FreshTabs)
            SignIn -> Transition.Update(SignedOut(listOf(SignIn)))
            else -> Transition.Ignore
        }

    private val SignedIn.currentStack: List<Route> get() = tabStacks.getValue(currentTab)

    private fun SignedIn.withCurrentStack(stack: List<Route>): SignedIn = copy(tabStacks = tabStacks + (currentTab to stack))
}
