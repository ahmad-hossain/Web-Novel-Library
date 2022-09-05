package com.github.godspeed010.weblib.models

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.android.parcel.Parcelize

@Keep
@Parcelize
data class WebNovel (
    var title: String = "",
    var url: String = "",
    var progression: Float = 0f
): Parcelable