package com.github.godspeed010.weblib.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import com.github.godspeed010.weblib.Constants
import com.github.godspeed010.weblib.data.FolderTypeAdapter
import com.github.godspeed010.weblib.data.WebNovelTypeAdapter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import com.github.godspeed010.weblib.models.WebNovel
import com.github.godspeed010.weblib.models.Folder

class PreferencesUtils {
    companion object {
        var gson: Gson

        init {
            var builder = GsonBuilder()
            builder.registerTypeAdapter(Folder::class.java, FolderTypeAdapter())
            builder.registerTypeAdapter(WebNovel::class.java, WebNovelTypeAdapter())
            gson = builder.create()
        }

        fun loadFolders(activity: Activity?): MutableList<Folder> {
            val json = activity!!.loadJsonData()

            val type: Type = object : TypeToken<ArrayList<Folder?>?>() {}.type

            val folders: MutableList<Folder>? = gson.fromJson(json, type)

            return folders ?: mutableListOf<Folder>()
        }

        fun saveFolders(activity: Activity?, folders: MutableList<Folder>) {
            val sharedPreferences: SharedPreferences = activity!!.getSharedPreferences(Constants.KEY_SHARED_PREFERENCES, Context.MODE_PRIVATE)

            val editor = sharedPreferences.edit()

            val json: String = gson.toJson(folders)

            editor.putString(Constants.KEY_FOLDERS_DATA, json)

            editor.apply()
        }
    }
}

fun Context.loadJsonData(): String? {
    val sharedPreferences: SharedPreferences = getSharedPreferences(Constants.KEY_SHARED_PREFERENCES, Context.MODE_PRIVATE)

    val emptyList = Gson().toJson(ArrayList<Folder>())

    val json = sharedPreferences.getString(Constants.KEY_FOLDERS_DATA, emptyList)

    return json
}