package com.example.modularizationtest.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.dynamicfeatures.DynamicInstallMonitor
import com.example.core.coreComponent
import com.example.core.ui.Destination
import com.example.core.ui.ExpandableText
import com.example.core.ui.NavigationGraphAPI
import com.example.core.ui.theme.HomeTheme
import com.example.core.utils.NavigationDispatcher
import com.example.core.utils.OnClick
import com.example.modularizationtest.di.DaggerAppComponent
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

class MainActivity : ComponentActivity() {

    @Inject
    lateinit var navigationDispatcher: NavigationDispatcher

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
        DaggerAppComponent.factory().create(this.coreComponent()).inject(this)
    }

    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        applyEdgeToEdge()
        setContent {
            HomeTheme {
                val lifecycleOwner = LocalLifecycleOwner.current
                val navController = rememberNavController()
                val isShowBottomMenu = remember { MutableStateFlow(false) }

                val navigationEvents = remember(navigationDispatcher.emitter, lifecycleOwner) {
                    navigationDispatcher.emitter.flowWithLifecycle(
                        lifecycleOwner.lifecycle,
                        Lifecycle.State.STARTED
                    )
                }

                DisposableEffect(navController) {
                    val callback = NavController.OnDestinationChangedListener { _, destination, _ ->
                        isShowBottomMenu.value = when (destination.route) {
                            Destination.SampleContainer.destination -> true
                            else -> false
                        }
                    }
                    navController.addOnDestinationChangedListener(callback)
                    onDispose {
                        navController.removeOnDestinationChangedListener(callback)
                    }
                }

                LaunchedEffect(Unit) {
                    navigationEvents.collect { event -> event(navController) }
                }

                NavHost(
                    navController = navController,
                    startDestination = Destination.SampleContainer.destination,
                ) {
                    composable(Destination.SampleContainer.destination) {
                        navigateToAuthFlow {
//TODO тут проблема в тому що не мож викликати dynamic-features-fragment без fragment
                        }
                    }
                    composable(Destination.SampleContainer1.destination) {
                        ExpandableText(text = "SampleContainer1")
                    }

                }
            }
        }
//        findViewById<ComposeView>(R.id.compose_view).apply {
//            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
//            setContent {
//                val destinationId by produceState(initialValue = R.id.signInFragment) {
//                    navController.addOnDestinationChangedListener { _, destination, _ ->
//                        value = destination.id
//                    }
//                }
//                if (destinationId != R.id.signInFragment) { // TODO check if token exist if not hide bar
//                    BottomNavigationBar {
//                        // TODO add navigationSafe
//                        navigationDispatcher.emit { nav -> nav.navigate(it) }
//                    }
//                }
//            }
//        }
    }

    private fun navigateToAuthFlow(onClick: OnClick) {
        val installMonitor = DynamicInstallMonitor()
        if (installMonitor.isInstallRequired) {
            installMonitor.status.observe(
                this, object : Observer<SplitInstallSessionState> {
                    override fun onChanged(sessionState: SplitInstallSessionState) {
                        when (sessionState.status()) {
                            SplitInstallSessionStatus.INSTALLED -> {
                                onClick()
                            }
                            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
//                            SplitInstallManager.startConfirmationDialogForResult(...)
                            }
                            // Handle all remaining states:
                            SplitInstallSessionStatus.FAILED -> {
                                Log.e("0707", sessionState.errorCode().toString())
                            }
                            SplitInstallSessionStatus.CANCELED -> {
                            }
                            else -> {
                            }
                        }

                        if (sessionState.hasTerminalStatus()) {
                            installMonitor.status.removeObserver(this)
                        }
                    }
                }
            )
        }
    }

    private fun applyEdgeToEdge() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

//    private suspend fun observeNavigationCommands() {
//        for (command in navigationDispatcher.navigationEmitter) {
//            command.invoke(navController)
//        }
//    }

//    private fun navigateWhenDownloadModule() {
//        val installMonitor = DynamicInstallMonitor()
//        navController.navigate(
//            R.id.signInFragment,
//            null,
//            null,
//            DynamicExtras(installMonitor)
//        )
//
//        if (installMonitor.isInstallRequired) {
//            installMonitor.status.observe(this, object : Observer<SplitInstallSessionState> {
//                override fun onChanged(sessionState: SplitInstallSessionState) {
//                    when (sessionState.status()) {
//                        SplitInstallSessionStatus.INSTALLED -> {
//                            navController.navigate(R.id.signInFragment)
//                        }
//                        SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
// //                            SplitInstallManager.startConfirmationDialogForResult(...)
//                        }
//                        // Handle all remaining states:
//                        SplitInstallSessionStatus.FAILED -> {}
//                        SplitInstallSessionStatus.CANCELED -> {}
//                        else -> {}
//                    }
//
//                    if (sessionState.hasTerminalStatus()) {
//                        installMonitor.status.removeObserver(this)
//                    }
//                }
//            })
//        }
//    }
}
