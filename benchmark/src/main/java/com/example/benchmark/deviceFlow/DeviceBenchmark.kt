package com.example.benchmark.deviceFlow

import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import com.example.benchmark.startActivityAndAllowNotifications
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceBenchmark {

    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    @Test
    fun benchmarkDeviceListScrollCheck() {
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
                val registrationTab = device.findObject(By.text("Registration"))
                registrationTab.click()
                registrationTab.wait(Until.hasObject(By.res("go_home")), 1_000)

                device.findObject(By.res("go_home")).click(1_000)
                device.wait(Until.hasObject(By.res("home")), 2_000)
                device.findObject(By.res("bnv_device")).click(1_000)
                device.waitForIdle()
            },
        ) {
            val bnvDevice = device.findObject(By.res("bnv_device"))
            bnvDevice.wait(Until.hasObject(By.res("device_list")), 2_000)
            val deviceList = device.findObject(By.res("device_list"))

            deviceList.setGestureMargin(device.displayWidth / 5)
            deviceList.fling(Direction.DOWN)
            device.waitForIdle()
        }
    }
}