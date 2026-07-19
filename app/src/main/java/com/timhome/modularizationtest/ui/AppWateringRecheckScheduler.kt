package com.timhome.modularizationtest.ui

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.timhome.core.common.WateringRecheckScheduler
import java.util.concurrent.TimeUnit

private const val RECHECK_DELAY_MINUTES = 5L
private const val RECHECK_WORK_NAME = "soil_moisture_recheck"

/**
 * WorkManager-backed [WateringRecheckScheduler]. Runs one extra poll a few minutes after a
 * manual watering, giving the ESP32 time to settle and decide whether the watering worked.
 */
class AppWateringRecheckScheduler(
    private val context: Context,
) : WateringRecheckScheduler {
    override fun scheduleQuickRecheck() {
        val request =
            OneTimeWorkRequestBuilder<SoilMoistureWorker>()
                .setInitialDelay(RECHECK_DELAY_MINUTES, TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance(context)
            .enqueueUniqueWork(RECHECK_WORK_NAME, ExistingWorkPolicy.REPLACE, request)
    }
}
