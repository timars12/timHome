package com.example.modularizationtest.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class BottomNavigationMenuItem(
    val destinationName: String,
    @StringRes val label: Int,
    @DrawableRes val icon: Int,
)
