package com.timhome.core.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.timhome.core.common.navigation.Route

/** Carries the route of a navigation entry into [ViewModelFactory]. */
val RouteExtrasKey: CreationExtras.Key<Route> = object : CreationExtras.Key<Route> {}

/**
 * The default creation extras of the current ViewModel store owner, plus [route]. Pass it as
 * `extras` wherever a screen creates its ViewModel with [ViewModelFactory].
 */
@Composable
fun routeExtras(route: Route): CreationExtras {
    val owner = checkNotNull(LocalViewModelStoreOwner.current) { "No ViewModelStoreOwner for $route" }
    val defaults =
        if (owner is HasDefaultViewModelProviderFactory) owner.defaultViewModelCreationExtras else CreationExtras.Empty
    return MutableCreationExtras(defaults).apply { set(RouteExtrasKey, route) }
}
