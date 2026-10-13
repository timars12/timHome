package com.timhome.modularizationtest.ui.navigation

/** Decides what a launch does with navigation requests issued before it. */
internal object StartPolicy {
    /**
     * Only a tap on the CO2 notification discards them. The notification's intent is handed back
     * on a rotation and when the task is reopened from Recents, so those two are excluded.
     */
    fun shouldDiscardPendingNavigation(
        hasSavedState: Boolean,
        openedFromAlert: Boolean,
        launchedFromHistory: Boolean,
    ): Boolean = openedFromAlert && !hasSavedState && !launchedFromHistory
}
