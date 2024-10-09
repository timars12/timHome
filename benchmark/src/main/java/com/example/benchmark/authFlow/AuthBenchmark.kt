package com.example.benchmark.authFlow

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import com.example.benchmark.startActivityAndAllowNotifications
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun benchmarkEmailFieldClick() {
        benchmarkRule.measureRepeated(
            packageName = "com.timhome.modularizationtest",
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            iterations = 5,
            startupMode = StartupMode.WARM,
            setupBlock = {
                // Start the app
                pressHome()
                startActivityAndAllowNotifications()
            },
        ) {
            device.findObject(By.res("email_field")).click(2_000)
        }
    }

    @Test
    fun benchmarkRegistrationClick() {
        benchmarkRule.measureRepeated(
            packageName = "com.timhome.modularizationtest",
            metrics = listOf(FrameTimingMetric()),
            compilationMode = CompilationMode.Partial(),
            iterations = 5,
            startupMode = StartupMode.WARM,
            setupBlock = {
                // Start the app
                pressHome()
                startActivityAndAllowNotifications()
            },
        ) {
            device.findObject(By.text("Registration")).click(2_000)
        }
    }
}