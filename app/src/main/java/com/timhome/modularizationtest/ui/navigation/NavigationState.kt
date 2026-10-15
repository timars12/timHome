package com.timhome.modularizationtest.ui.navigation

import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.Route
import com.timhome.core.common.navigation.TopLevelRoute
import com.timhome.core.common.navigation.topLevelRoutes
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * The full picture of where the user is. It is immutable and saved as one snapshot.
 *
 * Invariants:
 * 1. [SignedOut.stack] is not empty.
 * 2. [SignedIn.tabStacks] has exactly the keys `{Home, Devices, Setting}`, and every stack is
 *    non-empty and starts with its own tab route.
 * 3. [SignedIn.visitOrder] is not empty, has no duplicates, and contains only [TopLevelRoute]s.
 * 4. Within a single stack, no two adjacent entries have the same route type (FR-026).
 * 5. A tab that is not in [SignedIn.visitOrder] has a stack equal to `[tab]`.
 */
@Serializable
internal sealed interface NavigationState {
    /** The screen that is shown. */
    val currentRoute: Route

    val isBottomBarVisible: Boolean

    @Serializable
    data class SignedOut(val stack: List<Route>) : NavigationState {
        override val currentRoute: Route get() = stack.last()
        override val isBottomBarVisible: Boolean get() = false
    }

    /**
     * @param tabStacks one stack per tab, with all tabs present.
     * @param visitOrder the tab visit history, most recent last.
     */
    @Serializable
    data class SignedIn(
        val tabStacks: Map<TopLevelRoute, List<Route>>,
        val visitOrder: List<TopLevelRoute>,
    ) : NavigationState {
        val currentTab: TopLevelRoute get() = visitOrder.last()

        /** The stacks of the visited tabs, in visit order. Flattened, this is what is displayed. */
        val displayedStacks: List<List<Route>> get() = visitOrder.map { tabStacks.getValue(it) }

        override val currentRoute: Route get() = tabStacks.getValue(currentTab).last()
        override val isBottomBarVisible: Boolean get() = true
    }
}

/** Every tab at its first screen, with only Home visited. */
internal val FreshTabs: NavigationState.SignedIn =
    NavigationState.SignedIn(
        tabStacks = topLevelRoutes.associateWith { listOf(it) },
        visitOrder = listOf(Home),
    )

/**
 * The only [Json] used to save [NavigationState]. The default instance refuses
 * [NavigationState.SignedIn.tabStacks], because its keys are sealed objects.
 */
internal val NavigationJson = Json { allowStructuredMapKeys = true }
