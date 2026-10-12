package com.timhome.modularizationtest.ui.navigation

import androidx.lifecycle.SavedStateHandle
import com.timhome.core.common.navigation.NavigationCommand.Back
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
    fun `V7 back on the only tab's first screen asks to finish`() {
        val viewModel = NavigationViewModel(savedStateHandle)
        viewModel.start(FreshTabs)

        assertTrue(viewModel.dispatch(Back))
    }
}
