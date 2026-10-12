package com.timhome.core.ui.viewmodel

import androidx.activity.ComponentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import com.timhome.core.common.navigation.ChannelNavigator
import com.timhome.core.common.navigation.DeviceDetail
import com.timhome.core.common.navigation.IssuedCommand
import com.timhome.core.common.navigation.NavigationCommand.Back
import com.timhome.core.common.navigation.Navigator
import com.timhome.core.common.navigation.Route
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

/**
 * Robolectric, because `createSavedStateHandle()` needs a real saved-state owner: a
 * [ComponentActivity] supplies the default creation extras.
 */
@RunWith(RobolectricTestRunner::class)
class ViewModelFactoryTest {
    private val channelNavigator = ChannelNavigator()
    private val route = DeviceDetail(7)

    private class RoutedViewModel : ViewModel()

    private class PlainViewModel : ViewModel()

    private class RecordingFactory : ViewModelAssistedFactory<RoutedViewModel> {
        var route: Route? = null
        var navigator: Navigator? = null

        override fun create(handle: SavedStateHandle): RoutedViewModel = error("the 3-argument create must be used")

        override fun create(
            handle: SavedStateHandle,
            route: Route,
            navigator: Navigator,
        ): RoutedViewModel {
            this.route = route
            this.navigator = navigator
            return RoutedViewModel()
        }
    }

    private class PlainFactory : ViewModelAssistedFactory<PlainViewModel> {
        val created = PlainViewModel()

        override fun create(handle: SavedStateHandle): PlainViewModel = created
    }

    private val recordingFactory = RecordingFactory()
    private val plainFactory = PlainFactory()
    private val factory =
        ViewModelFactory(
            mapOf(RoutedViewModel::class.java to recordingFactory, PlainViewModel::class.java to plainFactory),
            channelNavigator,
        )

    private fun <T : ViewModel> create(
        modelClass: Class<T>,
        extras: (ComponentActivity) -> CreationExtras,
    ): T {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        return ViewModelProvider.create(activity.viewModelStore, factory, extras(activity))[modelClass]
    }

    private fun routeExtrasOf(activity: ComponentActivity): CreationExtras =
        MutableCreationExtras(activity.defaultViewModelCreationExtras).apply { set(RouteExtrasKey, route) }

    @Test
    fun `passes the entry route and a navigator scoped to it`() =
        runTest {
            create(RoutedViewModel::class.java, ::routeExtrasOf)

            assertEquals(route, recordingFactory.route)
            recordingFactory.navigator!!.back()
            assertEquals(IssuedCommand(Back, origin = route), channelNavigator.commands.first())
        }

    @Test
    fun `the default three-argument create delegates to create(handle)`() {
        val viewModel = create(PlainViewModel::class.java, ::routeExtrasOf)

        assertSame(plainFactory.created, viewModel)
    }

    @Test
    fun `a missing route fails with IllegalStateException`() {
        assertThrows(IllegalStateException::class.java) {
            create(RoutedViewModel::class.java) { it.defaultViewModelCreationExtras }
        }
    }
}
