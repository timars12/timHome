package com.timhome.modularizationtest.ui.navigation

import com.timhome.core.common.navigation.Home
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

/** The navigation rules, as pure functions. */
internal object NavigationReducer {
    fun initialState(isSignedIn: Boolean): NavigationState = if (isSignedIn) FreshTabs else SignedOut(listOf(SignIn))

    /**
     * The state to start with, or `null` while it cannot be chosen yet: [isSignedIn] is `null`
     * until the stored flag has been read.
     */
    fun startState(
        restored: NavigationState?,
        isSignedIn: Boolean?,
    ): NavigationState? =
        when {
            restored is SignedIn -> restored
            isSignedIn == null -> null
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

    private fun goTo(
        state: NavigationState,
        route: Route,
    ): Transition =
        when (state) {
            is SignedOut -> if (route is TopLevelRoute) Transition.Ignore else Transition.Update(SignedOut(state.stack + route))
            is SignedIn ->
                if (route is TopLevelRoute) {
                    Transition.Update(state.copy(visitOrder = state.visitOrder - route + route))
                } else {
                    Transition.Update(state.withCurrentStack(state.currentStack + route))
                }
        }

    private fun back(state: NavigationState): Transition =
        when (state) {
            is SignedOut -> if (state.stack.size > 1) Transition.Update(SignedOut(state.stack.dropLast(1))) else Transition.Exit
            is SignedIn ->
                if (state.currentStack.size > 1) {
                    Transition.Update(state.withCurrentStack(state.currentStack.dropLast(1)))
                } else {
                    Transition.Exit
                }
        }

    private fun replaceAll(route: Route): Transition = if (route == Home) Transition.Update(FreshTabs) else Transition.Ignore

    private val SignedIn.currentStack: List<Route> get() = tabStacks.getValue(currentTab)

    private fun SignedIn.withCurrentStack(stack: List<Route>): SignedIn = copy(tabStacks = tabStacks + (currentTab to stack))
}
