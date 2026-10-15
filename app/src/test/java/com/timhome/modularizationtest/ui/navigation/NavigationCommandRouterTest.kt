package com.timhome.modularizationtest.ui.navigation

import androidx.lifecycle.SavedStateHandle
import com.timhome.core.common.navigation.BuyModule
import com.timhome.core.common.navigation.ChannelNavigator
import com.timhome.core.common.navigation.Devices
import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.IssuedCommand
import com.timhome.core.common.navigation.NavigationCommand.Back
import com.timhome.core.common.navigation.NavigationCommand.GoTo
import com.timhome.core.common.navigation.Setting
import com.timhome.modularizationtest.ui.navigation.NavigationReducerTest.Companion.onBuyModule
import com.timhome.modularizationtest.ui.navigation.NavigationReducerTest.Companion.onDeviceDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Cases W1–W8 of `contracts/navigation-rules.md`, with a real navigator and a real ViewModel. */
class NavigationCommandRouterTest {
    private val navigator = ChannelNavigator()
    private val viewModel = NavigationViewModel(SavedStateHandle())
    private val router = NavigationCommandRouter(navigator, viewModel)

    @Test
    fun `W1 with no gesture a command is applied`() {
        viewModel.start(FreshTabs)

        assertFalse(router.onCollected(IssuedCommand(GoTo(Devices), origin = null), gestureInProgress = false))

        assertEquals(FreshTabs.copy(visitOrder = listOf(Home, Devices)), viewModel.state.value)
        assertTrue(navigator.takeHeld().isEmpty())
    }

    @Test
    fun `W2 during a gesture a screen's command is parked`() {
        viewModel.start(onBuyModule)
        val backFromBuyModule = IssuedCommand(Back, BuyModule(1))

        assertFalse(router.onCollected(backFromBuyModule, gestureInProgress = true))

        assertSame(onBuyModule, viewModel.state.value)
        assertEquals(listOf(backFromBuyModule), navigator.takeHeld())
    }

    @Test
    fun `W3 with no gesture an exit is passed on`() {
        viewModel.start(FreshTabs)

        assertTrue(router.onCollected(IssuedCommand(Back, Home), gestureInProgress = false))
    }

    @Test
    fun `W4 releasing drops a stale back and empties the parked list`() {
        viewModel.start(onDeviceDetail)
        navigator.hold(IssuedCommand(Back, BuyModule(1)))

        val result = router.releaseHeld()

        assertEquals(HeldResult(onDeviceDetail, exit = false, applied = 0, dropped = 1), result)
        assertSame(onDeviceDetail, viewModel.state.value)
        assertTrue(navigator.takeHeld().isEmpty())
    }

    @Test
    fun `W5 releasing with nothing parked does nothing`() {
        viewModel.start(FreshTabs)

        assertNull(router.releaseHeld())

        assertSame(FreshTabs, viewModel.state.value)
    }

    @Test
    fun `W6 releasing passes an exit on`() {
        viewModel.start(FreshTabs)
        navigator.hold(IssuedCommand(Back, Home))

        assertEquals(true, router.releaseHeld()?.exit)
    }

    @Test
    fun `W7 releasing before the start state exists keeps the parked commands`() {
        val parked = IssuedCommand(Back, Home)
        navigator.hold(parked)

        assertNull(router.releaseHeld())

        assertNull(viewModel.state.value)
        assertEquals(listOf(parked), navigator.takeHeld())
    }

    @Test
    fun `W8 during a gesture a bottom-bar tap is dropped for good`() {
        viewModel.start(onDeviceDetail)

        assertFalse(router.onCollected(IssuedCommand(GoTo(Setting), origin = null), gestureInProgress = true))

        assertSame(onDeviceDetail, viewModel.state.value)
        assertTrue(navigator.takeHeld().isEmpty())
        assertNull(router.releaseHeld())
        assertSame(onDeviceDetail, viewModel.state.value)
    }
}
