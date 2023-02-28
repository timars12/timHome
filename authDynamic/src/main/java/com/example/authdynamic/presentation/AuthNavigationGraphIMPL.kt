package com.example.authdynamic.presentation

import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import com.example.authdynamic.presentation.signin.SignInScreen
import com.example.core.ui.NavigationGraphAPI

class AuthNavigationGraphIMPL: NavigationGraphAPI {

    override fun registerGraph(
        navGraphBuilder: NavGraphBuilder,
        navController: NavHostController,
        modifier: Modifier
    ) {
        navGraphBuilder.navigation(startDestination = "username", route = "login"){
            composable("username") {
                SignInScreen()
            }
        }
    }
}