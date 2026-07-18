package com.timhome.modularizationtest.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class BottomNavigationMenuItem(
    val route: Any,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
    val testTag: String,
)
