package com.timhome.modularizationtest.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.timhome.core.common.navigation.Route
import com.timhome.core.common.navigation.topLevelRoutes
import com.timhome.modularizationtest.ui.navigation.NavigationState.SignedIn
import com.timhome.modularizationtest.ui.navigation.NavigationState.SignedOut

/**
 * The entries to display for [state]: the signed-out stack, or the stacks of the visited tabs in
 * visit order. It holds no navigation state of its own.
 *
 * Each stack is decorated on its own, so a screen's ViewModel and saved UI state are released
 * only when the screen leaves its own stack, not when the user switches tabs.
 *
 * All three tab stacks are decorated on every recomposition, including the tabs that are not in
 * the visit order. A stack that stopped being decorated would release everything it holds. This
 * way a tab's first screen, which never leaves its stack, keeps its ViewModel and saved UI state
 * even after the user backs out of the tab (FR-006, FR-011).
 */
@Composable
internal fun rememberNavigationEntries(
    state: NavigationState,
    entryProvider: (Route) -> NavEntry<Route>,
): List<NavEntry<Route>> {
    val signedIn = state as? SignedIn
    val signedOutEntries = rememberDecoratedStack((state as? SignedOut)?.stack.orEmpty(), entryProvider)
    val tabEntries =
        topLevelRoutes.associateWith { tab ->
            key(tab) { rememberDecoratedStack(signedIn?.tabStacks?.get(tab).orEmpty(), entryProvider) }
        }
    return signedIn?.visitOrder?.flatMap { tabEntries.getValue(it) } ?: signedOutEntries
}

@Composable
private fun rememberDecoratedStack(
    stack: List<Route>,
    entryProvider: (Route) -> NavEntry<Route>,
): List<NavEntry<Route>> =
    rememberDecoratedNavEntries(
        backStack = stack,
        entryDecorators =
            listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
        entryProvider = entryProvider,
    )
