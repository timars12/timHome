package com.example.modularizationtest.ui

import android.app.AlarmManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent

class RunServiceAfterRebootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED && context != null) {
            startCo2JobService(context)
        }
    }

    private fun startCo2JobService(context: Context) {
        val jobId = 1 // Unique job ID
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val jobs = jobScheduler.allPendingJobs.singleOrNull { jobInfo -> jobInfo.id == jobId }

        if (jobs == null) {
            val jobInfo = JobInfo.Builder(jobId, ComponentName(context, CO2JobService::class.java))
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY) // Requires a network connection
                .setPeriodic(AlarmManager.INTERVAL_FIFTEEN_MINUTES) // Sets the job to repeat every 5 minutes
                .build()

            jobScheduler.schedule(jobInfo)
        }
    }
}
