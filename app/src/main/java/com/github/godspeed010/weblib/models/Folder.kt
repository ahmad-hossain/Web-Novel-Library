package com.github.godspeed010.weblib.models

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class Folder (
    var name: String = "",
    var color: FolderColor = FolderColor.VANILLA,
    var webNovels: MutableList<WebNovel> = mutableListOf<WebNovel>()
    ): Parcelable