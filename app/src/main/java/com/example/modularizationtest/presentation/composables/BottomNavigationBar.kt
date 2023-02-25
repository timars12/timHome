package com.example.modularizationtest.presentation.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.core.ui.theme.SelectedTabBottomBar
import com.example.modularizationtest.R
import com.example.modularizationtest.data.BottomNavigationMenuItem
import okhttp3.internal.immutableListOf

typealias OnNavigateClick = (destinationId: Int) -> Unit

@Composable
fun BottomNavigationBar(navigateTo: OnNavigateClick) {
    val shape = remember { RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp) }
    val menuItems = remember {
        immutableListOf(
            BottomNavigationMenuItem(
                destinationId = R.id.home_navigation,
                label = R.string.home,
                icon = R.drawable.ic_home_bottom_menu
            ),
            BottomNavigationMenuItem(
                destinationId = R.id.nav_device,
                label = R.string.device,
                icon = R.drawable.ic_device_bottom_menu
            ),
            BottomNavigationMenuItem(
                destinationId = R.id.settingFragment,
                label = R.string.setting,
                icon = R.drawable.ic_setting_bottom_menu
            )
        )
    }
    var selectedMenu by rememberSaveable { mutableStateOf(menuItems.first().destinationId) }

    BottomNavigation(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(horizontal = 8.dp)
            .clip(shape),
        backgroundColor = Color.White
    ) {
        menuItems.forEach {
            BottomNavigationItem(
                selected = selectedMenu == it.destinationId,
                onClick = {
                    selectedMenu = it.destinationId
                    navigateTo(it.destinationId)
                },
                icon = {
                    Icon(
                        painter = painterResource(it.icon),
                        contentDescription = stringResource(it.label)
                    )
                },
                label = { Text(text = stringResource(it.label)) },
                alwaysShowLabel = true,
                selectedContentColor = SelectedTabBottomBar,
                unselectedContentColor = Color.Black
            )
        }
    }
}
