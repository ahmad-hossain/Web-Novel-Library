package com.github.godspeed010.weblib.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Folder (
    var name: String = "",
    var color: FolderColor = FolderColor.VANILLA,
    var webNovels: MutableList<WebNovel> = mutableListOf<WebNovel>()
    ): Parcelable