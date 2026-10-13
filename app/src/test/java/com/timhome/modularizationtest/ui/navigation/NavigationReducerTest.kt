package com.timhome.modularizationtest.ui.navigation

import com.timhome.core.common.navigation.BuyModule
import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.common.navigation.Devices
import com.timhome.core.common.navigation.Home
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

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

    private companion object {
        val signedOut = SignedOut(listOf(SignIn))

        /** A signed-in state; every tab left out is at its first screen. */
        fun signedIn(
            home: List<Route> = listOf(Home),
            devices: List<Route> = listOf(Devices),
            setting: List<Route> = listOf(Setting),
            visitOrder: List<TopLevelRoute> = listOf(Home),
        ) = SignedIn(tabStacks = mapOf(Home to home, Devices to devices, Setting to setting), visitOrder = visitOrder)
    }
}
