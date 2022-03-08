package com.example.modularizationtest

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.dynamicfeatures.DynamicExtras
import androidx.navigation.dynamicfeatures.DynamicInstallMonitor
import androidx.navigation.fragment.NavHostFragment
import com.google.android.play.core.splitinstall.SplitInstallSessionState
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navInflater = navHost.navController.navInflater
        val navGraph = navInflater.inflate(R.navigation.nav_graph)
//        navGraph.setStartDestination(R.id.signInFragment)
        navHost.navController.graph = navGraph
        navController = navHost.navController

        val installMonitor = DynamicInstallMonitor()
        navController.navigate(
            R.id.signInFragment,
            null,
            null,
            DynamicExtras(installMonitor)
        )

        if (installMonitor.isInstallRequired) {
            installMonitor.status.observe(this, object : Observer<SplitInstallSessionState> {
                override fun onChanged(sessionState: SplitInstallSessionState) {
                    when (sessionState.status()) {
                        SplitInstallSessionStatus.INSTALLED -> {
                            navController.navigate(R.id.signInFragment)
                        }
                        SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
//                            SplitInstallManager.startConfirmationDialogForResult(...)
                        }
                        // Handle all remaining states:
                        SplitInstallSessionStatus.FAILED -> {}
                        SplitInstallSessionStatus.CANCELED -> {}
                        else -> {}
                    }

                    if (sessionState.hasTerminalStatus()) {
                        installMonitor.status.removeObserver(this)
                    }
                }
            })
        }
    }
}