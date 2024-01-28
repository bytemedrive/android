package com.bytemedrive.network

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import java.text.SimpleDateFormat
import java.util.TimeZone

object JsonConfig {

    val mapper: ObjectMapper = jacksonObjectMapper()

    init {
        mapper.apply {
            registerModule(JavaTimeModule())
            registerModule(kotlinModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            setTimeZone(TimeZone.getTimeZone("UTC"))
            setDateFormat(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
        }
    }
}
