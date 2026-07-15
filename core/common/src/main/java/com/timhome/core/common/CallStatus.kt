package com.timhome.core.common

sealed class CallStatus<out T> {
    data class Success<T>(val data: T? = null) : CallStatus<T>()

    data class Error(val error: String? = null) : CallStatus<Nothing>()
}
