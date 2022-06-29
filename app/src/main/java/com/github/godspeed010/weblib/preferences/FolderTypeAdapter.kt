package com.github.godspeed010.weblib.preferences

import com.github.godspeed010.weblib.models.Folder
import com.github.godspeed010.weblib.models.FolderColor
import com.google.gson.*
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class FolderTypeAdapter: TypeAdapter<Folder>() {
    private val webNovelTypeAdapter = WebNovelTypeAdapter()

    override fun write(writer: JsonWriter, value: Folder?) {
        writer.beginObject()

        writer.name("name")
        writer.value(value?.name)

        writer.name("color")
        writer.value(value?.color?.name)

        writer.name("webNovels")
        writer.beginArray()
        value?.webNovels?.forEach {
            webNovelTypeAdapter.write(writer,it)
        }
        writer.endArray()
        writer.endObject()
    }

    override fun read(
        reader: JsonReader
    ): Folder {
        var folder: Folder = Folder()

        if(reader.peek() == JsonToken.NULL)
        {
            reader.nextNull()
            return folder
        }
        reader.beginObject();

        while(reader.hasNext())
        {
            val name = reader.nextName()
            try {
                when (name) {
                    "name" -> folder.name = reader.nextString()
                    "color" -> folder.color = FolderColor.valueOf(reader.nextString())
                    "webNovels" -> {
                        reader.beginArray()

                        while(reader.hasNext())
                        {
                            folder.webNovels.add(webNovelTypeAdapter.read(reader))
                        }

                        reader.endArray()
                    }
                    else -> reader.skipValue()
                }
            } catch(err: Error) {}
        }

        reader.endObject()

        return folder
    }
}