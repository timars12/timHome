package com.timhome.modularizationtest.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.timhome.core.common.navigation.TopLevelRoute

data class BottomNavigationMenuItem(
    val route: TopLevelRoute,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    val testTag: String,
)
