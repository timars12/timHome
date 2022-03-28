package com.example.modularizationtest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null)
            initNavigation()
    }

    private fun initNavigation() {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).also { navHost ->
            val navInflater = navHost.navController.navInflater
            val navGraph = navInflater.inflate(R.navigation.nav_graph)
            navHost.navController.graph = navGraph
            navController = navHost.navController
        }
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
////                            SplitInstallManager.startConfirmationDialogForResult(...)
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