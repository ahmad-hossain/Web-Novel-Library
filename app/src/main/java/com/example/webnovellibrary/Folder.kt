package com.example.webnovellibrary

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Folder (
    var name: String,
    var webNovels: MutableList<WebNovel> = mutableListOf<WebNovel>()
    ): Parcelable