package com.example.authdynamic.ui.signin

import android.os.Parcelable
import com.example.core.data.models.FieldText
import com.example.core.utils.mvi.MviError
import com.example.core.utils.mvi.MviViewState
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginViewState(
    val email: FieldText = FieldText(data = null, error = null),
    val password: FieldText = FieldText(data = null, error = null),
    val isLoading: Boolean,
    val isLoginBtnEnable: Boolean,
    val isLoginSuccess: Boolean = false,
    override val error: MviError?,
) : MviViewState, Parcelable {
    companion object {
        fun initial() =
            LoginViewState(
                isLoading = false,
                isLoginBtnEnable = false,
                error = null,
            )
    }
}
