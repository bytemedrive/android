package com.bytemedrive.event

import com.bytemedrive.upload.EventFileUploaded
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

data class Event<T>(val eventType: EventType, val data: T)







class MyClassDeserializer : JsonDeserializer<Event<*>> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Event<*> {
        val jsonObject = json?.asJsonObject

        val eventType = EventType.valueOf(jsonObject?.get("eventType")?.asString!!)
        val data = when (eventType) {
            EventType.FILE_UPLOADED -> context?.deserialize<EventFileUploaded>(jsonObject.get("data"), EventFileUploaded::class.java)
        }

        return Event(eventType, data)
    }
}