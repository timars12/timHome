package com.example.core.utils.mvi

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

interface MviViewState {
    val error: MviError?
}

@Parcelize
data class MviError(
    val type: ErrorType,
    val errorMessage: String? = null,
    @StringRes val errorRes: Int? = null
) : Parcelable

enum class ErrorType {
    TOAST, DIALOG, FIELD
}
