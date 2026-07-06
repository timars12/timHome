package com.timhome.auth.ui.signin.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.timhome.auth.di.DaggerAuthenticationComponent
import com.timhome.auth.ui.signin.SignInScreen
import com.timhome.auth.ui.signin.SignInViewModel
import com.timhome.core.ModularizationApplication
import com.timhome.core.di.ViewModelFactoryContainer
import com.timhome.core.ui.slideIntoContainer
import com.timhome.core.ui.slideOutOfContainer

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
