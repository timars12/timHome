package com.timhome.core.data.models

import android.os.Parcelable
import com.timhome.core.utils.mvi.MviError
import kotlinx.parcelize.Parcelize

@Parcelize
data class FieldText(val data: String?, val error: MviError? = null) : Parcelable
