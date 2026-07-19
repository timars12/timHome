package com.timhome.core.common

/**
 * Lets a feature request a quick, one-off soil-moisture re-poll shortly after the user
 * taps "water now", so the ESP32's watering verdict is picked up in a few minutes instead
 * of waiting for the next 30-minute cycle.
 *
 * The real implementation lives in the app module (it needs WorkManager and the worker
 * class, which sit above the feature/core modules), and is installed into [instance] at
 * startup. Tests and non-app callers get the no-op default.
 */
interface WateringRecheckScheduler {
    fun scheduleQuickRecheck()

    companion object {
        @Volatile
        var instance: WateringRecheckScheduler =
            object : WateringRecheckScheduler {
                override fun scheduleQuickRecheck() = Unit
            }
    }
}
