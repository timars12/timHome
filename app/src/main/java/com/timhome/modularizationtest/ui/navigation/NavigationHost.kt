package com.timhome.modularizationtest.ui.navigation

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.NavigationEventTransitionState.InProgress
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import com.timhome.core.common.navigation.ChannelNavigator
import com.timhome.core.common.navigation.NavigationCommand
import com.timhome.core.common.navigation.Route
import com.timhome.core.ui.toContentTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

private const val TAG = "NavigationHost"

/**
 * Everything between the navigation state and the screen: it collects navigation commands and
 * displays the entries for [state].
 *
 * While a predictive back gesture is in progress, collected commands are not applied. They are
 * released one frame after the gesture ends. [NavigationCommandRouter] decides what happens to
 * each one; only the timing is here.
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
    val navigationEventDispatcher =
        checkNotNull(LocalNavigationEventDispatcherOwner.current) {
            "NavigationHost needs a NavigationEventDispatcherOwner"
        }.navigationEventDispatcher
    val router =
        remember(channelNavigator, navigationViewModel) {
            NavigationCommandRouter(channelNavigator, navigationViewModel)
        }

    LaunchedEffect(router, navigationEventDispatcher, lifecycle) {
        // Commands held before the activity was recreated come first. A recreation cancels the
        // gesture, so nothing else releases them.
        if (router.releaseHeldAndLog()) currentOnExit()
        channelNavigator.commands
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .collect { issued ->
                val gestureInProgress = navigationEventDispatcher.transitionState.value is InProgress
                if (router.onCollected(issued, gestureInProgress)) currentOnExit()
            }
    }

    LaunchedEffect(router, navigationEventDispatcher) {
        navigationEventDispatcher.transitionState
            .map { it is InProgress }
            .distinctUntilChanged()
            .filter { gestureInProgress -> !gestureInProgress }
            .collect {
                // One frame later, the back of a completed gesture has already been applied.
                withFrameNanos { }
                if (router.releaseHeldAndLog()) currentOnExit()
            }
    }

    NavDisplay(
        entries = rememberNavigationEntries(state, entryProvider),
        modifier = modifier,
        // System back goes straight to the ViewModel, never through the navigator.
        onBack = { if (navigationViewModel.dispatch(NavigationCommand.Back)) currentOnExit() },
        // Always the style of the screen being shown, whichever way the user got to it.
        transitionSpec = { transitionStyleFor(targetState.metadata).toContentTransform() },
        popTransitionSpec = { transitionStyleFor(targetState.metadata).toContentTransform() },
        predictivePopTransitionSpec = { transitionStyleFor(targetState.metadata).toContentTransform() },
    )
}

/** Releases the held commands and returns `true` when the activity should finish. */
private fun NavigationCommandRouter.releaseHeldAndLog(): Boolean {
    val result = releaseHeld() ?: return false
    if (result.applied != 0 || result.dropped != 0) {
        Log.d(TAG, "released held navigation: applied ${result.applied}, dropped ${result.dropped}")
    }
    return result.exit
}
