package com.github.godspeed010.weblib.data

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import com.github.godspeed010.weblib.models.WebNovel

class WebNovelTypeAdapter: TypeAdapter<WebNovel>() {
    override fun write(writer: JsonWriter, value: WebNovel?) {
        writer.beginObject()

        writer.name("title")
        writer.value(value?.title)

        writer.name("url")
        writer.value(value?.url)

        writer.endObject()
    }

    override fun read(reader: JsonReader): WebNovel {
        var novel: WebNovel = WebNovel()

        if(reader.peek() == JsonToken.NULL)
        {
            reader.nextNull()
            return novel
        }

        reader.beginObject()

        while(reader.hasNext()) {
            val name = reader.nextName()
            when(name)
            {
                "title" -> novel.title = reader.nextString()
                "url" -> novel.url = reader.nextString()
            }
        }

        reader.endObject()

        return novel
    }

}