package com.example.authdynamic.presentation.signin

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.savedstate.SavedStateRegistryOwner
import com.example.authdynamic.R
import com.example.authdynamic.di.DaggerAuthenticationComponent
import com.example.authdynamic.presentation.signin.composable.EmailTextField
import com.example.authdynamic.presentation.signin.composable.PasswordTextField
import com.example.core.ModularizationApplication
import com.example.core.ui.SnackbarMessage
import kotlinx.coroutines.flow.receiveAsFlow

@Composable
fun SignInScreen() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val abstractFactory = DaggerAuthenticationComponent.factory().create(
        ModularizationApplication.coreComponent(context)).getViewModel()

    val viewModelFactory = abstractFactory.create(lifecycleOwner as SavedStateRegistryOwner)
    val viewModel: SignInViewModel = viewModelFactory.create(SignInViewModel::class.java)

    val focusManager = LocalFocusManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val email by viewModel.email.observeAsState()
    val password by viewModel.password.observeAsState()

    val events = remember(viewModel.events, lifecycleOwner) {
        viewModel.events.receiveAsFlow().flowWithLifecycle(
            lifecycleOwner.lifecycle,
            Lifecycle.State.STARTED
        )
    }

    LaunchedEffect(Unit) {
//        events.collect { event ->
//            when (event) {
//                is SignInEvent.GoToHomeScreen -> navigateToHome()
//                is SignInEvent.ShowErrorMessage -> {
//                    snackbarHostState.showSnackbar(message = event.error)
//                }
//            }
//        }
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            EmailTextField(email, focusManager, viewModel::onEnterEmail)
            PasswordTextField(
                password = password,
                focusManager = focusManager,
                onEnterText = viewModel::onEnterPassword,
                onClickDone = viewModel::onSignInByEmail
            )
            Button(onClick = viewModel::onSignInByEmail) {
                Text(stringResource(id = R.string.sign_in))
            }
        }
        SnackbarMessage(
            snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
        )
    }
}