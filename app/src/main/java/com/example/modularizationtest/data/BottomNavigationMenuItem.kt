package com.example.modularizationtest.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class BottomNavigationMenuItem(
    val id: Byte,
    @StringRes val label: Int,
    @DrawableRes val icon: Int
)