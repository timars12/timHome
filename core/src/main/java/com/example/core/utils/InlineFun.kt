package com.example.core.utils

import android.util.Log

inline fun <T> tryOrNull(f: () -> T) =
    try {
        f()
    } catch (e: Exception) {
        Log.i("Retrofit Error", "$e")
        null
    }
