package com.timhome.modularizationtest.ui.navigation

import androidx.navigation3.runtime.entryProvider
import com.timhome.auth.ui.signin.navigation.signInEntries
import com.timhome.core.common.navigation.BuyModule
import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.common.navigation.Devices
import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.Route
import com.timhome.core.common.navigation.Setting
import com.timhome.core.common.navigation.SignIn
import com.timhome.core.ui.TransitionStyle
import com.timhome.core.ui.transitionStyle
import com.timhome.device.ui.navigation.deviceEntries
import com.timhome.home.ui.navigation.homeEntries
import com.timhome.settings.ui.navigation.settingEntries
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Cases T1–T5 of `contracts/navigation-rules.md`, against the app's real entries. Only their
 * metadata is read; no screen is composed.
 */
class TransitionSelectionTest {
    private val entries =
        entryProvider<Route> {
            signInEntries()
            homeEntries()
            deviceEntries()
            settingEntries()
        }

    private fun styleShowing(route: Route) = transitionStyleFor(entries(route).metadata)

    @Test
    fun `T1 showing Sign In slides`() {
        assertEquals(TransitionStyle.Slide, styleShowing(SignIn))
    }

    @Test
    fun `T1 T3 showing Home slides`() {
        assertEquals(TransitionStyle.Slide, styleShowing(Home))
    }

    @Test
    fun `T2 showing a tab other than Home scales`() {
        assertEquals(TransitionStyle.Scale, styleShowing(Devices))
        assertEquals(TransitionStyle.Scale, styleShowing(Setting))
    }

    @Test
    fun `T4 showing Device Detail or Buy Module scales`() {
        assertEquals(TransitionStyle.Scale, styleShowing(DeviceDetail(1)))
        assertEquals(TransitionStyle.Scale, styleShowing(BuyModule(1)))
    }

    @Test
    fun `T5 every entry declares a style`() {
        allRoutes.forEach { route ->
            assertNotNull("$route", entries(route).metadata.transitionStyle())
        }
    }

    @Test
    fun `an entry without a style is an error`() {
        assertThrows(IllegalStateException::class.java) { transitionStyleFor(emptyMap()) }
    }

    private companion object {
        /** One sample of every [Route] subtype. The `when` stops compiling when a subtype is added. */
        val allRoutes = listOf(SignIn, Home, Devices, DeviceDetail(1), BuyModule(1), Setting).onEach(::checkListed)

        fun checkListed(route: Route) =
            when (route) {
                SignIn, Home, Devices, Setting, is DeviceDetail, is BuyModule -> Unit
            }
    }
}
