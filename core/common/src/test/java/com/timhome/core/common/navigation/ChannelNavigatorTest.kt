package com.timhome.core.common.navigation

import com.timhome.core.common.navigation.NavigationCommand.Back
import com.timhome.core.common.navigation.NavigationCommand.GoTo
import com.timhome.core.common.navigation.NavigationCommand.ReplaceAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ChannelNavigatorTest {
    private val navigator = ChannelNavigator()

    @Test
    fun `N1 direct calls emit their command with no origin`() =
        runTest {
            navigator.navigate(DeviceDetail(1))
            navigator.back()
            navigator.replaceAll(Home)

            assertEquals(
                listOf(
                    IssuedCommand(GoTo(DeviceDetail(1)), origin = null),
                    IssuedCommand(Back, origin = null),
                    IssuedCommand(ReplaceAll(Home), origin = null),
                ),
                navigator.commands.take(3).toList(),
            )
        }

    @Test
    fun `N2 commands keep the order they were issued in`() =
        runTest {
            val routes = (1..20).map { DeviceDetail(it) }

            routes.forEach { navigator.navigate(it) }

            assertEquals(routes.map { GoTo(it) }, navigator.commands.take(routes.size).toList().map { it.command })
        }

    @Test
    fun `N3 commands issued with no collector are delivered when collection starts`() =
        runTest {
            navigator.replaceAll(Home)
            advanceUntilIdle()

            assertEquals(IssuedCommand(ReplaceAll(Home), origin = null), navigator.commands.first())
        }

    @Test
    fun `N4 a command is never delivered twice, also across re-collection`() =
        runTest {
            navigator.navigate(Devices)
            navigator.back()

            val first = navigator.commands.first()
            val second = navigator.commands.first()
            val received = mutableListOf<IssuedCommand>()
            val collector = launch { navigator.commands.collect { received += it } }
            advanceUntilIdle()
            collector.cancel()

            assertEquals(IssuedCommand(GoTo(Devices), origin = null), first)
            assertEquals(IssuedCommand(Back, origin = null), second)
            assertEquals(emptyList<IssuedCommand>(), received)
        }

    @Test
    fun `N5 calls never suspend or throw, whatever is buffered`() {
        repeat(1_000) { navigator.navigate(DeviceDetail(it)) }
        navigator.back()
        navigator.replaceAll(SignIn)

        assertEquals(1_002, navigator.discardPending())
    }

    @Test
    fun `N6 discardPending drops buffered and held commands and returns the total`() =
        runTest {
            navigator.navigate(Devices)
            navigator.back()
            navigator.hold(IssuedCommand(Back, origin = BuyModule(1)))

            assertEquals(3, navigator.discardPending())
            assertEquals(emptyList<IssuedCommand>(), navigator.takeHeld())

            navigator.navigate(Setting)
            assertEquals(IssuedCommand(GoTo(Setting), origin = null), navigator.commands.first())
        }

    @Test
    fun `N6 discardPending returns zero when nothing is pending`() {
        assertEquals(0, navigator.discardPending())
    }

    @Test
    fun `N7 scoped navigators emit their origin and share one order`() =
        runTest {
            val detail = navigator.issuedFrom(DeviceDetail(1))
            val buy = navigator.issuedFrom(BuyModule(1))

            detail.navigate(BuyModule(1))
            buy.back()
            detail.replaceAll(Home)
            detail.back()

            assertEquals(
                listOf(
                    IssuedCommand(GoTo(BuyModule(1)), origin = DeviceDetail(1)),
                    IssuedCommand(Back, origin = BuyModule(1)),
                    IssuedCommand(ReplaceAll(Home), origin = DeviceDetail(1)),
                    IssuedCommand(Back, origin = DeviceDetail(1)),
                ),
                navigator.commands.take(4).toList(),
            )
        }

    @Test
    fun `N8 takeHeld returns held commands in order, once, and never emits them`() =
        runTest {
            val first = IssuedCommand(Back, origin = BuyModule(1))
            val second = IssuedCommand(GoTo(BuyModule(2)), origin = DeviceDetail(2))
            navigator.hold(first)
            navigator.hold(second)

            val received = mutableListOf<IssuedCommand>()
            val collector = launch { navigator.commands.collect { received += it } }
            advanceUntilIdle()
            collector.cancel()

            assertEquals(emptyList<IssuedCommand>(), received)
            assertEquals(listOf(first, second), navigator.takeHeld())
            assertEquals(emptyList<IssuedCommand>(), navigator.takeHeld())
        }

    @Test
    fun `N9 a held command survives the collector being replaced`() =
        runTest {
            val heldCommand = IssuedCommand(Back, origin = BuyModule(1))
            val firstCollector = launch { navigator.commands.collect {} }
            advanceUntilIdle()
            navigator.hold(heldCommand)
            firstCollector.cancel()

            val secondCollector = launch { navigator.commands.collect {} }
            advanceUntilIdle()

            assertEquals(listOf(heldCommand), navigator.takeHeld())
            secondCollector.cancel()
        }
}
