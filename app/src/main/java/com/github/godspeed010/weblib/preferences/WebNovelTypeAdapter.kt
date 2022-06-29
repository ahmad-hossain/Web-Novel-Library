package com.github.godspeed010.weblib.preferences

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.github.godspeed010.weblib.models.WebNovel

class WebNovelTypeAdapter: TypeAdapter<WebNovel>() {
    override fun write(writer: JsonWriter, value: WebNovel?) {
        if(value == null) return;

        writer.beginObject()

        writer.name("title")
        writer.value(value.title)

        writer.name("url")
        writer.value(value.url)

        writer.name("progression")
        writer.value(value.progression.toDouble())

        writer.endObject()
    }

    override fun read(reader: JsonReader): WebNovel {
        var novel = WebNovel()

        if(reader.peek() == JsonToken.NULL)
        {
            reader.nextNull()
            return novel
        }

        reader.beginObject()

        while(reader.hasNext()) {
            when(reader.nextName())
            {
                "title" -> novel.title = reader.nextString()
                "url" -> novel.url = reader.nextString()
                "progression" -> novel.progression = reader.nextDouble().toFloat()
                else -> reader.skipValue()
            }
        }

        reader.endObject()

        return novel
    }

}