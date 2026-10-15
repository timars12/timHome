package com.timhome.modularizationtest.ui.navigation

import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.common.navigation.Devices
import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.Setting
import com.timhome.core.common.navigation.SignIn
import com.timhome.modularizationtest.ui.navigation.NavigationReducerTest.Companion.onDeviceDetail
import com.timhome.modularizationtest.ui.navigation.NavigationReducerTest.Companion.signedIn
import com.timhome.modularizationtest.ui.navigation.NavigationReducerTest.Companion.signedOut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The properties derived from [NavigationState]. */
class NavigationStateTest {
    @Test
    fun `signed out shows the top of its stack with no bottom bar`() {
        assertEquals(SignIn, signedOut.currentRoute)
        assertFalse(signedOut.isBottomBarVisible)
    }

    @Test
    fun `signed in shows the top of the current tab with the bottom bar`() {
        assertEquals(Devices, onDeviceDetail.currentTab)
        assertEquals(DeviceDetail(1), onDeviceDetail.currentRoute)
        assertTrue(onDeviceDetail.isBottomBarVisible)
    }

    @Test
    fun `displayed stacks are the visited tabs' stacks in visit order`() {
        val state =
            signedIn(
                devices = listOf(Devices, DeviceDetail(1)),
                visitOrder = listOf(Devices, Setting, Home),
            )

        assertEquals(listOf(listOf(Devices, DeviceDetail(1)), listOf(Setting), listOf(Home)), state.displayedStacks)
    }

    @Test
    fun `tabs that are not visited are not displayed`() {
        assertEquals(listOf(listOf(Home)), FreshTabs.displayedStacks)
    }

    @Test
    fun `each variant round-trips through its own serializer`() {
        val signedOutJson = NavigationJson.encodeToString(NavigationState.SignedOut.serializer(), signedOut)
        val signedInJson = NavigationJson.encodeToString(NavigationState.SignedIn.serializer(), onDeviceDetail)

        assertEquals(signedOut, NavigationJson.decodeFromString(NavigationState.SignedOut.serializer(), signedOutJson))
        assertEquals(onDeviceDetail, NavigationJson.decodeFromString(NavigationState.SignedIn.serializer(), signedInJson))
    }
}
