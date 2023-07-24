package com.example.modularizationtest.ui

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.base.DaggerBaseComponent
import com.example.core.coreComponent
import com.example.core.utils.NavigationDispatcher
import com.example.modularizationtest.R
import com.example.modularizationtest.di.DaggerAppComponent
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.splitcompat.SplitCompat
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavController

    @Inject
    lateinit var navigationDispatcher: NavigationDispatcher

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        when {
            isGranted -> createNotificationChannel()
            else -> explainWhyWeNeedToUseNotification()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        SplitCompat.installActivity(this)
        DaggerAppComponent
            .factory()
            .create(baseComponent = DaggerBaseComponent.factory().create(coreComponent()))
            .inject(this)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkNotificationPermission()

        if (savedInstanceState == null) {
            initNavigation()
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    observeNavigationCommands()
                }
            }
            onBackPressedDispatcher.addCallback(
                this /* lifecycle owner */,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (!navController.popBackStack()) finish()
                    }
                }
            )
            navController.addOnDestinationChangedListener { controller, _, _ ->
                if (controller.currentDestination?.id != R.id.signInFragment) { // TODO change signInFragment to graph
                    findViewById<BottomNavigationView>(R.id.bottomNavigationView).apply {
                        if (visibility == View.GONE) visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkNotificationPermission() {
        NotificationManagerCompat.from(this).apply {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && !areNotificationsEnabled() -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> createNotificationChannel()
            }
        }
    }

    private fun startCo2JobService() {
        val jobId = 1 // Unique job ID
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobs = jobScheduler.allPendingJobs.singleOrNull { jobInfo -> jobInfo.id == jobId }

        if (jobs == null) {
            val jobInfo = JobInfo.Builder(jobId, ComponentName(this, CO2JobService::class.java))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Requires a network connection
                .setPeriodic(AlarmManager.INTERVAL_FIFTEEN_MINUTES) // Sets the job to repeat every 5 minutes
                .build()

            jobScheduler.schedule(jobInfo)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = getString(R.string.notification_channel_id)
            val name = getString(R.string.app_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance)
            // Register the channel with the system
            NotificationManagerCompat.from(this).createNotificationChannel(channel)
        }
        startCo2JobService()
    }

    private fun explainWhyWeNeedToUseNotification() {
        AlertDialog.Builder(this).apply {
            setTitle(getString(R.string.enable_notifications))
            setMessage(getString(R.string.explain_why_we_should_enable_notification))
            setPositiveButton(getString(R.string.go_to_settings)) { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            create()
        }.show()
    }

    private fun initNavigation() {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).also { navHost ->
            val navInflater = navHost.navController.navInflater
            val navGraph = navInflater.inflate(R.navigation.nav_graph)
            navHost.navController.graph = navGraph
            navController = navHost.navController
            NavigationUI.setupWithNavController(
                findViewById<BottomNavigationView>(R.id.bottomNavigationView),
                navController
            )
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
