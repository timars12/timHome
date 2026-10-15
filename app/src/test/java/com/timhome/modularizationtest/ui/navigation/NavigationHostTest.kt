package com.timhome.modularizationtest.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.entryProvider
import com.timhome.core.common.navigation.BuyModule
import com.timhome.core.common.navigation.ChannelNavigator
import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.common.navigation.Devices
import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.Navigator
import com.timhome.core.common.navigation.Route
import com.timhome.core.common.navigation.Setting
import com.timhome.core.common.navigation.SignIn
import com.timhome.core.ui.scaleTransition
import com.timhome.core.ui.viewmodel.ViewModelAssistedFactory
import com.timhome.core.ui.viewmodel.ViewModelFactory
import com.timhome.core.ui.viewmodel.routeExtras
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Hosts [NavigationHost] in a plain activity, with a text label per route instead of the real
 * screens, so it runs without the app's Dagger graph.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NavigationHostTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val channelNavigator = ChannelNavigator()
    private val navigationViewModel = NavigationViewModel(SavedStateHandle())
    private var exited = false

    private class ProbeViewModel(val route: Route, val navigator: Navigator) : ViewModel() {
        var cleared = false

        override fun onCleared() {
            cleared = true
        }
    }

    private val probes = mutableListOf<ProbeViewModel>()
    private val probeFactory =
        object : ViewModelAssistedFactory<ProbeViewModel> {
            override fun create(handle: SavedStateHandle): ProbeViewModel = error("needs a route and a navigator")

            override fun create(
                handle: SavedStateHandle,
                route: Route,
                navigator: Navigator,
            ): ProbeViewModel = ProbeViewModel(route, navigator).also { probes += it }
        }
    private val viewModelFactory = ViewModelFactory(mapOf(ProbeViewModel::class.java to probeFactory), channelNavigator)

    private val labels =
        entryProvider<Route> {
            entry<SignIn>(metadata = scaleTransition()) { Text("SignIn") }
            entry<Home>(metadata = scaleTransition()) { Text("Home") }
            entry<Devices>(metadata = scaleTransition()) { Text("Devices") }
            entry<Setting>(metadata = scaleTransition()) { Text("Setting") }
            entry<DeviceDetail>(metadata = scaleTransition()) {
                viewModel<ProbeViewModel>(factory = viewModelFactory, extras = routeExtras(it))
                Text("DeviceDetail ${it.deviceId}")
            }
            entry<BuyModule>(metadata = scaleTransition()) { Text("BuyModule ${it.deviceId}") }
        }

    private fun host(start: NavigationState) {
        navigationViewModel.start(start)
        composeRule.setContent {
            val state by navigationViewModel.state.collectAsState()
            state?.let {
                NavigationHost(
                    state = it,
                    navigationViewModel = navigationViewModel,
                    channelNavigator = channelNavigator,
                    entryProvider = labels,
                    onExit = { exited = true },
                )
            }
        }
    }

    private fun assertShown(label: String) {
        composeRule.waitForIdle()
        composeRule.onNodeWithText(label).assertIsDisplayed()
    }

    private fun devicesStack(): List<Route>? = (navigationViewModel.state.value as NavigationState.SignedIn).tabStacks[Devices]

    @Test
    fun `shows the signed-out screen, then Home after replaceAll`() {
        host(NavigationReducer.initialState(isSignedIn = false))
        assertShown("SignIn")

        channelNavigator.issuedFrom(SignIn).replaceAll(Home)

        assertShown("Home")
        assertEquals(FreshTabs, navigationViewModel.state.value)
    }

    @Test
    fun `a command issued before the host exists is applied on top of the start state`() {
        channelNavigator.navigate(Devices)

        host(FreshTabs)

        assertShown("Devices")
    }

    @Test
    fun `a command issued in the background is applied once when the app returns`() {
        host(FreshTabs)
        channelNavigator.navigate(Devices)
        channelNavigator.issuedFrom(Devices).navigate(DeviceDetail(1))
        channelNavigator.issuedFrom(DeviceDetail(1)).navigate(BuyModule(1))
        assertShown("BuyModule 1")

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        channelNavigator.issuedFrom(BuyModule(1)).back()
        composeRule.waitForIdle()

        // Not applied while the app is in the background (FR-015).
        assertEquals(listOf(Devices, DeviceDetail(1), BuyModule(1)), devicesStack())

        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        // Applied exactly once: a second Back would have popped Device Detail too.
        assertShown("DeviceDetail 1")
        assertEquals(listOf(Devices, DeviceDetail(1)), devicesStack())
        assertFalse(exited)
    }

    @Test
    fun `pushes and pops screens inside a tab and switches tabs`() {
        host(FreshTabs)
        assertShown("Home")

        channelNavigator.navigate(Devices)
        assertShown("Devices")

        channelNavigator.issuedFrom(Devices).navigate(DeviceDetail(1))
        assertShown("DeviceDetail 1")

        channelNavigator.issuedFrom(DeviceDetail(1)).navigate(BuyModule(1))
        assertShown("BuyModule 1")

        channelNavigator.issuedFrom(BuyModule(1)).back()
        assertShown("DeviceDetail 1")

        channelNavigator.navigate(Setting)
        assertShown("Setting")

        channelNavigator.navigate(Devices)
        assertShown("DeviceDetail 1")
        assertFalse(exited)
    }

    @Test
    fun `system back pops a screen`() {
        host(FreshTabs)
        channelNavigator.navigate(Devices)
        channelNavigator.issuedFrom(Devices).navigate(DeviceDetail(1))
        assertShown("DeviceDetail 1")

        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.onBackPressed() }

        assertShown("Devices")
        assertFalse(exited)
    }

    @Test
    fun `back on the only tab's first screen asks to exit`() {
        host(FreshTabs)
        assertShown("Home")

        channelNavigator.issuedFrom(Home).back()
        composeRule.waitForIdle()

        assertTrue(exited)
    }

    @Test
    fun `a screen's ViewModel gets its route, survives a tab switch and is cleared when popped`() {
        host(FreshTabs)
        channelNavigator.navigate(Devices)
        channelNavigator.issuedFrom(Devices).navigate(DeviceDetail(1))
        assertShown("DeviceDetail 1")
        val created = probes.single()
        assertEquals(DeviceDetail(1), created.route)

        channelNavigator.navigate(Setting)
        assertShown("Setting")
        channelNavigator.navigate(Devices)
        assertShown("DeviceDetail 1")

        assertEquals(listOf(created), probes)
        assertFalse(created.cleared)

        created.navigator.back()
        assertShown("Devices")

        assertTrue(created.cleared)
    }
}
