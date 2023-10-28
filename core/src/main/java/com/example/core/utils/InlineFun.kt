package com.example.core.utils

import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance

inline fun <T> tryOrNull(f: () -> T) =
    try {
        f()
    } catch (e: Exception) {
        Log.i("Retrofit Error", "$e")
        null
    }

//works only on main thread
suspend inline fun <T> coroutineBlockTrace(label: String, crossinline block: suspend () -> T) {
    val perf = Firebase.performance.newTrace(label)
    perf.start()
    block()
    perf.stop()
}

//works only on main thread
inline fun <T> trace(label: String, crossinline block: () -> T) {
    val perf = Firebase.performance.newTrace(label)
    perf.start()
    block()
    perf.stop()
}
