package com.github.godspeed010.weblib.models

import android.os.Parcelable
import com.github.godspeed010.weblib.R
import kotlinx.parcelize.Parcelize

@Parcelize
enum class FolderColor(val rgbId: Int) : Parcelable {
    VANILLA(R.color.folder_vanilla),
    RED(R.color.folder_red),
    ORANGE(R.color.folder_orange),
    YELLOW(R.color.folder_yellow),
    GREEN(R.color.folder_green),
    BLUE(R.color.folder_blue),
    PURPLE(R.color.folder_purple);

    companion object {
        private val map = FolderColor.values().associateBy(FolderColor::rgbId);
        fun fromRGBId(rgbId: Int): FolderColor? {
          return map[rgbId]
        }
    }
}