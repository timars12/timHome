package com.example.core.data.models

import android.os.Parcelable
import com.example.core.utils.mvi.MviError
import kotlinx.parcelize.Parcelize

@Parcelize
data class FieldText(val data: String?, val error: MviError? = null) : Parcelable
