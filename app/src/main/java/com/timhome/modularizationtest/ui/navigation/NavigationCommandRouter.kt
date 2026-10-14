package com.timhome.modularizationtest.ui.navigation

import com.timhome.core.common.navigation.ChannelNavigator
import com.timhome.core.common.navigation.IssuedCommand

/**
 * Decides what happens to a collected navigation command: apply it, hold it until the predictive
 * back gesture ends, or drop it. It holds no state of its own.
 */
internal class NavigationCommandRouter(
    private val navigator: ChannelNavigator,
    private val viewModel: NavigationViewModel,
) {
    /**
     * During a back gesture, drops a bottom-bar command and parks any other; otherwise applies
     * it. Returns `true` when the activity should finish.
     */
    fun onCollected(
        issued: IssuedCommand,
        gestureInProgress: Boolean,
    ): Boolean {
        if (!gestureInProgress) return viewModel.dispatch(issued.command)
        // A bottom-bar tap (no origin) during the gesture is ignored for good.
        if (issued.origin != null) navigator.hold(issued)
        return false
    }

    /**
     * Applies the parked commands. Returns `null` when nothing was released. While there is no
     * state yet, the parked commands are left in place for a later call.
     */
    fun releaseHeld(): HeldResult? {
        if (viewModel.state.value == null) return null
        return viewModel.applyHeld(navigator.takeHeld())
    }
}
