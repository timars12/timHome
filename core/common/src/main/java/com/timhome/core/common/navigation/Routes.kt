package com.timhome.core.common.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations shared across features.
 *
 * They live here (next to [com.timhome.core.common.NavigationDispatcher]) so features can
 * reference each other's route contracts without depending on each other's implementations.
 */

@Serializable
data object SignIn

@Serializable
data object Home

@Serializable
data object Devices

@Serializable
data class DeviceDetail(val deviceId: Int)

@Serializable
data class BuyModule(val deviceId: Int)

@Serializable
data object Setting
