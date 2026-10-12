package com.timhome.core.common.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations shared across features.
 *
 * They live here (next to [Navigator]) so features can reference each other's route contracts
 * without depending on each other's implementations.
 */
@Serializable
sealed interface Route

/** A [Route] that is the first screen of a bottom-bar tab. */
@Serializable
sealed interface TopLevelRoute : Route

@Serializable
data object SignIn : Route

@Serializable
data object Home : TopLevelRoute

@Serializable
data object Devices : TopLevelRoute

@Serializable
data class DeviceDetail(val deviceId: Int) : Route

@Serializable
data class BuyModule(val deviceId: Int) : Route

@Serializable
data object Setting : TopLevelRoute

/** The tabs, in bottom-bar order. */
val topLevelRoutes: List<TopLevelRoute> = listOf(Home, Devices, Setting)
