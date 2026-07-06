package com.timhome.benchmark.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.junit4.BaselineProfileRule
import com.timhome.benchmark.startActivityAndAllowNotifications
import org.junit.Rule
import org.junit.Test

class StartupBaselineProfile {
    @get:Rule val baselineProfileRule = BaselineProfileRule()

    // command task :benchmark:pixel4Api33BenchmarkAndroidTest --rerun-tasks -P android.testInstrumentationRunnerArguments.class=com.timhome.benchmark.baselineprofile.StartupBaselineProfile
    @Test
    fun generate() = baselineProfileRule.collect(
        "com.timhome.modularizationtest",
        includeInStartupProfile = true,
        profileBlock = MacrobenchmarkScope::startActivityAndAllowNotifications,
    )
}