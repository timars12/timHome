package com.timhome.modularizationtest.ui.navigation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartPolicyTest {
    private fun discards(
        hasSavedState: Boolean,
        openedFromAlert: Boolean,
        launchedFromHistory: Boolean,
    ) = StartPolicy.shouldDiscardPendingNavigation(hasSavedState, openedFromAlert, launchedFromHistory)

    @Test
    fun `D1 a notification tap discards pending requests`() {
        assertTrue(discards(hasSavedState = false, openedFromAlert = true, launchedFromHistory = false))
    }

    @Test
    fun `D2 a normal launch keeps them`() {
        assertFalse(discards(hasSavedState = false, openedFromAlert = false, launchedFromHistory = false))
    }

    @Test
    fun `D3 a rotation of a notification-started activity keeps them`() {
        assertFalse(discards(hasSavedState = true, openedFromAlert = true, launchedFromHistory = false))
    }

    @Test
    fun `D4 reopening the notification's task from Recents keeps them`() {
        assertFalse(discards(hasSavedState = false, openedFromAlert = true, launchedFromHistory = true))
    }

    @Test
    fun `every other combination keeps them`() {
        assertFalse(discards(hasSavedState = true, openedFromAlert = false, launchedFromHistory = false))
        assertFalse(discards(hasSavedState = false, openedFromAlert = false, launchedFromHistory = true))
        assertFalse(discards(hasSavedState = true, openedFromAlert = false, launchedFromHistory = true))
        assertFalse(discards(hasSavedState = true, openedFromAlert = true, launchedFromHistory = true))
    }
}
