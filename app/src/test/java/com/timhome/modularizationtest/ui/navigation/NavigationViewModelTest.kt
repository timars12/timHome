package com.timhome.modularizationtest.ui.navigation

import androidx.lifecycle.SavedStateHandle
import com.timhome.core.common.navigation.Devices
import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.NavigationCommand.Back
import com.timhome.core.common.navigation.NavigationCommand.GoTo
import com.timhome.core.common.navigation.SignIn
import com.timhome.modularizationtest.ui.navigation.NavigationState.SignedOut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationViewModelTest {
    private val savedStateHandle = SavedStateHandle()

    @Test
    fun `V1 nothing saved gives no restored state and no state`() {
        val viewModel = NavigationViewModel(savedStateHandle)

        assertNull(viewModel.restored)
        assertNull(viewModel.state.value)
    }

    @Test
    fun `V2 dispatch does nothing while there is no state`() {
        val viewModel = NavigationViewModel(savedStateHandle)

        assertFalse(viewModel.dispatch(Back))

        assertNull(viewModel.state.value)
        assertFalse(savedStateHandle.contains(NavigationViewModel.KEY_STATE))
    }

    @Test
    fun `V3 only the first start sets the state`() {
        val viewModel = NavigationViewModel(savedStateHandle)

        viewModel.start(FreshTabs)
        viewModel.start(SignedOut(listOf(SignIn)))

        assertEquals(FreshTabs, viewModel.state.value)
    }

    @Test
    fun `V4 every state change is saved`() {
        val viewModel = NavigationViewModel(savedStateHandle)

        viewModel.start(FreshTabs)
        assertEquals(FreshTabs, savedState())

        assertFalse(viewModel.dispatch(GoTo(Devices)))

        assertEquals(onDevices, viewModel.state.value)
        assertEquals(onDevices, savedState())
    }

    @Test
    fun `V4 a command that changes nothing leaves the saved state as it was`() {
        val viewModel = NavigationViewModel(savedStateHandle)
        viewModel.start(FreshTabs)

        viewModel.dispatch(GoTo(Home))
        viewModel.dispatch(Back)

        assertEquals(FreshTabs, savedState())
    }

    @Test
    fun `V5 a signed-in state saved before process death is the state again, with no start`() {
        val beforeDeath = NavigationViewModel(savedStateHandle)
        beforeDeath.start(FreshTabs)
        beforeDeath.dispatch(GoTo(Devices))
        val savedString = savedStateHandle.get<String>(NavigationViewModel.KEY_STATE)

        val afterDeath = NavigationViewModel(SavedStateHandle(mapOf(NavigationViewModel.KEY_STATE to savedString)))

        assertEquals(onDevices, afterDeath.restored)
        assertEquals(onDevices, afterDeath.state.value)
    }

    @Test
    fun `V6 a restored Sign In waits for start`() {
        val signedOut = SignedOut(listOf(SignIn))
        val savedString = NavigationJson.encodeToString(NavigationState.serializer(), signedOut)

        val viewModel = NavigationViewModel(SavedStateHandle(mapOf(NavigationViewModel.KEY_STATE to savedString)))

        assertEquals(signedOut, viewModel.restored)
        assertNull(viewModel.state.value)

        viewModel.start(FreshTabs)

        assertEquals(FreshTabs, viewModel.state.value)
    }

    @Test
    fun `V7 back on the only tab's first screen asks to finish`() {
        val viewModel = NavigationViewModel(savedStateHandle)
        viewModel.start(FreshTabs)

        assertTrue(viewModel.dispatch(Back))
    }

    private fun savedState(): NavigationState? =
        savedStateHandle.get<String>(NavigationViewModel.KEY_STATE)?.let {
            NavigationJson.decodeFromString(NavigationState.serializer(), it)
        }

    private companion object {
        val onDevices = FreshTabs.copy(visitOrder = listOf(Home, Devices))
    }
}
