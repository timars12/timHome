package com.timhome.modularizationtest.ui.navigation

import androidx.activity.BackEventCompat
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedDispatcher
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.SavedStateHandle
import androidx.navigation3.runtime.entryProvider
import com.timhome.core.common.navigation.BuyModule
import com.timhome.core.common.navigation.ChannelNavigator
import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.common.navigation.Devices
import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.IssuedCommand
import com.timhome.core.common.navigation.NavigationCommand
import com.timhome.core.common.navigation.Route
import com.timhome.core.common.navigation.Setting
import com.timhome.core.common.navigation.SignIn
import com.timhome.core.ui.scaleTransition
import com.timhome.modularizationtest.ui.navigation.NavigationReducerTest.Companion.onBuyModule
import com.timhome.modularizationtest.ui.navigation.NavigationReducerTest.Companion.onDeviceDetail
import com.timhome.modularizationtest.ui.navigation.NavigationReducerTest.Companion.signedIn
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Checks the order in which [NavigationHost] releases the commands held during a predictive back
 * gesture: the gesture's own back is applied first.
 *
 * It runs under Robolectric. The gesture is driven through the activity's
 * `onBackPressedDispatcher`, which moves the navigation event dispatcher's transition state to
 * `InProgress` and back to `Idle`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NavigationHostGestureTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private val channelNavigator = ChannelNavigator()
    private val navigationViewModel = NavigationViewModel(SavedStateHandle())
    private var exited = false

    private val labels =
        entryProvider<Route> {
            entry<SignIn>(metadata = scaleTransition()) { Text("SignIn") }
            entry<Home>(metadata = scaleTransition()) { Text("Home") }
            entry<Devices>(metadata = scaleTransition()) { Text("Devices") }
            entry<Setting>(metadata = scaleTransition()) { Text("Setting") }
            entry<DeviceDetail>(metadata = scaleTransition()) { Text("DeviceDetail ${it.deviceId}") }
            entry<BuyModule>(metadata = scaleTransition()) { Text("BuyModule ${it.deviceId}") }
        }

    private val state get() = navigationViewModel.state.value

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
        composeRule.waitForIdle()
    }

    private fun onDispatcher(step: OnBackPressedDispatcher.() -> Unit) {
        composeRule.runOnUiThread { composeRule.activity.onBackPressedDispatcher.step() }
        composeRule.waitForIdle()
    }

    private fun startGesture() {
        onDispatcher { dispatchOnBackStarted(backEvent(progress = 0f)) }
        onDispatcher { dispatchOnBackProgressed(backEvent(progress = 0.5f)) }
    }

    private fun completeGesture() = onDispatcher { onBackPressed() }

    private fun cancelGesture() = onDispatcher { dispatchOnBackCancelled() }

    private fun backEvent(progress: Float) =
        BackEventCompat(touchX = 0f, touchY = 0f, progress = progress, swipeEdge = BackEventCompat.EDGE_LEFT)

    @Test
    fun `a back issued during a completed gesture does not go back twice`() {
        host(onBuyModule)
        startGesture()

        channelNavigator.issuedFrom(BuyModule(1)).back()
        composeRule.waitForIdle()

        assertEquals("nothing moves while the finger is down", onBuyModule, state)

        completeGesture()

        assertEquals(onDeviceDetail, state)
        assertFalse(exited)
    }

    @Test
    fun `a back issued during a canceled gesture is applied once`() {
        host(onBuyModule)
        startGesture()

        channelNavigator.issuedFrom(BuyModule(1)).back()
        composeRule.waitForIdle()
        cancelGesture()

        assertEquals(onDeviceDetail, state)
        assertFalse(exited)
    }

    @Test
    fun `a bottom-bar tap during a completed gesture is ignored`() {
        host(onDeviceDetail)
        startGesture()

        channelNavigator.navigate(Setting)
        composeRule.waitForIdle()
        completeGesture()

        assertEquals(signedIn(visitOrder = listOf(Home, Devices)), state)
    }

    @Test
    fun `a bottom-bar tap during a canceled gesture is ignored`() {
        host(onDeviceDetail)
        startGesture()

        channelNavigator.navigate(Setting)
        composeRule.waitForIdle()
        cancelGesture()

        assertEquals(onDeviceDetail, state)
    }

    @Test
    fun `with no gesture a back is applied right away`() {
        host(onBuyModule)

        channelNavigator.issuedFrom(BuyModule(1)).back()
        composeRule.waitForIdle()

        assertEquals(onDeviceDetail, state)
    }

    @Test
    fun `commands held before the host was recreated are released when it starts`() {
        channelNavigator.hold(IssuedCommand(NavigationCommand.Back, BuyModule(1)))

        host(onBuyModule)

        assertEquals(onDeviceDetail, state)
    }
}
