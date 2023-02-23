package com.example.modularizationtest.presentation

import android.content.Context
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.core.coreComponent
import com.example.core.utils.NavigationDispatcher
import com.example.modularizationtest.R
import com.example.modularizationtest.di.DaggerAppComponent
import com.example.modularizationtest.presentation.composables.BottomNavigationBar
import com.google.android.play.core.splitcompat.SplitCompat
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    @Inject
    lateinit var navigationDispatcher: NavigationDispatcher

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
        DaggerAppComponent.factory().create(this.coreComponent()).inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initNavigation()
        if (savedInstanceState == null) {
            lifecycleScope.launchWhenResumed { observeNavigationCommands() }
            onBackPressedDispatcher.addCallback(
                this /* lifecycle owner */,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (!navController.popBackStack()) finish()
                    }
                }
            )
        }
        findViewById<ComposeView>(R.id.compose_view).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val destinationId by produceState(initialValue = R.id.signInFragment) {
                    navController.addOnDestinationChangedListener { _, destination, _ ->
                        value = destination.id
                    }
                }
                if (destinationId != R.id.signInFragment) { // TODO check if token exist if not hide bar
                    BottomNavigationBar {
                        // TODO add navigationSafe
                        navigationDispatcher.emit { nav -> nav.navigate(it) }
                    }
                }
            }
        }
    }

    private fun initNavigation() {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).also { navHost ->
            val navInflater = navHost.navController.navInflater
            val navGraph = navInflater.inflate(R.navigation.nav_graph)
            navHost.navController.graph = navGraph
            navController = navHost.navController
        }
    }

    private suspend fun observeNavigationCommands() {
        for (command in navigationDispatcher.navigationEmitter) {
            command.invoke(navController)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }

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
