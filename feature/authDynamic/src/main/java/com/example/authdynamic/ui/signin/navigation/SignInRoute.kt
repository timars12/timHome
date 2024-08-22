package com.example.authdynamic.ui.signin.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.example.authdynamic.di.DaggerAuthenticationComponent
import com.example.authdynamic.ui.signin.SignInScreen
import com.example.authdynamic.ui.signin.SignInViewModel
import com.example.core.ModularizationApplication
import com.example.core.di.ViewModelFactoryContainer
import com.example.core.ui.slideIntoContainer
import com.example.core.ui.slideOutOfContainer

fun NavGraphBuilder.signInRoute() {
    composable(
        route = "signInScreen",
        enterTransition = { slideIntoContainer() },
        exitTransition = { slideOutOfContainer() },
    ) {
        val context = LocalContext.current

        val viewModelFactory =
            remember {
                ViewModelFactoryContainer().also {
                    DaggerAuthenticationComponent.factory()
                        .create(ModularizationApplication.coreComponent(context)).inject(it)
                }.viewModelFactory
            }
        val viewModel: SignInViewModel = viewModel(factory = viewModelFactory.get())
        SignInScreen(viewModel)
    }
}
