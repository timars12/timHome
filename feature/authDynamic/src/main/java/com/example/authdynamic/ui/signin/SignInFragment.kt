package com.example.authdynamic.ui.signin

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.dynamicfeatures.DynamicExtras
import androidx.navigation.dynamicfeatures.DynamicInstallMonitor
import androidx.navigation.fragment.findNavController
import com.example.authdynamic.R
import com.example.authdynamic.di.DaggerAuthenticationComponent
import com.example.authdynamic.ui.signin.composable.EmailTextField
import com.example.authdynamic.ui.signin.composable.PasswordTextField
import com.example.core.coreComponent
import com.example.core.ui.SnackbarMessage
import com.example.core.ui.theme.HomeTheme
import com.example.core.utils.viewmodel.ViewModelFactory
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject

internal class SignInFragment : Fragment() {

    @Inject
    lateinit var abstractFactory: dagger.Lazy<ViewModelFactory>
    private val viewModel: SignInViewModel by viewModels { abstractFactory.get() }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        DaggerAuthenticationComponent.factory().create(this.coreComponent()).inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                HomeTheme {
                    val lifecycleOwner = LocalLifecycleOwner.current
                    val focusManager = LocalFocusManager.current
                    val snackbarHostState = remember { SnackbarHostState() }
                    val email by viewModel.email.collectAsStateWithLifecycle()
                    val password by viewModel.password.collectAsStateWithLifecycle()

                    val events = remember(viewModel.events, lifecycleOwner) {
                        viewModel.events.receiveAsFlow().flowWithLifecycle(
                            lifecycleOwner.lifecycle,
                            Lifecycle.State.STARTED
                        )
                    }

                    LaunchedEffect(Unit) {
                        events.collect { event ->
                            when (event) {
                                SignInEvent.GoToHomeScreen -> navigateToHome()
                                is SignInEvent.ShowErrorMessage -> {
                                    snackbarHostState.showSnackbar(message = event.error)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = CenterHorizontally
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
            }
        }
    }

    private fun navigateToHome() {
        val installMonitor = DynamicInstallMonitor()
        findNavController().navigate(
            com.example.modularizationtest.R.id.home_navigation,
            null,
            null,
            DynamicExtras(installMonitor)
        )

        if (installMonitor.isInstallRequired) {
            installMonitor.status.observe(
                viewLifecycleOwner,
                object : Observer<SplitInstallSessionState> {
                    override fun onChanged(value: SplitInstallSessionState) {
                        when (value.status()) {
                            SplitInstallSessionStatus.INSTALLED -> {
                                findNavController().navigate(
                                    com.example.modularizationtest.R.id.home_navigation
                                )
                            }
                            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
//                            SplitInstallManager.startConfirmationDialogForResult(...)
                            }
                            // Handle all remaining states:
                            SplitInstallSessionStatus.FAILED -> {
                                Log.e("0707", value.errorCode().toString())
                            }
                            SplitInstallSessionStatus.CANCELED -> {
                            }
                            else -> {
                            }
                        }

                        if (value.hasTerminalStatus()) {
                            installMonitor.status.removeObserver(this)
                        }
                    }
                }
            )
        }
    }
}
