package com.timhome.auth.ui.signin.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import com.timhome.auth.di.DaggerAuthenticationComponent
import com.timhome.auth.ui.signin.SignInScreen
import com.timhome.auth.ui.signin.SignInViewModel
import com.timhome.core.ModularizationApplication
import com.timhome.core.common.navigation.Route
import com.timhome.core.common.navigation.SignIn
import com.timhome.core.ui.di.ViewModelFactoryContainer
import com.timhome.core.ui.slideTransition
import com.timhome.core.ui.viewmodel.routeExtras

fun EntryProviderScope<Route>.signInEntries() {
    entry<SignIn>(metadata = slideTransition()) {
        val context = LocalContext.current

        val viewModelFactory =
            remember {
                ViewModelFactoryContainer().also {
                    DaggerAuthenticationComponent.factory()
                        .create(ModularizationApplication.coreComponent(context)).inject(it)
                }.viewModelFactory
            }
        val viewModel: SignInViewModel = viewModel(factory = viewModelFactory.get(), extras = routeExtras(SignIn))
        SignInScreen(viewModel)
    }
}
