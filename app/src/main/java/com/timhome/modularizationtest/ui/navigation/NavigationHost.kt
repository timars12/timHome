package com.timhome.modularizationtest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.timhome.core.common.navigation.ChannelNavigator
import com.timhome.core.common.navigation.NavigationCommand
import com.timhome.core.common.navigation.Route

/**
 * Everything between the navigation state and the screen: it collects navigation commands and
 * displays the entries for [state].
 *
 * It is composed only once a state exists, so commands are never collected before the start
 * state.
 */
@Composable
internal fun NavigationHost(
    state: NavigationState,
    navigationViewModel: NavigationViewModel,
    channelNavigator: ChannelNavigator,
    entryProvider: (Route) -> NavEntry<Route>,
    onExit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnExit by rememberUpdatedState(onExit)
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    LaunchedEffect(channelNavigator, navigationViewModel, lifecycle) {
        channelNavigator.commands
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .collect { issued ->
                if (navigationViewModel.dispatch(issued.command)) currentOnExit()
            }
    }

    NavDisplay(
        entries = rememberNavigationEntries(state, entryProvider),
        modifier = modifier,
        // System back goes straight to the ViewModel, never through the navigator.
        onBack = { if (navigationViewModel.dispatch(NavigationCommand.Back)) currentOnExit() },
    )
}
