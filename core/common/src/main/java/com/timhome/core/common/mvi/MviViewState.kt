package com.timhome.core.common.mvi
// TODO(Task 05): dead MVI base parked here; remove when features move to MVVM.

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
    @StringRes val errorRes: Int? = null,
) : Parcelable

enum class ErrorType {
    TOAST,
    DIALOG,
    FIELD,
}
