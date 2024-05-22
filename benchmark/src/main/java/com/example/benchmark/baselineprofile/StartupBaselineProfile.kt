package com.example.benchmark.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import com.example.benchmark.startActivityAndAllowNotifications
import org.junit.Rule
import org.junit.Test

class StartupBaselineProfile {
    @get:Rule val baselineProfileRule = BaselineProfileRule()

    // command task :benchmark:pixel3Api33BenchmarkAndroidTest --rerun-tasks -P android.testInstrumentationRunnerArguments.class=com.example.benchmark.baselineprofile.StartupBaselineProfile
    @Test
    fun generate() = baselineProfileRule.collect(
        "com.timhome.modularizationtest",
        includeInStartupProfile = true,
        profileBlock = MacrobenchmarkScope::startActivityAndAllowNotifications,
    )
}