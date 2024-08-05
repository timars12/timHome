package com.example.authdynamic.ui.signin.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.authdynamic.di.DaggerAuthenticationComponent
import com.example.authdynamic.ui.signin.SignInScreen
import com.example.core.ModularizationApplication
import com.example.core.di.ViewModelFactoryContainer

fun NavGraphBuilder.signInScreen() {
    composable(route = "signInScreen") {
        val context = LocalContext.current
        val viewModelFactory =
            remember {
                ViewModelFactoryContainer().also {
                    DaggerAuthenticationComponent.factory()
                        .create(ModularizationApplication.coreComponent(context)).inject(it)
                }.viewModelFactory
            }
        SignInScreen(viewModelFactory)
    }
}
