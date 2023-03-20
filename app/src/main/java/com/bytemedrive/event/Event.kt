package com.bytemedrive.event

import com.bytemedrive.upload.EventFileUploaded
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

data class Event<T>(val eventType: EventType, val data: T)

// TODO: We were not able to get working StoreEvent annotation and get all classes by annotation via Reflection API. Get annotated class map was empty.
class EventSerializer : JsonDeserializer<Event<*>> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Event<*> {
        val jsonObject = json?.asJsonObject

        val eventType = EventType.of(jsonObject?.get("eventType")?.asString!!)
        val data = when (eventType) {
            EventType.FILE_UPLOADED -> context?.deserialize<EventFileUploaded>(jsonObject.get("data"), eventType.clazz)
        }

        return Event(eventType, data)
    }
}