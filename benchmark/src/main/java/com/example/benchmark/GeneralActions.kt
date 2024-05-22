package com.example.benchmark

import android.Manifest
import android.os.Build
import androidx.benchmark.macro.MacrobenchmarkScope

fun MacrobenchmarkScope.allowNotifications() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val command = "pm grant $packageName ${Manifest.permission.POST_NOTIFICATIONS}"
        device.executeShellCommand(command)
    }
}

/**
 * Wraps starting the default activity, waiting for it to start and then allowing notifications in
 * one convenient call.
 */
fun MacrobenchmarkScope.startActivityAndAllowNotifications() {
    startActivityAndWait()
    allowNotifications()
}

//fun MacrobenchmarkScope.forYouWaitForContent() {
//    // Wait until content is loaded by checking if topics are loaded
//    device.wait(Until.gone(By.res("loadingWheel")), 5_000)
//    // Sometimes, the loading wheel is gone, but the content is not loaded yet
//    // So we'll wait here for topics to be sure
//    val obj = device.waitAndFindObject(By.res("forYou:topicSelection"), 10_000)
//    // Timeout here is quite big, because sometimes data loading takes a long time!
//    obj.wait(untilHasChildren(), 60_000)
//}
//
///**
// * Waits until an object with [selector] if visible on screen and returns the object.
// * If the element is not available in [timeout], throws [AssertionError]
// */
//fun UiDevice.waitAndFindObject(selector: BySelector, timeout: Long): UiObject2 {
//    if (!wait(Until.hasObject(selector), timeout)) {
//        throw AssertionError("Element not found on screen in ${timeout}ms (selector=$selector)")
//    }
//
//    return findObject(selector)
//}
//
///**
// * Condition will be satisfied if given element has specified count of children
// */
//fun untilHasChildren(
//    childCount: Int = 1,
//    op: HasChildrenOp = AT_LEAST,
//): UiObject2Condition<Boolean> = object : UiObject2Condition<Boolean>() {
//    override fun apply(element: UiObject2): Boolean = when (op) {
//        AT_LEAST -> element.childCount >= childCount
//        EXACTLY -> element.childCount == childCount
//        AT_MOST -> element.childCount <= childCount
//    }
//}
//
//enum class HasChildrenOp {
//    AT_LEAST,
//    EXACTLY,
//    AT_MOST,
//}