package com.timhome.modularizationtest.ui.navigation

import com.timhome.core.common.navigation.Devices
import com.timhome.core.common.navigation.Home
import com.timhome.core.common.navigation.Setting
import com.timhome.modularizationtest.R
import com.timhome.modularizationtest.data.BottomNavigationMenuItem

/** The bottom-bar items, in display order. */
internal val topLevelTabs =
    listOf(
        BottomNavigationMenuItem(
            route = Home,
            label = R.string.home,
            icon = R.drawable.ic_home_bottom_menu,
            testTag = "bnv_home",
        ),
        BottomNavigationMenuItem(
            route = Devices,
            label = R.string.device,
            icon = R.drawable.ic_device_bottom_menu,
            testTag = "bnv_device",
        ),
        BottomNavigationMenuItem(
            route = Setting,
            label = R.string.setting,
            icon = R.drawable.ic_setting_bottom_menu,
            testTag = "bnv_setting",
        ),
    )
