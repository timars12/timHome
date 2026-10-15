package com.timhome.modularizationtest.ui.navigation

import com.timhome.core.common.navigation.BuyModule
import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.common.navigation.Devices
import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.Setting
import com.timhome.modularizationtest.ui.navigation.NavigationReducerTest.Companion.onBuyModule
import com.timhome.modularizationtest.ui.navigation.NavigationReducerTest.Companion.onDeviceDetail
import com.timhome.modularizationtest.ui.navigation.NavigationReducerTest.Companion.signedIn
import com.timhome.modularizationtest.ui.navigation.NavigationReducerTest.Companion.signedOut
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/** The saved form of [NavigationState]: what is written before process death is read back equal. */
class NavigationStateJsonTest {
    @Test
    fun `every state reached by the navigation rules survives a round trip`() {
        reachedStates.forEach { state ->
            assertEquals(state, roundTrip(state))
        }
    }

    @Test
    fun `screen arguments survive a round trip`() {
        val state =
            signedIn(
                devices = listOf(Devices, DeviceDetail(2), BuyModule(7)),
                visitOrder = listOf(Home, Devices),
            )

        val restored = roundTrip(state) as NavigationState.SignedIn

        assertEquals(listOf(Devices, DeviceDetail(2), BuyModule(7)), restored.tabStacks[Devices])
        assertEquals(BuyModule(7), restored.currentRoute)
    }

    @Test
    fun `the tab visit history survives a round trip in order`() {
        val state = signedIn(visitOrder = listOf(Devices, Setting, Home))

        val restored = roundTrip(state) as NavigationState.SignedIn

        assertEquals(listOf(Devices, Setting, Home), restored.visitOrder)
        assertEquals(Home, restored.currentTab)
    }

    @Test
    fun `the default Json refuses a signed-in state`() {
        assertThrows(SerializationException::class.java) {
            Json.encodeToString(NavigationState.serializer(), FreshTabs)
        }
    }

    private fun roundTrip(state: NavigationState): NavigationState =
        NavigationJson.decodeFromString(
            NavigationState.serializer(),
            NavigationJson.encodeToString(NavigationState.serializer(), state),
        )

    private companion object {
        /** The states that appear as a start or a result in rules R0a–R22. */
        val reachedStates: List<NavigationState> =
            listOf(
                signedOut,
                FreshTabs,
                signedIn(visitOrder = listOf(Home, Devices)),
                onDeviceDetail,
                onBuyModule,
                signedIn(visitOrder = listOf(Home, Devices, Setting)),
                signedIn(visitOrder = listOf(Devices, Setting, Home)),
                signedIn(devices = listOf(Devices, DeviceDetail(1)), visitOrder = listOf(Home, Setting, Devices)),
                signedIn(visitOrder = listOf(Home, Setting, Devices)),
                signedIn(visitOrder = listOf(Home, Setting)),
                signedIn(visitOrder = listOf(Devices, Home)),
                signedIn(visitOrder = listOf(Devices)),
                signedIn(visitOrder = listOf(Setting)),
                signedIn(
                    devices = listOf(Devices, DeviceDetail(1), BuyModule(1)),
                    visitOrder = listOf(Home, Setting, Devices),
                ),
            )
    }
}
