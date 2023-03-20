package com.bytemedrive.event

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

// TODO: We were not able to get working StoreEvent annotation and get all classes by annotation via Reflection API. Get annotated class map was empty.
class EventSerializer : StdDeserializer<Event<*>>(Event::class.java) {
    override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): Event<*> {
        val node: JsonNode? = p?.getCodec()?.readTree(p)

        val eventType = EventType.of(node?.get("eventType")?.asText()!!)

        val data = ctxt?.readTreeAsValue(node.get("data"), eventType.clazz)

        return Event(eventType, data)
    }
}