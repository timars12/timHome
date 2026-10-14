package com.timhome.modularizationtest.ui.navigation

import com.timhome.core.common.navigation.BuyModule
import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.common.navigation.Devices
import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.IssuedCommand
import com.timhome.core.common.navigation.NavigationCommand.Back
import com.timhome.core.common.navigation.NavigationCommand.GoTo
import com.timhome.core.common.navigation.NavigationCommand.ReplaceAll
import com.timhome.core.common.navigation.Route
import com.timhome.core.common.navigation.Setting
import com.timhome.core.common.navigation.SignIn
import com.timhome.core.common.navigation.TopLevelRoute
import com.timhome.modularizationtest.ui.navigation.NavigationState.SignedIn
import com.timhome.modularizationtest.ui.navigation.NavigationState.SignedOut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** One case per row of `contracts/navigation-rules.md`, named by its rule number. */
class NavigationReducerTest {
    // region start state

    @Test
    fun `R0a not signed in starts on Sign In`() {
        assertEquals(signedOut, NavigationReducer.initialState(isSignedIn = false))
        assertEquals(signedOut, NavigationReducer.startState(restored = null, isSignedIn = false))
    }

    @Test
    fun `R0b signed in starts on Home with fresh tabs`() {
        assertEquals(FreshTabs, NavigationReducer.initialState(isSignedIn = true))
        assertEquals(FreshTabs, NavigationReducer.startState(restored = null, isSignedIn = true))
        assertEquals(signedIn(), FreshTabs)
    }

    @Test
    fun `R0c a restored signed-in state is used as it was, whatever the flag`() {
        val restored = signedIn(devices = listOf(Devices, DeviceDetail(1)), visitOrder = listOf(Home, Devices))

        listOf(null, false, true).forEach { flag ->
            assertSame("flag=$flag", restored, NavigationReducer.startState(restored, flag))
        }
    }

    @Test
    fun `R0d restored Sign In with the flag set opens Home with fresh tabs`() {
        assertEquals(FreshTabs, NavigationReducer.startState(signedOut, isSignedIn = true))
    }

    @Test
    fun `R0e restored Sign In without the flag is used as it was`() {
        assertSame(signedOut, NavigationReducer.startState(signedOut, isSignedIn = false))
    }

    @Test
    fun `R0f there is no start state until the flag is read`() {
        assertNull(NavigationReducer.startState(restored = null, isSignedIn = null))
        assertNull(NavigationReducer.startState(signedOut, isSignedIn = null))
    }

    // endregion

    // region signed out

    @Test
    fun `R1 back on Sign In leaves the app`() {
        assertEquals(Transition.Exit, NavigationReducer.reduce(signedOut, Back))
    }

    @Test
    fun `R2 replacing everything with Home after sign-in gives fresh tabs`() {
        assertEquals(Transition.Update(FreshTabs), NavigationReducer.reduce(signedOut, ReplaceAll(Home)))
    }

    @Test
    fun `R3 going to Sign In from Sign In is ignored`() {
        assertEquals(Transition.Ignore, NavigationReducer.reduce(signedOut, GoTo(SignIn)))
    }

    @Test
    fun `R4 going anywhere else while signed out is ignored`() {
        listOf(Home, Devices, Setting, DeviceDetail(1), BuyModule(1)).forEach { route ->
            assertEquals("GoTo($route)", Transition.Ignore, NavigationReducer.reduce(signedOut, GoTo(route)))
        }
    }

    // endregion

    // region signed in

    @Test
    fun `R5 going to Device Detail pushes it onto the Devices tab`() {
        val state = signedIn(visitOrder = listOf(Home, Devices))

        assertEquals(
            Transition.Update(state.copy(tabStacks = state.tabStacks + (Devices to listOf(Devices, DeviceDetail(1))))),
            NavigationReducer.reduce(state, GoTo(DeviceDetail(1))),
        )
    }

    @Test
    fun `R6 going to the screen that is already on top is ignored`() {
        assertEquals(Transition.Ignore, NavigationReducer.reduce(onDeviceDetail, GoTo(DeviceDetail(1))))
    }

    @Test
    fun `R6a going to Device Detail of another device from Device Detail is ignored`() {
        assertEquals(Transition.Ignore, NavigationReducer.reduce(onDeviceDetail, GoTo(DeviceDetail(2))))
    }

    @Test
    fun `R6b going to Buy Module of another device from Buy Module is ignored`() {
        assertEquals(Transition.Ignore, NavigationReducer.reduce(onBuyModule, GoTo(BuyModule(2))))
        assertEquals(Transition.Ignore, NavigationReducer.reduce(onBuyModule, GoTo(BuyModule(1))))
    }

    @Test
    fun `R6a quick taps on two device rows open only the first device`() {
        val onDevices = signedIn(visitOrder = listOf(Home, Devices))

        val afterFirstTap = (NavigationReducer.reduce(onDevices, GoTo(DeviceDetail(1))) as Transition.Update).state

        assertEquals(onDeviceDetail, afterFirstTap)
        assertEquals(Transition.Ignore, NavigationReducer.reduce(afterFirstTap, GoTo(DeviceDetail(2))))
    }

    @Test
    fun `R7 going to Buy Module from Device Detail pushes it`() {
        assertEquals(Transition.Update(onBuyModule), NavigationReducer.reduce(onDeviceDetail, GoTo(BuyModule(1))))
    }

    @Test
    fun `R12 back from Buy Module returns to Device Detail`() {
        assertEquals(Transition.Update(onDeviceDetail), NavigationReducer.reduce(onBuyModule, Back))
    }

    @Test
    fun `R13 back from Device Detail returns to the Devices list`() {
        assertEquals(
            Transition.Update(signedIn(visitOrder = listOf(Home, Devices))),
            NavigationReducer.reduce(onDeviceDetail, Back),
        )
    }

    @Test
    fun `R8 going to the current tab is ignored on any of its screens`() {
        listOf(signedIn(visitOrder = listOf(Home, Devices)), onDeviceDetail, onBuyModule).forEach { state ->
            assertEquals("from ${state.currentRoute}", Transition.Ignore, NavigationReducer.reduce(state, GoTo(Devices)))
        }
        assertEquals(Transition.Ignore, NavigationReducer.reduce(FreshTabs, GoTo(Home)))
    }

    @Test
    fun `R9 going to another tab adds it to the visit history and keeps the stacks`() {
        assertEquals(
            Transition.Update(signedIn(visitOrder = listOf(Home, Devices))),
            NavigationReducer.reduce(FreshTabs, GoTo(Devices)),
        )
    }

    @Test
    fun `R10 going to a visited tab moves it to most recent without duplicating it`() {
        val state = signedIn(visitOrder = listOf(Home, Devices, Setting))

        assertEquals(
            Transition.Update(signedIn(visitOrder = listOf(Devices, Setting, Home))),
            NavigationReducer.reduce(state, GoTo(Home)),
        )
    }

    @Test
    fun `R11 a tab keeps its history while another tab is shown`() {
        val onSetting = (NavigationReducer.reduce(onDeviceDetail, GoTo(Setting)) as Transition.Update).state
        val backOnDevices = (NavigationReducer.reduce(onSetting, GoTo(Devices)) as Transition.Update).state as SignedIn

        assertEquals(listOf(Devices, DeviceDetail(1)), backOnDevices.tabStacks[Devices])
        assertEquals(DeviceDetail(1), backOnDevices.currentRoute)
        assertEquals(listOf(Home, Setting, Devices), backOnDevices.visitOrder)
    }

    @Test
    fun `R14 back on a tab's first screen returns to the previously visited tab`() {
        val state = signedIn(visitOrder = listOf(Home, Devices, Setting))

        assertEquals(
            Transition.Update(signedIn(visitOrder = listOf(Home, Devices))),
            NavigationReducer.reduce(state, Back),
        )
    }

    @Test
    fun `R15 backing out of a tab releases its history`() {
        val state = signedIn(devices = listOf(Devices, DeviceDetail(1)), visitOrder = listOf(Home, Setting, Devices))

        val afterFirstBack = (NavigationReducer.reduce(state, Back) as Transition.Update).state

        assertEquals(signedIn(visitOrder = listOf(Home, Setting, Devices)), afterFirstBack)
        assertEquals(
            Transition.Update(signedIn(visitOrder = listOf(Home, Setting))),
            NavigationReducer.reduce(afterFirstBack, Back),
        )
    }

    @Test
    fun `R17 back on Home's first screen returns to the previously visited tab`() {
        val state = signedIn(visitOrder = listOf(Devices, Home))

        assertEquals(Transition.Update(signedIn(visitOrder = listOf(Devices))), NavigationReducer.reduce(state, Back))
    }

    @Test
    fun `R18 back on the first screen of the only tab left leaves the app`() {
        assertEquals(Transition.Exit, NavigationReducer.reduce(signedIn(visitOrder = listOf(Setting)), Back))
    }

    @Test
    fun `R21 going to Sign In while signed in is ignored`() {
        listOf(FreshTabs, onDeviceDetail, signedIn(visitOrder = listOf(Home, Setting))).forEach { state ->
            assertEquals("from ${state.currentRoute}", Transition.Ignore, NavigationReducer.reduce(state, GoTo(SignIn)))
        }
    }

    @Test
    fun `the invariants hold after every update of random command sequences`() {
        val random = Random(SEED)
        val routes =
            listOf(SignIn, Home, Devices, Setting, DeviceDetail(1), DeviceDetail(2), BuyModule(1), BuyModule(2))

        repeat(SEQUENCES) { sequence ->
            var state: NavigationState = FreshTabs
            repeat(COMMANDS_PER_SEQUENCE) { step ->
                val command =
                    when (random.nextInt(COMMAND_KINDS)) {
                        0 -> GoTo(routes.random(random))
                        1 -> Back
                        else -> ReplaceAll(routes.random(random))
                    }
                val transition = NavigationReducer.reduce(state, command)
                if (transition is Transition.Update) {
                    state = transition.state
                    assertInvariants("sequence $sequence, step $step, after $command", state)
                }
            }
        }
    }

    /** Invariants 1–5 of `data-model.md`. */
    private fun assertInvariants(
        where: String,
        state: NavigationState,
    ) {
        when (state) {
            is SignedOut -> {
                assertTrue("$where: 1. the signed-out stack is not empty", state.stack.isNotEmpty())
                assertNoAdjacentSameKind(where, state.stack)
            }
            is SignedIn -> {
                assertEquals("$where: 2. exactly the three tabs", setOf(Home, Devices, Setting), state.tabStacks.keys)
                state.tabStacks.forEach { (tab, stack) ->
                    assertEquals("$where: 2. the $tab stack starts with its own tab", tab, stack.firstOrNull())
                    assertNoAdjacentSameKind(where, stack)
                    if (tab !in state.visitOrder) {
                        assertEquals("$where: 5. $tab is not visited, so it has no history", listOf<Route>(tab), stack)
                    }
                }
                assertTrue("$where: 3. the visit order is not empty", state.visitOrder.isNotEmpty())
                assertEquals("$where: 3. the visit order has no duplicates", state.visitOrder.distinct(), state.visitOrder)
            }
        }
    }

    private fun assertNoAdjacentSameKind(
        where: String,
        stack: List<Route>,
    ) {
        stack.zipWithNext().forEach { (below, above) ->
            assertNotEquals("$where: 4. $above is stacked on $below", below::class, above::class)
        }
    }

    @Test
    fun `R16 back on Home's first screen with only Home visited leaves the app`() {
        assertEquals(Transition.Exit, NavigationReducer.reduce(FreshTabs, Back))
    }

    @Test
    fun `R19 replacing everything with Home while signed in gives fresh tabs`() {
        val state =
            signedIn(
                devices = listOf(Devices, DeviceDetail(1), BuyModule(1)),
                visitOrder = listOf(Home, Setting, Devices),
            )

        assertEquals(Transition.Update(FreshTabs), NavigationReducer.reduce(state, ReplaceAll(Home)))
        assertEquals(Transition.Update(FreshTabs), NavigationReducer.reduce(FreshTabs, ReplaceAll(Home)))
    }

    @Test
    fun `R20 replacing everything with Sign In signs out`() {
        val state = signedIn(devices = listOf(Devices, DeviceDetail(1)), visitOrder = listOf(Home, Devices))

        assertEquals(Transition.Update(signedOut), NavigationReducer.reduce(state, ReplaceAll(SignIn)))
        assertEquals(Transition.Update(signedOut), NavigationReducer.reduce(signedOut, ReplaceAll(SignIn)))
    }

    @Test
    fun `R22 replacing everything with any other route is ignored`() {
        listOf(signedOut, FreshTabs).forEach { state ->
            listOf(Devices, Setting, DeviceDetail(1), BuyModule(1)).forEach { route ->
                assertEquals(
                    "ReplaceAll($route) from $state",
                    Transition.Ignore,
                    NavigationReducer.reduce(state, ReplaceAll(route)),
                )
            }
        }
    }

    // endregion

    // region held commands

    @Test
    fun `H1 a held back is applied after a canceled gesture`() {
        val result = NavigationReducer.applyHeld(onBuyModule, listOf(IssuedCommand(Back, BuyModule(1))))

        assertEquals(HeldResult(onDeviceDetail, exit = false, applied = 1, dropped = 0), result)
    }

    @Test
    fun `H2 a held back from the screen the gesture left is dropped, so there is no double back`() {
        val result = NavigationReducer.applyHeld(onDeviceDetail, listOf(IssuedCommand(Back, BuyModule(1))))

        assertSame(onDeviceDetail, result.state)
        assertEquals(HeldResult(onDeviceDetail, exit = false, applied = 0, dropped = 1), result)
    }

    @Test
    fun `H3 a held command from the screen now shown is applied`() {
        val result = NavigationReducer.applyHeld(onDeviceDetail, listOf(IssuedCommand(GoTo(BuyModule(1)), DeviceDetail(1))))

        assertEquals(HeldResult(onBuyModule, exit = false, applied = 1, dropped = 0), result)
    }

    @Test
    fun `H4 a held bottom-bar command is always dropped`() {
        val onSetting = signedIn(visitOrder = listOf(Home, Setting))

        val result = NavigationReducer.applyHeld(onSetting, listOf(IssuedCommand(GoTo(Devices), origin = null)))

        assertSame(onSetting, result.state)
        assertEquals(HeldResult(onSetting, exit = false, applied = 0, dropped = 1), result)
    }

    @Test
    fun `H5 each origin is checked against the state at that step`() {
        val held = listOf(IssuedCommand(GoTo(BuyModule(1)), DeviceDetail(1)), IssuedCommand(Back, DeviceDetail(1)))

        val result = NavigationReducer.applyHeld(onDeviceDetail, held)

        assertEquals(HeldResult(onBuyModule, exit = false, applied = 1, dropped = 1), result)
    }

    @Test
    fun `H6 the walk stops at the first exit`() {
        val held = listOf(IssuedCommand(Back, Home), IssuedCommand(GoTo(DeviceDetail(1)), Home))

        val result = NavigationReducer.applyHeld(FreshTabs, held)

        assertEquals(HeldResult(FreshTabs, exit = true, applied = 1, dropped = 0), result)
    }

    @Test
    fun `H7 nothing held changes nothing`() {
        listOf(signedOut, FreshTabs, onBuyModule).forEach { state ->
            val result = NavigationReducer.applyHeld(state, emptyList())

            assertSame(state, result.state)
            assertEquals(HeldResult(state, exit = false, applied = 0, dropped = 0), result)
        }
    }

    @Test
    fun `a held command that the rules ignore keeps the state and counts as applied`() {
        val result = NavigationReducer.applyHeld(onDeviceDetail, listOf(IssuedCommand(GoTo(DeviceDetail(2)), DeviceDetail(1))))

        assertSame(onDeviceDetail, result.state)
        assertEquals(HeldResult(onDeviceDetail, exit = false, applied = 1, dropped = 0), result)
    }

    // endregion

    internal companion object {
        private const val SEED = 20261013
        private const val SEQUENCES = 500
        private const val COMMANDS_PER_SEQUENCE = 40
        private const val COMMAND_KINDS = 3

        val signedOut = SignedOut(listOf(SignIn))

        /** A signed-in state; every tab left out is at its first screen. */
        fun signedIn(
            home: List<Route> = listOf(Home),
            devices: List<Route> = listOf(Devices),
            setting: List<Route> = listOf(Setting),
            visitOrder: List<TopLevelRoute> = listOf(Home),
        ) = SignedIn(tabStacks = mapOf(Home to home, Devices to devices, Setting to setting), visitOrder = visitOrder)

        /** `D=[Devices, DD(1)]`, with Devices as the current tab. */
        val onDeviceDetail = signedIn(devices = listOf(Devices, DeviceDetail(1)), visitOrder = listOf(Home, Devices))

        /** `D=[Devices, DD(1), BM(1)]`, with Devices as the current tab. */
        val onBuyModule =
            signedIn(devices = listOf(Devices, DeviceDetail(1), BuyModule(1)), visitOrder = listOf(Home, Devices))
    }
}
