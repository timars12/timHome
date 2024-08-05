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
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.example.authdynamic.ui.signin.navigation.signInScreen
import com.example.base.DaggerBaseComponent
import com.example.core.coreComponent
import com.example.core.utils.NavigationDispatcher
import com.example.home.ui.navigation.homeScreen
import com.example.modularizationtest.R
import com.example.modularizationtest.di.DaggerAppComponent
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.firebase.perf.metrics.AddTrace
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class MainActivity : AppCompatActivity() {
    private val navController: NavController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    }

    @Inject
    lateinit var navigationDispatcher: NavigationDispatcher

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
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

        initNavigation()
        lifecycleScope.launch {
            observeNavigationCommands()
        }
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!navController.popBackStack()) finish()
                }
            },
        )
        findViewById<ComposeView>(R.id.compose_view).apply {
            setContent {
                val navController: NavHostController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = "homeScreen",
                ) {
                    signInScreen()
                    homeScreen()
                }
            }
        }
        navController.addOnDestinationChangedListener { controller, _, _ ->
//            if (controller.currentDestination?.id != R.id.signInFragment) { // TODO change signInFragment to graph
//                findViewById<BottomNavigationView>(R.id.bottomNavigationView).apply {
//                    if (visibility == View.GONE) visibility = View.VISIBLE
//                }
//            }
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

    @AddTrace(name = "startCo2JobService", enabled = true)
    private fun startCo2JobService() {
        val jobId = 1 // Unique job ID
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobs = jobScheduler.allPendingJobs.singleOrNull { jobInfo -> jobInfo.id == jobId }

        if (jobs == null) {
            val jobInfo =
                JobInfo.Builder(jobId, ComponentName(this, CO2JobService::class.java))
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
        NavigationUI.setupWithNavController(
            findViewById<BottomNavigationView>(R.id.bottomNavigationView),
            navController,
        )
    }

    private suspend fun observeNavigationCommands() {
        navigationDispatcher.navigationEmitter.receiveAsFlow()
            .flowWithLifecycle(lifecycle, Lifecycle.State.RESUMED)
            .collect { command ->
                command.invoke(navController)
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp()
    }
}
