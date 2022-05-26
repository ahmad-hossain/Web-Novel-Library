package com.github.godspeed010.weblib.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class WebNovel (
    var title: String,
    var url: String
    ): Parcelable