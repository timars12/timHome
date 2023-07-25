package com.example.modularizationtest.ui.composables

// import androidx.compose.foundation.layout.fillMaxWidth
// import androidx.compose.foundation.layout.height
// import androidx.compose.foundation.layout.padding
// import androidx.compose.foundation.shape.RoundedCornerShape
// import androidx.compose.material3.Icon
// import androidx.compose.material3.NavigationBar
// import androidx.compose.material3.NavigationBarItem
// import androidx.compose.material3.NavigationBarItemDefaults
// import androidx.compose.material3.Text
// import androidx.compose.runtime.*
// import androidx.compose.runtime.saveable.rememberSaveable
// import androidx.compose.ui.Modifier
// import androidx.compose.ui.draw.clip
// import androidx.compose.ui.graphics.Color
// import androidx.compose.ui.res.painterResource
// import androidx.compose.ui.res.stringResource
// import androidx.compose.ui.unit.dp
// import com.example.core.ui.theme.SelectedTabBottomBar
// import com.example.modularizationtest.R
// import com.example.modularizationtest.data.BottomNavigationMenuItem
// import okhttp3.internal.toImmutableList

// typealias OnNavigateClick = (destinationId: Int) -> Unit

// can not use because has problem with stack of destination
// @Composable
// fun BottomNavigationBar(navigateTo: OnNavigateClick) {
//    val shape = remember { RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp) }
//    val menuItems = // TODO always recomposition
//        listOf(
//            BottomNavigationMenuItem(
//                destinationId = R.id.home_navigation,
//                label = R.string.home,
//                icon = R.drawable.ic_home_bottom_menu
//            ),
//            BottomNavigationMenuItem(
//                destinationId = R.id.nav_device,
//                label = R.string.device,
//                icon = R.drawable.ic_device_bottom_menu
//            ),
//            BottomNavigationMenuItem(
//                destinationId = R.id.settingFragment,
//                label = R.string.setting,
//                icon = R.drawable.ic_setting_bottom_menu
//            )
//        ).toImmutableList()
//
//    val selectedMenu = rememberSaveable { mutableStateOf(menuItems.first().destinationId) }
//
//    NavigationBar(
//        modifier = Modifier
//            .fillMaxWidth()
//            .height(80.dp)
//            .padding(horizontal = 8.dp)
//            .clip(shape),
//        containerColor = Color.White
//    ) {
//        menuItems.forEach {
//            NavigationBarItem(
//                selected = selectedMenu.value == it.destinationId,
//                onClick = {
//                    if (selectedMenu.value == it.destinationId) return@NavigationBarItem
//                    selectedMenu.value = it.destinationId
//                    navigateTo(it.destinationId)
//                },
//                icon = {
//                    Icon(
//                        painter = painterResource(it.icon),
//                        contentDescription = stringResource(it.label)
//                    )
//                },
//                label = { Text(text = stringResource(it.label)) },
//                alwaysShowLabel = true,
//                colors = NavigationBarItemDefaults.colors(
//                    selectedIconColor = SelectedTabBottomBar,
//                    selectedTextColor = SelectedTabBottomBar,
//                    unselectedIconColor = Color.Black,
//                    unselectedTextColor = Color.Black
//                )
//            )
//        }
//    }
// }
