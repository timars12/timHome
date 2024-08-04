package com.example.authdynamic.ui.signin.navigation

import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navDeepLink
import androidx.navigation.compose.composable
import com.example.authdynamic.di.DaggerAuthenticationComponent
import com.example.authdynamic.ui.signin.SignInScreen
import com.example.authdynamic.ui.signin.di.SignInContainer
import com.example.core.ModularizationApplication

private const val DEEP_LINK_URI_PATTERN =
    "com.example.authdynamic.ui.signIn"

fun NavGraphBuilder.signInScreen() {
    composable(
        route = "signInScreen",
        deepLinks = listOf(
            navDeepLink { uriPattern = DEEP_LINK_URI_PATTERN },
        ),
    ) {
        val context = LocalContext.current
        val viewModelFactory = remember {
            SignInContainer().also {
                DaggerAuthenticationComponent.factory()
                    .create(ModularizationApplication.coreComponent(context)).inject(it)
            }.viewModelFactory
        }
        SignInScreen(viewModelFactory)
    }
}